import pandas as pd
import logging
import time
import numpy as np
import copy
import json
import re
import hashlib
import traceback
import time
import datetime
import sys
import os
import mysql.connector
import argparse

sys.path.append('../')
from sklearn.preprocessing import StandardScaler
try:
    from pyflink.ml.clustering.agglomerativeclustering import AgglomerativeClustering
    from pyflink.ml.functions import array_to_vector, vector_to_array
    from pyflink.ml.common.window import EventTimeTumblingWindows
except ImportError:
    from pyflink.ml.lib.clustering.agglomerativeclustering import AgglomerativeClustering
    from pyflink.ml.lib.functions import array_to_vector, vector_to_array
    from pyflink.ml.core.windows import EventTimeTumblingWindows

from pyflink.table.types import DataTypes
from pyflink.table import StreamTableEnvironment, Schema
from pyflink.datastream import KeyedProcessFunction, RuntimeContext, StreamExecutionEnvironment, HashMapStateBackend
from pyflink.common import Types, Row
from pyflink.table.types import RowField
from pyflink.common.time import Time
from pyflink.table.window import Tumble
from pyflink.table.expressions import lit, col, null_of
from pyflink.table.udf import ScalarFunction, udf, udaf, udtf, AggregateFunction, TableFunction

time_interval_in_seconds = 60

parser = argparse.ArgumentParser()
parser.add_argument('--host', type=str, required=True, help='MySQL server hostname')
parser.add_argument('--port', type=int, required=False, default=3306, help='MySQL server port number')
parser.add_argument('--user', type=str, required=True, help='MySQL username')
parser.add_argument('--password', type=str, required=True, help='MySQL password')
parser.add_argument('--database', type=str, required=True, help='MySQL database name')
parser.add_argument('--table', type=str, required=True, help='MySQL table name')

args = parser.parse_args()

DB_HOST = args.host
DB_USER = args.user
DB_PASSWD = args.password
DB_PORT = args.port
DB_NAME = args.database
DB_ORIGIN_DATA_TABLE = args.table

# top_sen_df 和 top_sen_relation 预处理
class TopSenGeneration:

    @staticmethod
    def eval(s):
        try:
            try:
                s = s.encode('utf-8')
            except:
                pass
            hlib = hashlib.md5()
            if len(s) <= 100:
                x = s
                hlib.update(x)
            else:
                s = s.strip()
                try:
                    x = re.sub('\n', '@@', re.sub('\"', '``', re.sub('\'', '`', re.sub('[0-9a-zA-Z]*', '',
                                                                                       re.sub('[ \t]+', '_',
                                                                                              s.decode("utf-8"))))))
                    hlib.update(x.encode("utf-8"))
                except:
                    x = re.sub('\n', '@@', re.sub('\"', '``', re.sub('\'', '`', re.sub('[0-9a-zA-Z]*', '',
                                                                                       re.sub('[ \t]+', '_', s)))))
                    hlib.update(x)
            return hlib.hexdigest()
        except:
            return "GetMd5Exception"


    @staticmethod
    def preprocess_sampletmp(sample_info):
        def gen_sample(x):
            def clear_stop_words(x):
                try:
                    x = x.lower()
                    x = re.sub('[\w\d]{10,}\|', '', x)
                    x = re.sub('...\s*[0-9]*\s*more', '', x)
                    x = re.sub("/\S+/\S+", '', x)
                    x = re.sub("\S+\.\S+\.\S+\.\S+\.\S+\.\S+", '', x)
                    x = re.sub("[`'\*\"\[\]]", ' ', x)
                    x = re.sub("[._:=,();]", ' ', x)
                    x = re.sub("[|]", '\t', x)
                    x = re.sub('[0-9]+', ' ', x)
                    x = re.sub("\s{2,}", '\t', x)
                    x = re.sub("\\\\t", '\t', x)
                    x = re.sub("\n", '\t', x)
                    x = re.sub("\+", '', x)
                    x = re.sub("\!", '', x)
                    x = re.sub("\?", '', x)
                    x = re.sub(":", '', x)
                    x = re.sub("\$", '', x)
                    x = re.sub("\&", '', x)
                    x = re.sub("/", '', x)
                    x = re.sub(">", '', x)
                    x = re.sub("<", '', x)
                    x = re.sub("[\{\}\@]", '', x)
                    x = re.sub('\\\\', '', x)
                except Exception:
                    print(x)
                    pass
                return x

            user_defined = re.findall(r"(keywords:.*keywordsend)", x)
            x = re.sub(r"keywords:.*keywordsend", "", x)

            loginfo = re.sub(r" ODPS-", "\nODPS-", x)
            loginfo = loginfo.split('\n')
            res = ''
            for log in loginfo:
                log = log.strip()
                odps_num = re.findall(r"ODPS-\d+", log)
                log = clear_stop_words(log)
                if odps_num:
                    log = re.sub(r"odps-", odps_num[0], log)
                log = log.strip()
                res = res + log + "\t"
            if len(user_defined) > 0:
                res = res + ' ' + user_defined[0]
            return res

        sample_info = sample_info[~sample_info['sample_tmp'].isnull()]
        sample_info['sample'] = sample_info['sample_tmp'].apply(gen_sample)

        return sample_info

    @staticmethod
    def select_top_freq_sentences(sample_info, min_len=5, top_n=100, tfidf=0.3, min_freq=0):
        sen_info = sample_info['sample'].str.replace('\t', ' ').str.split(' ').apply(pd.Series, 1).stack()
        sen_info.index = sen_info.index.droplevel(-1)
        sen_info = pd.DataFrame(sen_info, columns=['sen'])
        sen_info['sen'] = sen_info['sen'].str.strip()
        sen_info = sen_info[sen_info['sen'] != '']
        sen_info = sen_info.reset_index()
        sen_info.rename(columns={'index': 'sample_index'}, inplace=True)
        sen_info = sen_info.reset_index()
        sen_info['sen'] = sen_info['sen'].values.astype('unicode')
        print(sen_info.dtypes)
        try:
            sen_frequency = sen_info.groupby(['sen'])['sample_index'].nunique().reset_index()
            print("tfidf")
            sen_frequency = sen_frequency[sen_frequency['sample_index'] <= len(sample_info) * tfidf]
        except:
            sen_frequency = sen_info.groupby(['sen'])['sample_index'].count().reset_index()
        sen_frequency = sen_frequency[sen_frequency['sample_index'] >= len(sample_info) * min_freq]
        sen_frequency = sen_frequency.sort_values(by=['sample_index'], ascending=[0])
        sen_frequency['sen_length'] = sen_frequency['sen'].str.len()
        top_sen = sen_frequency[sen_frequency['sen_length'] > int(min_len)][:int(top_n)]['sen'].reset_index(drop=True)
        return top_sen

    @staticmethod
    def get_keywords(df1, top_sentences):
        def g(x):
            tmp_keywords = []
            for i in range(0, len(top_sentences)):
                s = top_sentences[i]
                try:
                    if len(re.findall(s, x)) > 0:
                        tmp_keywords.append(s)
                except Exception:
                    pass
            tmp_keywords_string = ','.join(tmp_keywords)
            return tmp_keywords_string

        df1['keywords'] = df1['sample'].apply(g)
        return df1

    @staticmethod
    def modify_top_sen_by_null_keywords(origin_sample_data, top_sen, extra_top_n, clear_sen=[]):
        null_keywords_sample = origin_sample_data[origin_sample_data['keywords'] == '']
        print("关键词为空的样本数", len(null_keywords_sample))
        if len(null_keywords_sample) > 0:
            extra_top_sen = TopSenGeneration.select_top_freq_sentences(null_keywords_sample, min_len=5, top_n=extra_top_n, tfidf=1,
                                                      min_freq=0)
            print("根据关键词为空的样本提取出的新关键词个数", len(extra_top_sen))
            top_sen = np.append(top_sen, extra_top_sen)
            top_sen = [x for x in top_sen if x not in clear_sen]
            top_sen = list(set(top_sen))
        return top_sen

    @staticmethod
    def get_top_sen_relation(top_sen, extra_top_sen_relation):
        df_1 = pd.DataFrame(top_sen, columns=['sen_1'])
        df_1['tag'] = 1
        df_1['index_1'] = df_1.index

        df_2 = pd.DataFrame(top_sen, columns=['sen_2'])
        df_2['tag'] = 1
        df_2['index_2'] = df_2.index

        sen_df = pd.merge(df_1, df_2, how='outer')

        def cal_top_sen_relation(x):
            return pd.Series({'in': x['sen_1'] in x['sen_2'], 'index_1': x['index_1'], 'index_2': x['index_2']})

        top_sen_relation_part1 = sen_df.apply(cal_top_sen_relation, axis=1)
        top_sen_relation_part1 = top_sen_relation_part1[
            (top_sen_relation_part1['index_1'] != top_sen_relation_part1['index_2']) & (
                    top_sen_relation_part1['in'] == True)]

        def cal_extra_sen_relation(x):
            try:
                index_1 = df_1[df_1['sen_1'] == x['sen_1']].iloc[0]['index_1']
                index_2 = df_1[df_1['sen_1'] == x['sen_2']].iloc[0]['index_1']
                return pd.Series({'in': True, 'index_1': index_1, 'index_2': index_2})
            except Exception:
                return pd.Series({'in': False, 'index_1': 0, 'index_2': 0})

        if len(extra_top_sen_relation) > 0:
            top_sen_relation_part2 = extra_top_sen_relation.apply(cal_extra_sen_relation, axis=1)
            top_sen_relation_part2 = top_sen_relation_part2.drop_duplicates()
            top_sen_relation = pd.concat([top_sen_relation_part1, top_sen_relation_part2], ignore_index=True)
        else:
            top_sen_relation = top_sen_relation_part1

        top_sen_df = pd.DataFrame(top_sen, columns=['top_sen'])
        top_sen_df['index'] = top_sen_df.index
        return top_sen_relation, top_sen_df


