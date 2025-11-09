from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.providers.postgres.hooks.postgres import PostgresHook
from datetime import datetime, timedelta
import pandas as pd
import logging
from clickhouse_driver import Client

default_args = {
    'owner': 'bionicpro_etl',
    'start_date': datetime(2025, 11, 1),
    'retries': 2,
    'retry_delay': timedelta(minutes=5),
}

def get_clickhouse_client():
    """Get ClickHouse client connection"""
    return Client(
        host='clickhouse-server',
        port=9000,
        user='airflow',
        password='airflow',
        database='olap_db'
    )

def extract_crm_data():
    """Extract data from CRM database"""
    try:
        hook = PostgresHook(postgres_conn_id='crm_db')

        conn = hook.get_conn()
        logging.info("Successfully connected to CRM database")

        customers_query = """
        SELECT customer_id, username, email, first_name, last_name, registration_date, role 
        FROM customers
        """
        customers_df = hook.get_pandas_df(customers_query)
        logging.info(f"Retrieved {len(customers_df)} customers")

        customers_df['registration_date'] = pd.to_datetime(customers_df['registration_date']).dt.date

        orders_query = """
        SELECT customer_id, COUNT(*) as total_orders, SUM(total_amount) as total_order_amount,
               STRING_AGG(DISTINCT product_type, ', ') as product_types
        FROM orders 
        WHERE status = 'completed'
        GROUP BY customer_id
        """
        orders_df = hook.get_pandas_df(orders_query)

        crm_data = pd.merge(customers_df, orders_df, on='customer_id', how='left')
        crm_data.fillna({'total_orders': 0, 'total_order_amount': 0, 'product_types': 'No orders'}, inplace=True)

        crm_data.to_csv('/tmp/crm_extracted_data.csv', index=False)
        logging.info(f"Extracted {len(crm_data)} CRM records")
    except Exception as e:
        logging.error(f"Error extracting CRM data: {str(e)}")
        raise

def extract_telemetry_data():
    """Extract data from telemetry database"""
    try:
        hook = PostgresHook(postgres_conn_id='telemetry_db')

        telemetry_query = """
        SELECT 
            customer_id,
            session_date,
            SUM(usage_minutes) as daily_usage_minutes,
            SUM(movement_count) as daily_movements,
            SUM(error_count) as daily_errors,
            AVG(battery_level) as avg_battery,
            AVG(signal_quality) as avg_signal_quality
        FROM telemetry_data 
        WHERE session_date >= CURRENT_DATE - INTERVAL '7 days'
        GROUP BY customer_id, session_date
        """
        telemetry_df = hook.get_pandas_df(telemetry_query)

        telemetry_df['session_date'] = pd.to_datetime(telemetry_df['session_date']).dt.date

        movement_query = """
        SELECT 
            customer_id,
            session_date,
            AVG(success_rate) as avg_success_rate,
            AVG(response_time_ms) as avg_response_time
        FROM movement_logs 
        WHERE session_date >= CURRENT_DATE - INTERVAL '7 days'
        GROUP BY customer_id, session_date
        """
        movement_df = hook.get_pandas_df(movement_query)

        movement_df['session_date'] = pd.to_datetime(movement_df['session_date']).dt.date

        telemetry_data = pd.merge(telemetry_df, movement_df, on=['customer_id', 'session_date'], how='left')
        telemetry_data.fillna({'avg_success_rate': 0, 'avg_response_time': 0}, inplace=True)

        telemetry_data.to_csv('/tmp/telemetry_extracted_data.csv', index=False)
        logging.info(f"Extracted {len(telemetry_data)} telemetry records")
    except Exception as e:
        logging.error(f"Error extracting telemetry data: {str(e)}")
        raise

