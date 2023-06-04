import re
import hashlib
import traceback
import pandas as pd
import numpy as np
import time
import argparse
from sqlalchemy import create_engine
from sqlalchemy import inspect
from sqlalchemy import text

# from sqlalchemy.engine.reflection import Inspector

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


def modify_top_sen_by_null_keywords(origin_sample_data, top_sen, extra_top_n, clear_sen=[]):
    null_keywords_sample = origin_sample_data[origin_sample_data['keywords'] == '']
    print("关键词为空的样本数", len(null_keywords_sample))
    if len(null_keywords_sample) > 0:
        extra_top_sen = select_top_freq_sentences(null_keywords_sample, min_len=5, top_n=extra_top_n, tfidf=1,
                                                  min_freq=0)
        print("根据关键词为空的样本提取出的新关键词个数", len(extra_top_sen))
        top_sen = np.append(top_sen, extra_top_sen)
        top_sen = [x for x in top_sen if x not in clear_sen]
        top_sen = list(set(top_sen))
    return top_sen


# def get_top_sen_relation(top_sen, extra_top_sen_relation):
#     df_1 = pd.DataFrame(top_sen, columns=['sen_1'])
#     df_1['tag'] = 1
#     df_1['index_1'] = df_1.index
#
#     df_2 = pd.DataFrame(top_sen, columns=['sen_2'])
#     df_2['tag'] = 1
#     df_2['index_2'] = df_2.index
#
#     sen_df = pd.merge(df_1, df_2, how='outer')
#
#     def cal_top_sen_relation(x):
#         return pd.Series({'in': x['sen_1'] in x['sen_2'], 'index_1': x['index_1'], 'index_2': x['index_2']})
#
#     top_sen_relation_part1 = sen_df.apply(cal_top_sen_relation, axis=1)
#     top_sen_relation_part1 = top_sen_relation_part1[
#         (top_sen_relation_part1['index_1'] != top_sen_relation_part1['index_2']) & (
#                     top_sen_relation_part1['in'] == True)]
#
#     def cal_extra_sen_relation(x):
#         try:
#             index_1 = df_1[df_1['sen_1'] == x['sen_1']].iloc[0]['index_1']
#             index_2 = df_1[df_1['sen_1'] == x['sen_2']].iloc[0]['index_1']
#             return pd.Series({'in': True, 'index_1': index_1, 'index_2': index_2})
#         except Exception:
#             return pd.Series({'in': False, 'index_1': 0, 'index_2': 0})
#
#     if len(extra_top_sen_relation) > 0:
#         top_sen_relation_part2 = extra_top_sen_relation.apply(cal_extra_sen_relation, axis=1)
#         top_sen_relation_part2 = top_sen_relation_part2.drop_duplicates()
#         top_sen_relation = pd.concat([top_sen_relation_part1, top_sen_relation_part2], ignore_index=True)
#     else:
#         top_sen_relation = top_sen_relation_part1
#
#     top_sen_df = pd.DataFrame(top_sen, columns=['top_sen'])
#     top_sen_df['index'] = top_sen_df.index
#     return top_sen_relation, top_sen_df