cnx = mysql.connector.connect(**{
  'user': DB_USER,
  'password': DB_PASSWD,
  'host': DB_HOST,
  'port': DB_PORT,
  'database': DB_NAME,
  'raise_on_warnings': True
})
origin_sample_data = pd.read_sql("SELECT sample_tmp, sample_raw, pattern, sample FROM " + DB_ORIGIN_DATA_TABLE, con=cnx)
cnx.close()

##预处理
top_sen = TopSenGeneration.select_top_freq_sentences(sample_info=origin_sample_data.copy(), min_len=5, top_n=200)
origin_sample_data = TopSenGeneration.get_keywords(origin_sample_data.copy(), top_sen)
top_sen = TopSenGeneration.modify_top_sen_by_null_keywords(origin_sample_data.copy(), top_sen, extra_top_n=30)
origin_sample_data = origin_sample_data.drop(columns='keywords')
origin_sample_data = TopSenGeneration.get_keywords(origin_sample_data.copy(), top_sen)
top_sen_relation, top_sen_df = TopSenGeneration.get_top_sen_relation(
    top_sen,
    pd.DataFrame({'sen_1':['no partition found'],'sen_2':['partition not found']})
)

del origin_sample_data

# top_sen_df = pd.read_csv('top_sen_df.csv')
# top_sen_relation = pd.read_csv('top_sen_relation.csv')


def upsert_pattern_data_to_db_GmtModified(id, up_data):
    return

@udf(input_types=[DataTypes.STRING()], result_type=DataTypes.STRING())
def gen_pattern(s):
    try:
        try:
            s = s.encode('utf-8')
        except:
            pass
        hlib = hashlib.md5()
        if len(s) <= 100:
            x = s
            hlib.update(x)
        else:
            s = s.strip()
            try:
                x = re.sub('\n', '@@', re.sub('\"', '``', re.sub('\'', '`', re.sub('[0-9a-zA-Z]*', '',
                                                                                   re.sub('[ \t]+', '_',
                                                                                          s.decode("utf-8"))))))
                hlib.update(x.encode("utf-8"))
            except:
                x = re.sub('\n', '@@', re.sub('\"', '``', re.sub('\'', '`', re.sub('[0-9a-zA-Z]*', '',
                                                                                   re.sub('[ \t]+', '_', s)))))
                hlib.update(x)
        return hlib.hexdigest()
    except:
        return "GetMd5Exception"