def transform_and_load_to_clickhouse():
    """Transform and load data into ClickHouse"""
    import pandas as pd
    from datetime import datetime

    crm_data = pd.read_csv('/tmp/crm_extracted_data.csv')
    telemetry_data = pd.read_csv('/tmp/telemetry_extracted_data.csv')

    crm_data['registration_date'] = pd.to_datetime(crm_data['registration_date']).dt.date

    telemetry_agg = telemetry_data.groupby('customer_id').agg({
        'daily_usage_minutes': ['sum', 'mean'],
        'daily_movements': 'sum',
        'daily_errors': 'sum',
        'avg_battery': 'mean',
        'avg_signal_quality': 'mean',
        'avg_success_rate': 'mean',
        'avg_response_time': 'mean'
    }).round(2)

    telemetry_agg.columns = [
        'total_usage_minutes', 'avg_daily_usage_minutes',
        'total_movements', 'total_errors', 'avg_battery_level',
        'avg_signal_quality', 'avg_success_rate', 'avg_response_time_ms'
    ]
    telemetry_agg = telemetry_agg.reset_index()

    final_data = pd.merge(crm_data, telemetry_agg, on='customer_id', how='left')

    telemetry_columns = ['total_usage_minutes', 'avg_daily_usage_minutes', 'total_movements',
                         'total_errors', 'avg_battery_level', 'avg_signal_quality',
                         'avg_success_rate', 'avg_response_time_ms']

    for col in telemetry_columns:
        final_data[col] = final_data[col].fillna(0)

    final_data['usage_efficiency_score'] = (
            (final_data['avg_success_rate'] * 0.6) +
            ((100 - final_data['avg_response_time_ms'] / 2) * 0.4)
    ).round(2)

    final_data['movement_accuracy_score'] = (
            (final_data['avg_success_rate'] * 0.7) +
            (final_data['avg_signal_quality'] * 0.3)
    ).round(2)

    final_data['report_date'] = datetime.now().date()
    final_data['mart_id'] = range(1, len(final_data) + 1)
    final_data['last_updated'] = datetime.now()

    final_data['total_usage_minutes'] = final_data['total_usage_minutes'].astype(int)
    final_data['total_movements'] = final_data['total_movements'].astype(int)
    final_data['total_errors'] = final_data['total_errors'].astype(int)
    final_data['total_orders'] = final_data['total_orders'].astype(int)

    clickhouse_data = []
    for _, row in final_data.iterrows():
        clickhouse_data.append({
            'mart_id': row['mart_id'],
            'customer_id': row['customer_id'],
            'username': str(row['username']),
            'email': str(row['email']),
            'customer_role': str(row['role']),
            'report_date': row['report_date'],
            'total_usage_minutes': row['total_usage_minutes'],
            'avg_daily_usage_minutes': float(row['avg_daily_usage_minutes']),
            'total_movements': row['total_movements'],
            'avg_success_rate': float(row['avg_success_rate']),
            'avg_response_time_ms': float(row['avg_response_time_ms']),
            'total_errors': row['total_errors'],
            'avg_battery_level': float(row['avg_battery_level']),
            'avg_signal_quality': float(row['avg_signal_quality']),
            'registration_date': row['registration_date'],
            'product_type': str(row['product_types']),
            'total_orders': row['total_orders'],
            'total_order_amount': float(row['total_order_amount']),
            'usage_efficiency_score': float(row['usage_efficiency_score']),
            'movement_accuracy_score': float(row['movement_accuracy_score']),
            'last_updated': row['last_updated']
        })

    client = get_clickhouse_client()

    client.execute("ALTER TABLE customer_telemetry_mart DELETE WHERE report_date = %(report_date)s",
                   {'report_date': datetime.now().date()})

    if clickhouse_data:
        client.execute(
            """
            INSERT INTO customer_telemetry_mart (
                mart_id, customer_id, username, email, customer_role, report_date,
                total_usage_minutes, avg_daily_usage_minutes, total_movements,
                avg_success_rate, avg_response_time_ms, total_errors,
                avg_battery_level, avg_signal_quality, registration_date,
                product_type, total_orders, total_order_amount,
                usage_efficiency_score, movement_accuracy_score, last_updated
            ) VALUES
            """,
            clickhouse_data
        )

    logging.info(f"Loaded {len(clickhouse_data)} records into ClickHouse")

def create_daily_summary():
    """Create daily summary aggregates in ClickHouse"""
    client = get_clickhouse_client()

    summary_query = """
    INSERT INTO daily_telemetry_summary
    SELECT 
        report_date as summary_date,
        customer_role,
        COUNT(*) as total_customers,
        SUM(total_usage_minutes) / 60.0 as total_usage_hours,
        AVG(avg_success_rate) as avg_success_rate,
        AVG(avg_response_time_ms) as avg_response_time_ms,
        SUM(total_errors) as total_errors,
        AVG(avg_battery_level) as avg_battery_level
    FROM customer_telemetry_mart 
    WHERE report_date = %(report_date)s
    GROUP BY report_date, customer_role
    """

    client.execute(summary_query, {'report_date': datetime.now().date()})
    logging.info("Created daily summary aggregates")

with DAG(
        'bionicpro_clickhouse_etl',
        default_args=default_args,
        description='ETL pipeline for BionicPRO with ClickHouse OLAP',
        schedule_interval='0 2 * * *',
        catchup=False,
        tags=['bionicpro', 'clickhouse', 'etl', 'reports']
) as dag:

    extract_crm_task = PythonOperator(
        task_id='extract_crm_data',
        python_callable=extract_crm_data
    )

    extract_telemetry_task = PythonOperator(
        task_id='extract_telemetry_data',
        python_callable=extract_telemetry_data
    )

    transform_load_clickhouse_task = PythonOperator(
        task_id='transform_and_load_to_clickhouse',
        python_callable=transform_and_load_to_clickhouse
    )

    create_daily_summary_task = PythonOperator(
        task_id='create_daily_summary',
        python_callable=create_daily_summary
    )

    extract_crm_task >> extract_telemetry_task >> transform_load_clickhouse_task >> create_daily_summary_task