if __name__ == '__main__':
    ##将读入数据处理成list
    description = "This is a data initialization script for the log clustering service. \n"
    description += "It inserts the sample log into the database as initial data.\n"
    description += "Please provide the sample log and database connection information."

    parser = argparse.ArgumentParser(
        description=description,
        epilog='Example: python db-init.py /path/to/sample.log --host mysql.example.com --port 3306 --user myuser --password mypass --database mydb --table mytable'
    )
    parser.add_argument('log_path', type=str, help='path to local sample log file')
    parser.add_argument('--host', type=str, required=True, help='MySQL server hostname')
    parser.add_argument('--port', type=int, required=False, default=3306, help='MySQL server port number')
    parser.add_argument('--user', type=str, required=True, help='MySQL username')
    parser.add_argument('--password', type=str, required=True, help='MySQL password')
    parser.add_argument('--database', type=str, required=True, help='MySQL database name')
    parser.add_argument('--table', type=str, required=True, help='MySQL table name')

    args = parser.parse_args(None)

    fp = open(args.log_path)
    db_data = []
    for line in fp.readlines():
        db_data.append(line.strip())

    ##预处理
    extra_top_sen_relation = pd.DataFrame({'sen_1':['no partition found'],'sen_2':['partition not found']})
    origin_sample_data=pd.DataFrame(db_data,columns=['sample_tmp'])
    origin_sample_data['sample_raw'] = origin_sample_data['sample_tmp']
    origin_sample_data['pattern'] = origin_sample_data['sample_tmp'].apply(eval)

    origin_sample_data = preprocess_sampletmp(sample_info=origin_sample_data.copy())
    top_sen = select_top_freq_sentences(sample_info=origin_sample_data.copy(), min_len=5, top_n=200)
    origin_sample_data = get_keywords(origin_sample_data.copy(), top_sen)
    top_sen = modify_top_sen_by_null_keywords(origin_sample_data.copy(), top_sen, extra_top_n=30)
    origin_sample_data = origin_sample_data.drop(columns='keywords')
    origin_sample_data = get_keywords(origin_sample_data.copy(), top_sen)

    # top_sen_relation, top_sen_df =get_top_sen_relation(top_sen, extra_top_sen_relation)
    # top_sen_relation.to_csv('top_sen_relation.csv',index =False)
    # top_sen_df.to_csv('top_sen_df.csv',index = False)

    # origin_sample_data = origin_sample_data.drop(columns=['sample','keywords'])
    origin_sample_data = origin_sample_data[['pattern', 'sample_raw', 'sample_tmp','sample', 'keywords']]
    origin_sample_data['gmt_create'] = int(time.time())
    origin_sample_data['gmt_modified'] = int(time.time())
    origin_sample_data['config_id'] = 0
    origin_sample_data['label_id'] = -1
    origin_sample_data['rec_label'] = -1
    origin_sample_data = origin_sample_data.assign(top_pattern=origin_sample_data['pattern'])
    origin_sample_data = origin_sample_data.assign(groupby_label=origin_sample_data['pattern'])

    # origin_sample_data.to_csv('origin_sample_data.csv',index=False,header=None)
    origin_sample_data = origin_sample_data.drop_duplicates(subset=['pattern'])
    engine = create_engine(f"mysql+mysqldb://{args.user}:{args.password}@{args.host}:{args.port}/{args.database}")

    create_table_sql = f"""CREATE TABLE IF NOT EXISTS `{args.table}` (
  `gmt_create` bigint(20) DEFAULT NULL,
  `gmt_modified` bigint(20) DEFAULT NULL,
  `pattern` varchar(32) NOT NULL COMMENT '模式的md5',
  `config_id` bigint(20) NOT NULL,
  `groupby_label` varchar(255) DEFAULT NULL COMMENT '页面上进行聚合的字段',
  `log_num` bigint(20) DEFAULT '0',
  `label_id` varchar(128) DEFAULT '-1' COMMENT '用户标注的标签id',
  `rec_label` varchar(128) DEFAULT NULL COMMENT '算法推荐的标签id',
  `top_pattern` varchar(255) DEFAULT NULL COMMENT '算法聚出来的top_pattern',
  `sample_raw` text COMMENT '与该pattern对应的sample_raw',
  `sample` text COMMENT '预处理后用于聚类的样本',
  `sample_tmp` text COMMENT '备用',
  `keywords` text COMMENT '算法提取的关键词',
  `modifier` varchar(100) DEFAULT NULL,
  `primary_dim_num` int(11) DEFAULT '0',
  UNIQUE KEY `pattern_config_id` (`config_id`,`pattern`) USING BTREE,
  KEY `pattern` (`pattern`) USING BTREE,
  KEY `gmt_create` (`gmt_create`) USING BTREE,
  KEY `gmt_modified` (`gmt_modified`) USING BTREE,
  KEY `groupby_label` (`groupby_label`) USING BTREE,
  KEY `log_num` (`log_num`) USING BTREE,
  KEY `label_id` (`label_id`) USING BTREE,
  KEY `rec_label` (`rec_label`) USING BTREE,
  KEY `top_pattern` (`top_pattern`) USING BTREE,
  KEY `modifier` (`modifier`) USING BTREE,
  FULLTEXT KEY `sample_raw` (`sample_raw`),
  FULLTEXT KEY `sample` (`sample`),
  FULLTEXT KEY `sample_tmp` (`sample_tmp`),
  FULLTEXT KEY `keywords` (`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
"""

    if not inspect(engine).has_table(args.table):
        with engine.connect() as conn:
            conn.execute(text(create_table_sql))

    origin_sample_data.to_sql(name=args.table, con=engine, if_exists='append', index=False)