@udf(input_types=[DataTypes.STRING()], result_type=DataTypes.STRING())
def gen_sampletmp(s):
    s = re.sub("[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}", "uuid", s)
    s = re.sub("ALIYUN\$\w+\@aliyun.com", "", s)
    s = re.sub("\w+-\w+-.*", "", s)
    s = re.sub("\d", "", s)
    s = re.sub('"[\S]*"', '""', s)
    s = re.sub("'[\S]*'", "''", s)
    s = re.sub("\[\S*\]", "[]", s)
    s = re.sub("string", "type", s)
    s = re.sub("bigint", "type", s)
    s = re.sub("float", "type", s)
    s = re.sub("double", "type", s)
    s = re.sub("datetime", "type", s)
    s = re.sub("\S+\/\S+\/\S*", "///", s)
    s = re.sub("\/\S+\/\S*", "///", s)
    s = re.sub("\S+\_\S+\_\S+\_\S*", "___", s)

    s = re.sub('[pP]roject:\s*\w+', 'project', s)
    s = re.sub('[pP]roject\s*\w+_\w+', 'project', s)
    s = re.sub('[cC]olumns:\s*\w+', 'column', s)
    s = re.sub('[cC]olumn:\s*\w+', 'column', s)
    s = re.sub(', sql INSERT INTO.*', ', sql INSERT INTO*', s)
    s = re.sub(', sql REPLACE INTO.*', ', sql INSERT INTO*', s)
    s = re.sub('Runtime error in \[ Topology.*', 'Runtime error in [ Topology', s)
    s = re.sub('GROUP.*ATOM[^:]*', 'GROUP * ATOM *', s)
    s = re.sub("[Cc]olumn[: ]*'[^ ]*'", "column '*'", s)
    s = re.sub('Write ads error, .*', 'Write ads error', s)
    s = re.sub('was [^ ]* milliseconds ago', 'was * milliseconds ago', s)
    s = re.sub('directory\(.*\)', 'directory(*)', s)
    s = re.sub("DB '[^ ]*'", "DB '*'", s)
    s = re.sub('AppName:[^ ]*', 'AppName:*', s)
    s = re.sub('not exist:[ ]*/[^ ]*', 'File does not exist: /*', s)
    s = re.sub('hdfs:/[^ ]*', 'hdfs://*', s)
    s = re.sub('file[: ]*/[^ ]*', 'file /*', s)
    s = re.sub("path '/[^ ]*", "path '/*'", s)
    s = re.sub("FileNotFoundException: /[^ ]*", 'FileNotFoundException : /*', s)
    s = re.sub('Call to [^ ]*.tbsite.net/', 'Call to *.tbsite.net/', s)
    s = re.sub("Table '[^ ]*' ", "Table '*' ", s)
    s = re.sub('NumberFormatException: For input string:.*', 'NumberFormatException: For input string:*', s)
    s = re.sub('finally out:\n.*\n', 'finally out: *\n', s)
    # repace json
    s = re.sub('{".*["]*}', '{...}', s)
    s = re.sub('\[".*\]', '[...]', s)
    s = re.sub('[sSqQLL]:INSERT INTO .*', 'sql template sql:INSERT INTO *', s)
    s = re.sub("Duplicate entry '[^\']*' for key '[^ ]*'", 'Duplicate entry * for key *', s)

    s = re.sub('[^ ]*.aliyuncs.com', '*.aliyuncs.com', s)
    s = re.sub('[^ ]*.tbsite.net', '*.tbsite.net', s)
    s = re.sub('[^ ]*.aliyun.com', '*.aliyun.com', s)
    s = re.sub('Source .* partitions number', 'Source * partitions number', s)
    s = re.sub('[^ ]* partition changed', '* partition changed', s)
    s = re.sub('\nCaused by: .*Could not forward element to next operator\n[\t]*', '', s)
    s = re.sub('... [0-9]* more', '', s)

    s = re.sub("user '[^ ]*'@'[^ ]*'", "user *@*", s)
    s = re.sub("table '[^ ]*'", "table *", s)
    s = re.sub("database '[^ ]*'", "database *", s)
    s = re.sub("Field '[^ ]*'", "Field '*'", s)

    s = re.sub('Failing row contains \(.*', 'Failing row contains (...)', s)
    # for pg null values
    s = re.sub('column "[^ ]*"', 'column "*"', s)

    s = re.sub('task Source:.*', 'task Source: *', s)

    s = re.sub('Partition Info: \[[^ ]*\]', 'Partition Info: [*]', s)
    # namespace=fin_data_market, table=s_tc_biz_order,
    s = re.sub('namespace=[^ ]*', 'namespace=*', s)
    s = re.sub('table=[^ ]*', 'table=*', s)

    # ads only
    s = re.sub('Write ads error[^\t]*ACTION:', 'Write ads error\nACTION', s)
    s = re.sub('InvalidStatementException:\[USER ERROR\][^\t]*',
               'InvalidStatementException:[USER ERROR]\n', s)

    s = re.sub(' Server side Error:{ location:.*', ' Server side Error:{ location:*', s)
    # RDS-dim_product_list can not get connection or connect to server successfuly
    s = re.sub('RDS-[^ ]*', 'RDS-*', s)
    s = re.sub('Request ID[\d\w\:].*\)', '', s)

    s = re.sub('UPSERT INTO.*', 'UPSERT INTO *', s)
    s = re.sub('Invocation =.*', ' Invocation =*', s)
    s = re.sub('TDDL Group [^ ]*', 'TDDL Group *', s)
    s = re.sub(' record:.*', ' record: *', s)
    s = re.sub(' topic\[[^ ]*\] ', ' topic[*] ', s)

    all_trace = re.findall('[\t]*at .*', s)
    s = re.sub('[\t]*at .*\n', '', s)
    s = re.sub(r'stdout F', '', s)
    s = re.sub(r'--T::.\+:', '', s)
    s = re.sub(r'java.lang.', '', s)
    s = re.sub(r'java.io.', '', s)
    s = re.sub(r'java.net.', '', s)
    # s = re.sub(r'[\u4e00-\u9fff]', '', s)
    s = re.sub(r'\*.aliyuncs.com/...,', '', s)
    s = re.sub(r'\*.aliyuncs.com/...\]', '', s)
    s = re.sub(r'\*.aliyuncs.com:', '', s)
    s = re.sub(r'java.util.', '', s)
    s = re.sub(r'java.sql.', '', s)
    s = re.sub(r'(Record is \+I).*', '', s)
    s = re.sub(r'(accessKeyId:).*', '', s)
    s = re.sub(r'(accessKeyId:).*', '', s)
    s = re.sub(r'RequestId=.+,C', 'C', s)
    s = re.sub(r'RequestId=.*, E', 'E', s)
    s = re.sub(r'httpStatus:, requestId:.*, e', 'e', s)
    s = re.sub(r'Read split MySqlSnapshotSplit.*', 'Read split MySqlSnapshotSplit', s)
    s = re.sub(r'Batch entry  delete from "" where.*', 'Batch entry  delete from "" where', s)
    s = re.sub(r'GlobalGroupAggregate.*', 'GlobalGroupAggregate', s)
    s = re.sub(r'for operator Source:.*', 'for operator Source:', s)
    s = re.sub(r'HoloClientWithDetailsException: failed records .*', 'HoloClientWithDetailsException: failed records ',
               s)
    s = re.sub(r'\[\]Batch entry  insert into.*', '', s)
    s = re.sub(r'for operator LookupJoin.*', 'for operator LookupJoin', s)
    s = re.sub(r'BatchUpdateException: Incorrect type value:.*', 'BatchUpdateException: Incorrect type value:', s)
    s = re.sub(r'ClickHouse exception, code:.*', 'ClickHouse exception, code:', s)
    s = re.sub(r'for operator Join.*', 'for operator Join', s)
    s = re.sub(r'for operator Rank.*', 'for operator Rank', s)
    s = re.sub(r'for operator GlobalWindowAggregate.*', 'for operator GlobalWindowAggregate', s)
    s = re.sub(r'failed on final try.*', 'failed on final try', s)
    s = re.sub(r'FATAL: pooler:.*', '[]FATAL: pooler:', s)
    s = re.sub(r'Failed to deserialize JSON.*', 'Failed to deserialize JSON', s)
    s = re.sub(r'AccessKeyId not found:.*', 'AccessKeyId not found:', s)
    s = re.sub(r'params InsertParams.*', 'params InsertParams', s)
    s = re.sub(r'Failed to deserialize CSV row.*', 'Failed to deserialize JSON', s)

    if len(re.findall('ERR_ID:', s)) == 0:
        s += '\n'.join(all_trace[:6])
        s += '\n'.join(all_trace[-6:])
        s = s + '\n'
    s = re.sub(r'~\[\]', '', s)

    all_log = []
    for i in s.split('\n'):
        if i not in all_log and i.strip() != '':
            all_log.append(i)
    s = '\n'.join(all_log)
    return s


