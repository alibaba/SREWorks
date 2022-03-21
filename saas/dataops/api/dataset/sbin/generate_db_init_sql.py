# coding: utf-8

import time
import pymysql
from pymysql.cursors import DictCursor

host = "data-mysql.ca221ae8860d9421688e59c8ab45c8b21.cn-hangzhou.alicontainer.com"
port = 13308
user = "root"
password = "root"
db_name = "dataset"

def convert_str_column(value):
    return "'" + str(value) + "'" if value is not None else 'null'

def convert_int_column(value):
    return str(int(value)) if value is not None else 'null'

def convert_bool_column(value):
    return str(value).lower() if value is not None else 'null'

def convert_datetime_column(value):
    return "'" + str(value) + "'" if value is not None else 'null'


COLUMN_MAPPING = {
    'tinyint': convert_int_column,
    'int': convert_int_column,
    'bigint': convert_int_column,
    'varchar': convert_str_column,
    'text': convert_str_column,
    'datetime': convert_datetime_column

}


def get_db_conn(max_try_count=3, autocommit=True):
    success_connect = False
    try_count = 1
    while not success_connect and try_count <= max_try_count:
        try:
            try_count = try_count + 1
            conn = pymysql.connect(host=host, user=user, password=password, db=db_name, port=port, charset='utf8',
                                   cursorclass=DictCursor, connect_timeout=10, read_timeout=600, autocommit=autocommit)
            return conn
        except Exception as ex:
            print(ex)
            time.sleep(10)
    return None


def query(dbcon, sql):
    cur = dbcon.cursor()
    cur.execute(sql)
    res = cur.fetchall()
    dbcon.commit()
    return res


def get(conn, db_name, table_name):
    schema_column_sql = f'''
        SELECT 
            TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, IS_NULLABLE, DATA_TYPE, COLUMN_TYPE
        FROM 
            INFORMATION_SCHEMA.COLUMNS 
        WHERE 
            TABLE_SCHEMA='{db_name}' AND TABLE_NAME='{table_name}';
    '''
    schema_columns = query(conn, schema_column_sql)
    columns_define_mapping = {}
    for schema_column in schema_columns:
        columns_define_mapping[schema_column['COLUMN_NAME']] = schema_column
    # print(columns_define_mapping)

    data_sql = f'''
        SELECT
            *
        FROM
            {db_name}.{table_name}
    '''
    data_results = query(conn, data_sql)
    # print(data_results)

    sql_list = []
    for data in data_results:
        columns = []
        values = []

        for k, v in data.items():
            columns.append("`" + k + "`")
            column_define = columns_define_mapping.get(k)
            # print(column_define)
            convert_func = COLUMN_MAPPING.get(column_define.get('DATA_TYPE'))
            if convert_func:
                values.append(convert_func(v))
            else:
                raise "type error"

        columns_str = ','.join(columns)
        values_str = ','.join(values)

        sql = f'''REPLACE INTO `{table_name}` ({columns_str}) VALUES ({values_str});'''
        sql_list.append(sql)

    return sql_list


if __name__ == '__main__':
    conn = get_db_conn()
    # sql_list = get(conn, 'dataset', 'data_model_config')
    # sql_list = get(conn, 'dataset', 'data_interface_config')
    sql_list = get(conn, 'dataset', 'data_interface_params')
    for sql in sql_list:
        print(sql)
    conn.close()