class PeriodicReadSQLFunction(AggregateFunction):
    def __init__(self):
        self.db_connection = None
        self.current_db_length = 0
        self.current_patterns = set()
        self.current_top_patterns = set()
        self.current_new_patterns = set()
        self.column_names = ['pattern', 'sample_raw', 'sample_tmp', 'time_ltz', '__timestamp__']
        self.dummy_records = []
        self.window_count = 0
        self.previous_accumulator_results = []
        self.latest_timestamp = 0
        self.previous_timestamp = -1

    def open(self, function_context):
        logging.info('before connecting to database')
        self.db_connection = mysql.connector.connect(
            host=DB_HOST,
            user=DB_USER,
            password=DB_PASSWD,
            database=DB_NAME,
            port=DB_PORT
        )
        logging.info('after connecting to database')

    def create_accumulator(self):
        return []

    def accumulate(self, accumulator, *args):
        self.latest_timestamp = list(args)[self.column_names.index('__timestamp__')]
        accumulator.append(tuple(args))
        return accumulator

    def get_value(self, accumulator):

        if self.previous_timestamp < 0 or self.latest_timestamp >= self.previous_timestamp +time_interval_in_seconds:
            for value in self.previous_accumulator_results:
                accumulator.append(value)
            if self.previous_timestamp < 0:
                self.previous_timestamp = self.latest_timestamp
            else:
                self.previous_timestamp += time_interval_in_seconds
        else:
            for value in accumulator:
                self.previous_accumulator_results.append(value)
            for record in self.dummy_records:
                record['__timestamp__'] = self.latest_timestamp
            return self.dummy_records
        # self.window_count += 1
        # if self.window_count < time_interval_in_seconds:
        #     for value in accumulator:
        #         self.previous_accumulator_results.append(value)
        #     for record in self.dummy_records:
        #         record['__timestamp__'] = self.latest_timestamp
        #     return self.dummy_records
        # else:
        #     self.window_count = 0
        #     for value in self.previous_accumulator_results:
        #         accumulator.append(value)

        logging.info('PeriodicReadSQLFunction2 start ' + str(len(accumulator)))

        if len(accumulator) == 0:
            return []

        cursor = self.db_connection.cursor()
        cursor.execute("USE " + DB_NAME)
        selected_fields = ['pattern', 'sample_raw', 'sample_tmp', 'gmt_create', 'gmt_modified', 'config_id',
                           'groupby_label', 'label_id', 'rec_label', 'top_pattern', 'sample', 'keywords']

        cursor.execute("SELECT " + ','.join(selected_fields) + " FROM " + DB_ORIGIN_DATA_TABLE + " WHERE config_id=0 order by `gmt_create` desc limit 200")
        records = list(cursor.fetchall())

        cursor.close()
        self.current_top_patterns.clear()
        self.current_patterns.clear()
        self.current_new_patterns.clear()
        for record in records:
            self.current_patterns.add(record[selected_fields.index('pattern')])
        logging.info('读入数据长度为：' + str(len(self.current_patterns)))

        result = []
        update_data = []
        # update_data.clear()
        logging.info('时间更新初始长度'+ str(len(update_data)))
        for value in accumulator:
            value = [x for x in value]
            if value[self.column_names.index('pattern')] not in self.current_patterns:
                if value[self.column_names.index('pattern')] not in self.current_new_patterns:
                    self.current_new_patterns.add(value[self.column_names.index('pattern')])
                    result.append(
                        Row(
                            pattern=value[self.column_names.index('pattern')],
                            sample_raw=value[self.column_names.index('sample_raw')],
                            sample_tmp=value[self.column_names.index('sample_tmp')],
                            __timestamp__=self.latest_timestamp,
                            gmt_create=None,
                            gmt_modified=None,
                            config_id=None,
                            groupby_label=None,
                            label_id='-1',
                            rec_label='-1',
                            top_pattern=None,
                            sample=None,
                            keywords=None,
                            isnew='1'
                        )
                    )
                    # logging.info('appending new value ' + str(len(result)))
                else:
                    pass
                    # logging.info('sls old value pass')
            else:
                if value[self.column_names.index('pattern')] not in update_data:
                    update_data.append(value[self.column_names.index('pattern')])

                # 增加写回数据库的逻辑
                # logging.info('更新数据时间')
                pass
        api_data = []
        for data in update_data:
            api_data.append([data,int(time.time())])
        upsert_pattern_data_to_db_GmtModified(1630581657,api_data)
        logging.info('时间更新最终长度'+ str(len(update_data)))
        self.previous_accumulator_results = []
        if len(result) == 0:
            # logging.info('no sls new pattern')
            return []


        for record in records:
            # logging.info(' '.join([x + ' ' + str(type(record[selected_fields.index(x)])) for x in selected_fields]))
            if record[selected_fields.index('groupby_label')] not in self.current_top_patterns:
                self.current_top_patterns.add(record[selected_fields.index('groupby_label')])
                result.append(
                    Row(
                        pattern=record[selected_fields.index('pattern')],
                        sample_raw=record[selected_fields.index('sample_raw')],
                        sample_tmp=record[selected_fields.index('sample_tmp')],
                        __timestamp__=self.latest_timestamp,
                        gmt_create=record[selected_fields.index('gmt_create')],
                        gmt_modified=record[selected_fields.index('gmt_modified')],
                        config_id=record[selected_fields.index('config_id')],
                        groupby_label=record[selected_fields.index('groupby_label')],
                        label_id=record[selected_fields.index('label_id')],
                        rec_label=record[selected_fields.index('rec_label')],
                        top_pattern=record[selected_fields.index('top_pattern')],
                        sample=record[selected_fields.index('sample')],
                        keywords=record[selected_fields.index('keywords')],
                        isnew='2'
                    )
                )
                if len([x for x in self.dummy_records if x['isnew'] == '2']) == 0:
                    self.dummy_records.append(result[-1])
                # logging.info('rds top pattern selected')
            else:
                result.append(
                    Row(
                        pattern=record[selected_fields.index('pattern')],
                        sample_raw=record[selected_fields.index('sample_raw')],
                        sample_tmp=record[selected_fields.index('sample_tmp')],
                        __timestamp__=self.latest_timestamp,
                        gmt_create=record[selected_fields.index('gmt_create')],
                        gmt_modified=record[selected_fields.index('gmt_modified')],
                        config_id=record[selected_fields.index('config_id')],
                        groupby_label=record[selected_fields.index('groupby_label')],
                        label_id=record[selected_fields.index('label_id')],
                        rec_label=record[selected_fields.index('rec_label')],
                        top_pattern=record[selected_fields.index('top_pattern')],
                        sample=record[selected_fields.index('sample')],
                        keywords=record[selected_fields.index('keywords')],
                        isnew='0'
                    )
                )
                if len([x for x in self.dummy_records if x['isnew'] == '0']) == 0:
                    self.dummy_records.append(result[-1])

        self.current_db_length = len(records)
        logging.info('写出数据长度为：' + str(len(self.current_patterns)))
        logging.info('参与聚类rds数据长度'+str(len(self.current_top_patterns)))
        logging.info('参与聚类sls新数据长度:'+ str(len(self.current_new_patterns)))
        logging.info('PeriodicReadSQLFunction2 end ' + str(len(result)))

        return result


@udf(input_types=[DataTypes.STRING()], result_type=DataTypes.STRING())
def gen_sample(x):
    # logging.info('数据进入gen_sample')
    def clear_stop_words(x):
        try:
            # 将所有大写字母转化为小写字母
            x = x.lower()
            # 去除数字与字符串拼接且以|结尾的字符串
            x = re.sub('[\w\d]{10,}\|', '', x)
            # 去除... more
            x = re.sub('...\s*[0-9]*\s*more', '', x)
            # 把路径类型的都去除
            x = re.sub("/\S+/\S+", '', x)
            # 把包含五个点以上的字符串去掉
            x = re.sub("\S+\.\S+\.\S+\.\S+\.\S+\.\S+", '', x)
            # 去除特殊字符,并替换为空格
            x = re.sub("[`'\*\"\[\]]", ' ', x)
            # 去除特殊字符,并替换为空格
            x = re.sub("[._:=,();]", ' ', x)
            # 去除特殊字符,并替换为\t
            x = re.sub("[|]", '\t', x)
            # 去除数字
            x = re.sub('[0-9]+', ' ', x)
            # 把多个空字符换成一个
            x = re.sub("\s{2,}", '\t', x)
            x = re.sub("\\\\t", '\t', x)
            x = re.sub("\n", '\t', x)
            x = re.sub("\+", '', x)
            x = re.sub("\!", '', x)
            x = re.sub("\?", '', x)
            x = re.sub(":", '', x)
            x = re.sub("\$", '', x)
            x = re.sub("\&", '', x)
            x = re.sub("/", '', x)
            x = re.sub(">", '', x)
            x = re.sub("<", '', x)
            x = re.sub("[\{\}\@]", '', x)
            x = re.sub('\\\\', '', x)
        except Exception:
            print(x)
            pass
        return x

    # 将人工自定义的关键词补充到sample的末尾，使得提取关键词的时候能被提取出来
    user_defined = re.findall(r"(keywords:.*keywordsend)", x)
    x = re.sub(r"keywords:.*keywordsend", "", x)

    loginfo = re.sub(r" ODPS-", "\nODPS-", x)
    loginfo = loginfo.split('\n')
    res = ''
    for log in loginfo:
        log = log.strip()
        odps_num = re.findall(r"ODPS-\d+", log)
        log = clear_stop_words(log)
        if odps_num:
            log = re.sub(r"odps-", odps_num[0], log)
        log = log.strip()
        res = res + log + "\t"
    if len(user_defined) > 0:
        res = res + ' ' + user_defined[0]
    # logging.info('数据流出gen_sample')
    return res


class GetKeywords(ScalarFunction):
    def __init__(self, top_sen_df):
        self.top_sen_df = top_sen_df

    def eval(self, x):
        # logging.info('数据进入GetKeywords')
        top_sentences = top_sen_df['top_sen'].unique().tolist()
        tmp_keywords = []
        for i in range(0, len(top_sentences)):
            s = top_sentences[i]
            try:
                if len(re.findall(s, x)) > 0:
                    tmp_keywords.append(s)
            except Exception:
                pass
        tmp_keywords_string = ','.join(tmp_keywords)
        # logging.info('数据流出GetKeywords')
        return tmp_keywords_string


get_keywords = udf(GetKeywords(top_sen_df), result_type=DataTypes.STRING())


class BuildFeatureMatrixFunction(ScalarFunction):
    def __init__(self, top_sen_df, top_sen_relation):
        self.top_sen_df = top_sen_df
        self.top_sen_relation = top_sen_relation

    def eval(self, x):
        # logging.info('数据进入BuildFeatures')
        clustering_metrics = []
        feature_raw = []
        feature_matrix = []

        for i in range(0, len(top_sen_df)):
            s = top_sen_df.iloc[i]['top_sen']
            index = top_sen_df.iloc[i]['index']
            res = 0
            try:
                if len(re.findall(s, x)) > 0:
                    res = 1
            except Exception:
                pass
            feature_raw.append(res)
            clustering_metrics.append(index)
        #         feature_matrix = np.array(feature_matrix)
        for i in range(len(top_sen_relation)):
            short_index = top_sen_relation.iloc[i]['index_1']
            long_index = top_sen_relation.iloc[i]['index_2']
            if feature_raw[short_index] > 0:
                feature_raw[long_index] = 1
            if short_index in clustering_metrics:
                clustering_metrics.remove(short_index)

        for f_index in clustering_metrics:
            feature_matrix.append(feature_raw[f_index])
        # logging.info('数据流出BuildFeatures')
        return feature_matrix


class StandardScaler_feature(AggregateFunction):
    def create_accumulator(self):
        return []

    def accumulate(self, accumulator, *args):
        accumulator.append(tuple(args))
        return accumulator

    def get_value(self, accumulator):

        flags = False
        accumulator = [list(x) for x in accumulator]
        if len(accumulator) == 0:
            print("无数据")
            return []
        features = [list(x[-1]) for x in accumulator]
        if len(features) > 2:
            print('数据进入StandarScaler')
            flags = True

        X = StandardScaler().fit_transform(features)
        X = X.tolist()
        for i in range(len(X)):
            accumulator[i].pop(-1)
            accumulator[i].append(X[i])
            # print(accumulator[i][-1])
            # print(len(accumulator[i]))
        #                 print(X[i])
        #         for data in accumulator:
        #             for x in X:
        #                 print(data[-1])
        #                 data.pop(-1)
        #                 data.append(x)
        #                 print(x)
        if flags:
            print('数据流出StandarScaler')

        return [Row(*x) for x in accumulator]


class Split(TableFunction):
    def eval(self, value_list):
        for value in value_list:
            yield value

def get_top_pattern_for_nlp_online_clustering(online_clustering_info, dws_new_pattern_info,
                                              dws_top_pattern_info):
    # print("len(dws_new_pattern_info):", len(dws_new_pattern_info))
    # print("len(online_clustering_info):", len(online_clustering_info))
    # 先处理那些不包含top_sen的sample
    # print("情形一:先处理那些不包含top_sen的new sample")
    # 检查不包含上述所有特征的outlier样本有多少
    # online_clustering_info['feature_sum'] = online_clustering_info[clustering_metrics].sum(axis=1)
    online_clustering_info['feature_sum'] = online_clustering_info['featuresArray'].apply(sumf)
    online_outlier_sample = online_clustering_info[online_clustering_info['feature_sum'] == 0]
    # print("不包含上述所有特征的样本有:", len(online_outlier_sample))
    # 从新的待分类样本中，找到这些不包含top sen的样本，独立为top_pattern
    online_new_top_pattern_1 = pd.merge(
        online_outlier_sample[['pattern', 'sample', 'sample_raw', 'sample_tmp', 'keywords']],
        dws_new_pattern_info[['pattern']],
        on=['pattern'])
    if len(online_new_top_pattern_1) > 0:
        # 没有提取到特征的sample属于哪个簇
        online_outlier_cluster = online_outlier_sample['cluster'].values[0]
        # print("没有提取到特征的sample属于哪个簇:", online_outlier_cluster)
        online_new_top_pattern_1['sample'] = online_new_top_pattern_1['sample'].str.replace('\t', ' ')
        online_new_top_pattern_1['top_pattern'] = online_new_top_pattern_1['pattern']
    # print("情形一结果个数:", len(online_new_top_pattern_1))
    # print(online_new_top_pattern_1.columns)
    #
    # print("情形二:从其他正常聚类的簇中，找到老样本作为top_pattern")
    # 从其他正常聚类的簇中，找到label_id不为空的老样本作为top_pattern
    try:
        # exclude_outlier_clustering_info = online_clustering_info[
        #     online_clustering_info['cluster'] != online_outlier_cluster]
        exclude_outlier_clustering_info = online_clustering_info[
            ~online_clustering_info['pattern'].isin(list(online_new_top_pattern_1['pattern'].unique()))]
    except Exception:
        exclude_outlier_clustering_info = online_clustering_info
    # 找到新样本
    exclude_outlier_new_sample = pd.merge(exclude_outlier_clustering_info, dws_new_pattern_info[['pattern']])[
        ['pattern', 'sample', 'cluster', 'keywords', 'sample_raw', 'sample_tmp']]
    # 找到正常聚类的老样本

    exclude_outlier_old_sample = \
        pd.merge(exclude_outlier_clustering_info, dws_top_pattern_info[['pattern', 'label_id']], on=['pattern'])

    exclude_outlier_old_sample = exclude_outlier_old_sample[['pattern', 'cluster', 'label_id']]
    # 排序，使得后面尽可能保留label_id不为-1的记录
    exclude_outlier_old_sample = exclude_outlier_old_sample.sort_values(by=['label_id'], ascending=[0])
    # 为每一个cluster，选定一个老样本作为top_pattern
    cluster_info_old_sample = exclude_outlier_old_sample.drop_duplicates(['cluster'])[['cluster', 'pattern']]
    cluster_info_old_sample.rename(columns={'pattern': 'top_pattern'},
                                   inplace=True)
    # 将正常聚类的新样本与老样本的cluster信息join，以cluster为key
    online_new_sample_old_top_pattern = pd.merge(exclude_outlier_new_sample, cluster_info_old_sample,
                                                 on=['cluster'])
    # print("情形二结果个数", len(online_new_sample_old_top_pattern))
    # print(online_new_sample_old_top_pattern.columns)
    #
    # print("情形三:正常聚类的新样本对应的cluster没有老sample")
    # 如果正常聚类的新样本对应的cluster没有老sample，那边就随机选择一个作为top pattern
    online_new_top_pattern_2 = pd.concat(
        [online_new_sample_old_top_pattern[['pattern', 'sample', 'cluster', 'keywords', 'sample_raw', 'sample_tmp']],
         exclude_outlier_new_sample]).drop_duplicates(keep=False)
    if len(online_new_top_pattern_2) > 0:
        online_new_top_pattern_2['sample'] = online_new_top_pattern_2['sample'].str.replace('\t', ' ')
        online_cluster = online_new_top_pattern_2.sort_values(by=['cluster'], ascending=[1])
        online_top_pattern = online_cluster.drop_duplicates(['cluster'])
        online_top_pattern = online_top_pattern[['pattern', 'cluster']]
        online_top_pattern = online_top_pattern.rename(columns={'pattern': 'top_pattern'})
        online_new_top_pattern_2 = pd.merge(online_top_pattern,
                                            online_new_top_pattern_2[
                                                ['cluster', 'sample', 'pattern', 'keywords', 'sample_raw',
                                                 'sample_tmp']],
                                            on=['cluster'])
    # print("情形三结果", len(online_new_top_pattern_2))
    # print(online_new_top_pattern_2.columns)

    # 将上述三部分结果拼接起来
    selected_columns = ['pattern', 'sample', 'keywords', 'sample_raw', 'top_pattern', 'sample_tmp']
    if len(online_new_top_pattern_1) > 0:
        online_result = pd.concat(
            [online_new_top_pattern_1[selected_columns], online_new_sample_old_top_pattern[selected_columns]])
    else:
        online_result = online_new_sample_old_top_pattern[selected_columns]

    if len(online_new_top_pattern_2) > 0:
        online_result = pd.concat([online_result, online_new_top_pattern_2[selected_columns]])
    online_result = online_result.reset_index(drop=True)

    # print("len(online_result)", len(online_result))
    return online_result


def update_top_pattern_based_on_user_defined_pattern(final_pattern_info, user_defined_pattern, origin_db_data=[]):
    # print("根据user defined pattern合并之前的top_pattern数", len(final_pattern_info['top_pattern'].unique()))
    if len(user_defined_pattern) == 0:
        # print("没有用户自定义的模式")
        return final_pattern_info

    # 最后针对用户自定义的pattern模式再进行一次聚合
    for each_user_defined_pattern_str in user_defined_pattern:
        # 针对用户定义的每一个模式都分别处理
        each_user_defined_pattern_list = each_user_defined_pattern_str.split(",")
        temp_selected_final_pattern_info = final_pattern_info.copy()
        if len(origin_db_data) > 0:
            temp_origin_db_data = origin_db_data.copy()
        else:
            temp_origin_db_data = []
        for each_user_defined_pattern in each_user_defined_pattern_list:
            # 用户定义的模式中可能包含多个关键词,每个关键词都必须包含
            temp_selected_final_pattern_info = temp_selected_final_pattern_info[
                temp_selected_final_pattern_info['keywords'].str.contains(each_user_defined_pattern)]
            if len(origin_db_data) > 0:
                temp_origin_db_data['keywords'] = temp_origin_db_data['keywords'].fillna('')
                temp_origin_db_data = temp_origin_db_data[
                    temp_origin_db_data['keywords'].str.contains(each_user_defined_pattern)]
                temp_origin_db_data_with_label = temp_origin_db_data[temp_origin_db_data['label_id'] != -1]
            else:
                temp_origin_db_data_with_label = []
        # 对于包含这一模式的所有patterm, 统一top_pattern为其中一个
        if len(temp_selected_final_pattern_info) > 0:
            # 如果数据库中包含人工定义过的样本，且人工打过标，那边就直接用数据库中的top_pattern作为新样本的top_pattern
            if len(temp_origin_db_data_with_label) > 0:
                temp_selected_final_pattern_info['top_pattern'] = temp_origin_db_data_with_label.iloc[0][
                    'top_pattern']
            # 如果数据库中包含人工定义过的样本，那边就直接用数据库中的top_pattern作为新样本的top_pattern
            elif len(temp_origin_db_data) > 0:
                temp_selected_final_pattern_info['top_pattern'] = temp_origin_db_data.iloc[0][
                    'top_pattern']
            # 否则就直接用新样本中的某一个pattern作为大家的top_pattern
            else:
                temp_selected_final_pattern_info['top_pattern'] = temp_selected_final_pattern_info.iloc[0]['pattern']
            temp_selected_final_pattern_info['keywords'] = each_user_defined_pattern_str
            temp_selected_final_pattern_info.rename(
                columns={'top_pattern': 'selected_top_pattern', 'keywords': 'new_keywords'}, inplace=True)
            # 更新final_pattern_info中对应的pattern的值
            final_pattern_info = pd.merge(final_pattern_info,
                                          temp_selected_final_pattern_info[
                                              ['pattern', 'selected_top_pattern', 'new_keywords']],
                                          on=['pattern'], how='left')
            final_pattern_info = final_pattern_info.fillna('')
            final_pattern_info.loc[final_pattern_info['selected_top_pattern'] != '', 'top_pattern'] = \
                final_pattern_info['selected_top_pattern']
            final_pattern_info.loc[final_pattern_info['selected_top_pattern'] != '', 'keywords'] = \
                final_pattern_info['new_keywords']
            final_pattern_info = final_pattern_info.drop('selected_top_pattern', axis=1)
            final_pattern_info = final_pattern_info.drop('new_keywords', axis=1)
    final_pattern_info = final_pattern_info.drop_duplicates(['pattern'])
    # print("根据user defined pattern合并之后的top_pattern数", len(final_pattern_info['top_pattern'].unique()))
    return final_pattern_info


def merge_online_result_with_db_data(final_pattern_info, origin_db_data):
    """
    根据数据库里已经有的数据，以及新的聚类结果，更新label_id, rec_label, groupby_label等字段
    :param final_pattern_info:
    :param origin_db_data:
    :return:
    """
    # 所有新的pattern，label_id都没有经过人工确认，所以全部为-1
    final_pattern_info['label_id'] = -1
    # 将新pattern的top_pattern, 去和数据库的pattern进行join，如果join上了， 则把rec_label置为与它的label_id, label_id为空的话则置为与它的rec_label相同的，否则置为-1
    # 最后处理groupby_label字段，去和数据库的pattern进行join，如果join上了，则把groupby_label置为和它一样的，否则置为自己的top_pattern
    final_pattern_info.rename(columns={'top_pattern': 'cls_top_pattern'}, inplace=True)
    origin_db_data.rename(columns={
        'pattern': 'cls_top_pattern', 'top_pattern': 'origin_top_pattern',
        'rec_label': 'origin_rec_label', 'groupby_label': 'origin_groupby_label', 'label_id': 'origin_label_id'},
        inplace=True)
    # print(len(pd.merge(final_pattern_info, origin_db_data[['cls_top_pattern', 'origin_rec_label', 'origin_groupby_label']], on=['cls_top_pattern'])))
    final_pattern_info = pd.merge(
        final_pattern_info,
        origin_db_data[['cls_top_pattern', 'origin_rec_label', 'origin_groupby_label', 'origin_label_id']],
        on=['cls_top_pattern'], how='left')
    final_pattern_info['origin_rec_label'] = final_pattern_info['origin_rec_label'].fillna(-1)
    final_pattern_info['origin_groupby_label'] = final_pattern_info['origin_groupby_label'].fillna(-1)
    # 先将rec_label设置为top_pattern的label_id
    final_pattern_info['rec_label'] = final_pattern_info['origin_label_id']
    # 如果这个模式没有人工标注，则置为和rec_label一样
    final_pattern_info.loc[final_pattern_info['rec_label'] == -1, 'rec_label'] = final_pattern_info['origin_rec_label']
    # 如果是-2，则先置为-1
    final_pattern_info.loc[final_pattern_info['rec_label'] == -2, 'rec_label'] = -1
    # groupby label 就置为和top pattern的groupby label一样
    final_pattern_info['groupby_label'] = final_pattern_info['origin_groupby_label']
    # print(len(final_pattern_info[final_pattern_info['groupby_label']== -1]))
    final_pattern_info.loc[
        (final_pattern_info['groupby_label'] == -1) | (final_pattern_info['groupby_label'] == -2), 'groupby_label'] = \
    final_pattern_info['cls_top_pattern']
    final_pattern_info.rename(columns={'cls_top_pattern': 'top_pattern'}, inplace=True)
    final_pattern_info['gmt_create'] = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))
    final_pattern_info = final_pattern_info[
        ['pattern', 'sample_raw', 'sample_tmp', 'keywords', 'top_pattern', 'label_id', 'rec_label', 'groupby_label',
         'gmt_create', 'sample']]
    return final_pattern_info


class PostProcessFunction(AggregateFunction):
    def __init__(self, user_defined_pattern):
        self.user_defined_pattern = user_defined_pattern

    def create_accumulator(self):
        return []

    def accumulate(self, accumulator, *args):
        # logging.info('累积数据：' + str(list(args)[:]))
        accumulator.append(tuple(args))
        return accumulator

    def get_value(self, accumulator):
        flag = False
        accumulator = [list(x) for x in accumulator]
        if len(accumulator)>2:
            logging.info('数据进入post')
            flag = True
        online_clustering_info = pd.DataFrame([x for x in accumulator if x[column1_names.index('isnew')] != '0'],
                                              columns=column1_names)
        online_clustering_info['label_id'] = online_clustering_info['label_id'].fillna('-1')
        online_clustering_info['rec_label'] = online_clustering_info['rec_label'].fillna('-1')
        online_clustering_info['label_id'] = online_clustering_info['label_id'].astype(int)
        online_clustering_info['rec_label'] = online_clustering_info['rec_label'].astype(int)

        online_clustering_info = online_clustering_info.drop('label_id', axis=1)

        dws_new_pattern_info = pd.DataFrame([x for x in accumulator if x[column1_names.index('isnew')] == '1'],
                                            columns=column1_names)
        dws_new_pattern_info = dws_new_pattern_info.drop(['gmt_create', 'gmt_modified', 'config_id', 'groupby_label',
                                                          'label_id', 'rec_label', 'top_pattern', 'sample', 'keywords',
                                                          'isnew', 'featuresArray', 'cluster', '__timestamp__',
                                                          'time_ltz'], axis=1)
        dws_top_pattern_info = pd.DataFrame([x for x in accumulator if x[column1_names.index('isnew')] == '2'],
                                            columns=column1_names)
        dws_top_pattern_info['label_id'] = dws_top_pattern_info['label_id'].astype(int)
        dws_top_pattern_info['rec_label'] = dws_top_pattern_info['rec_label'].astype(int)

        dws_top_pattern_info = dws_top_pattern_info.drop(['gmt_create', 'gmt_modified', 'config_id', 'groupby_label',
                                                          'rec_label', 'top_pattern',
                                                          'isnew', 'featuresArray', 'cluster', '__timestamp__',
                                                          'time_ltz'], axis=1)
        user_defined_pattern = self.user_defined_pattern
        # origin_db_data = pd.DataFrame([], columns=['pattern', 'top_pattern', 'rec_label', 'groupby_label', 'label_id'], dtype='object')
        origin_db_data = pd.DataFrame([x for x in accumulator if x[column1_names.index('isnew')] != '1'],
                                      columns=column1_names)
        origin_db_data['label_id'] = origin_db_data['label_id'].astype(int)
        origin_db_data['rec_label'] = origin_db_data['rec_label'].astype(int)
        origin_db_data = pd.DataFrame(origin_db_data,
                                      columns=['pattern', 'top_pattern', 'rec_label', 'groupby_label', 'label_id'],
                                      dtype='object')
        # column1_names = ['pattern', 'sample_raw', 'sample_tmp', 'gmt_create',
        #                  'gmt_modified', 'config_id', 'groupby_label', 'label_id',
        #                  'rec_label', 'top_pattern', 'sample', 'keywords',
        #                  'isnew', 'cluster', '__timestamp__', 'time_ltz']

        online_top_pattern_result = get_top_pattern_for_nlp_online_clustering(online_clustering_info,
                                                                              dws_new_pattern_info,
                                                                              dws_top_pattern_info)
        online_top_pattern_result = update_top_pattern_based_on_user_defined_pattern(online_top_pattern_result.copy(),
                                                                                     user_defined_pattern)

        online_top_pattern_result = merge_online_result_with_db_data(
            final_pattern_info=online_top_pattern_result.copy(),
            origin_db_data=origin_db_data)

        online_top_pattern_result['gmt_create'] = int(time.time())
        online_top_pattern_result['label_id'] = online_top_pattern_result['label_id'].astype(str)
        online_top_pattern_result['rec_label'] = online_top_pattern_result['rec_label'].fillna(-1)
        online_top_pattern_result['rec_label'] = online_top_pattern_result['rec_label'].astype(str)
        online_top_pattern_result['gmt_modified'] = int(time.time())
        online_top_pattern_result['config_id'] = 0
        online_top_pattern_result = online_top_pattern_result.drop_duplicates(['pattern'])
        if flag:
            logging.info('数据流出post')
        return [Row(*x) for x in online_top_pattern_result.values.tolist()]


def sumf(s):
    return np.sum(s)

if __name__ == '__main__':
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(1)
    env.disable_operator_chaining()
    env.get_config().set_auto_watermark_interval(100)
    t_env = StreamTableEnvironment.create(env)

    
    # SOURCE_BLOCK #

    # SINK_BLOCK #

    # EXEC_BLOCK #


