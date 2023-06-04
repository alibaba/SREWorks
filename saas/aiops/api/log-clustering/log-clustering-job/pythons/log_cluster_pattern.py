input_table = t_env.from_path("log_input")
input_table = input_table.select(
    col('content').alias('sample_raw'),
    col('time_ltz'),
    col('__timestamp__')
)
input_table = input_table.select(col('sample_raw'), col('time_ltz'),
                                 col('__timestamp__'),
                                 gen_sample(col('sample_raw')).alias('sample_tmp'))
input_table = input_table.select(col('sample_raw'), col('time_ltz'),
                                 col('__timestamp__'), col('sample_tmp'),
                                 gen_pattern(col('sample_tmp')).alias('pattern'))

input_table = input_table.select(
    col('pattern').alias('pattern'),
    col('sample_raw').alias('sample_raw'),
    col('sample_tmp').alias('sample_tmp'),
    col('time_ltz'),
    col('__timestamp__')
)
input_table.print_schema()
periodic_read_sql_function_result_element_type = DataTypes.ROW([
    RowField('pattern', DataTypes.STRING()),
    RowField('sample_raw', DataTypes.STRING()),
    RowField('sample_tmp', DataTypes.STRING()),
    RowField('__timestamp__', DataTypes.BIGINT()),
    RowField('gmt_create', DataTypes.BIGINT()),
    RowField('gmt_modified', DataTypes.BIGINT()),
    RowField('config_id', DataTypes.BIGINT()),
    RowField('groupby_label', DataTypes.STRING()),
    RowField('label_id', DataTypes.STRING()),
    RowField('rec_label', DataTypes.STRING()),
    RowField('top_pattern', DataTypes.STRING()),
    RowField('sample', DataTypes.STRING()),
    RowField('keywords', DataTypes.STRING()),
    RowField('isnew', DataTypes.STRING()),
])

periodic_read_sql_function_result_element_name = [
    'pattern',
    'sample_raw',
    'sample_tmp',
    '__timestamp__',
    'gmt_create',
    'gmt_modified',
    'config_id',
    'groupby_label',
    'label_id',
    'rec_label',
    'top_pattern',
    'sample',
    'keywords',
    'isnew',
]


##数据读入算子
periodic_read_sql_function = udaf(
    PeriodicReadSQLFunction(),
    accumulator_type=DataTypes.ARRAY(
        DataTypes.ROW([
            RowField('pattern', DataTypes.STRING()),
            RowField('sample_raw', DataTypes.STRING()),
            RowField('sample_tmp', DataTypes.STRING()),
            RowField('time_ltz', DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE(3)),
            RowField('__timestamp__', DataTypes.BIGINT()),

        ])
    ),
    result_type=DataTypes.ARRAY(periodic_read_sql_function_result_element_type)
)

periodic_read_sql_function_split = udtf(
    Split(),
    result_types=[
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.BIGINT(),
        DataTypes.BIGINT(),
        DataTypes.BIGINT(),
        DataTypes.BIGINT(),
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.STRING(),
        DataTypes.STRING(),
    ]
)

input_data_table = input_table.window(Tumble.over(lit(1).seconds).on(col('time_ltz')).alias("w")) \
    .group_by(col('w')).aggregate(periodic_read_sql_function(col('*'))) \
    .select(col('TMP_0').alias('f0'))

input_data_table = input_data_table.flat_map(periodic_read_sql_function_split(col('f0')))

input_data_table = input_data_table.select(
    *[col('f' + str(i)).alias(periodic_read_sql_function_result_element_name[i]) for i in
      range(len(periodic_read_sql_function_result_element_name))])

input_data_stream = t_env.to_data_stream(input_data_table)
schema = Schema.new_builder() \
    .from_row_data_type(t_env.from_data_stream(input_data_stream).get_schema().to_row_data_type()) \
    .column_by_expression("time_ltz", "TO_TIMESTAMP_LTZ(__timestamp__ * 1000, 3)") \
    .watermark("time_ltz", "time_ltz - INTERVAL '5' SECOND") \
    .build()
input_data_table = t_env.from_data_stream(input_data_stream, schema)
input_data_table.print_schema()


##数据处理&&聚类
build_feature_matrix = udf(BuildFeatureMatrixFunction(top_sen_df, top_sen_relation),
                           result_type=DataTypes.ARRAY(DataTypes.DOUBLE()))
get_keywords = udf(GetKeywords(top_sen_df), result_type=DataTypes.STRING())
t_env.register_function('gen_sample', gen_sample)
t_env.register_table("input_data_table", input_data_table)
t_env.create_temporary_function("build_feature_matrix",
                                udf(BuildFeatureMatrixFunction(top_sen_df, top_sen_relation),
                                    result_type=DataTypes.ARRAY(DataTypes.DOUBLE())))
t_env.create_temporary_function("get_keywords", udf(GetKeywords(top_sen_df), result_type=DataTypes.STRING()))

# 预处理
new_clustering_data = input_data_table.select(col('pattern'), col('sample_raw'), col('sample_tmp'),
                                              col('gmt_create'),
                                              col('gmt_modified'), col('config_id'), col('groupby_label'),
                                              col('label_id'), col('rec_label'), col('top_pattern'),
                                              col('keywords'),
                                              col('isnew'), col('__timestamp__'), col('time_ltz'),
                                              gen_sample(col('sample_tmp')).alias('sample'))

new_clustering_data = new_clustering_data.select(col('pattern'), col('sample_raw'), col('sample_tmp'),
                                                 col('gmt_create'),
                                                 col('gmt_modified'), col('config_id'), col('groupby_label'),
                                                 col('label_id'), col('rec_label'), col('top_pattern'),
                                                 col('sample'),
                                                 col('isnew'), col('__timestamp__'), col('time_ltz'),
                                                 get_keywords(col('sample')).alias('keywords'))

res_sup_table = new_clustering_data.where(col('isnew') == '0')
res_sup_table = res_sup_table.add_columns(null_of(DataTypes.ARRAY(DataTypes.DOUBLE())).alias('featuresArray'))

res_sup_table.print_schema()

new_clustering_data = new_clustering_data.where(col('isnew') != '0')

new_clustering_data = new_clustering_data.select(col('*'),
                                                 build_feature_matrix(col('sample')).alias('featuresArray'))

new_clustering_data.print_schema()
column_names = ['pattern', 'sample_raw', 'sample_tmp', 'gmt_create',
                'gmt_modified', 'config_id', 'groupby_label', 'label_id',
                'rec_label', 'top_pattern', 'sample', 'isnew', '__timestamp__', 'time_ltz',
                'keywords', 'featuresArray']

accumulator_data_type = [DataTypes.STRING() for _ in column_names]
accumulator_data_type[column_names.index('featuresArray')] = DataTypes.ARRAY(DataTypes.DOUBLE())
accumulator_data_type[column_names.index('gmt_create')] = DataTypes.BIGINT()
accumulator_data_type[column_names.index('gmt_modified')] = DataTypes.BIGINT()
accumulator_data_type[column_names.index('config_id')] = DataTypes.BIGINT()
accumulator_data_type[column_names.index('__timestamp__')] = DataTypes.BIGINT()
accumulator_data_type[column_names.index('time_ltz')] = DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE(3)

StandardScaler_feature_result_types = [
    RowField('pattern', DataTypes.STRING()),
    RowField('sample_raw', DataTypes.STRING()),
    RowField('sample_tmp', DataTypes.STRING()),
    RowField('gmt_create', DataTypes.BIGINT()),
    RowField('gmt_modified', DataTypes.BIGINT()),
    RowField('config_id', DataTypes.BIGINT()),
    RowField('groupby_label', DataTypes.STRING()),
    RowField('label_id', DataTypes.STRING()),
    RowField('rec_label', DataTypes.STRING()),
    RowField('top_pattern', DataTypes.STRING()),
    RowField('sample', DataTypes.STRING()),
    RowField('isnew', DataTypes.STRING()),
    RowField('__timestamp__', DataTypes.BIGINT()),
    RowField('time_ltz', DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE(3)),
    RowField('keywords', DataTypes.STRING()),
    RowField('featuresArray', DataTypes.ARRAY(DataTypes.DOUBLE())),
]

standardscaler_feature = udaf(f=StandardScaler_feature(),
                              result_type=DataTypes.ARRAY(DataTypes.ROW(StandardScaler_feature_result_types)),
                              accumulator_type=DataTypes.ROW(
                                  [RowField(column_names[i], accumulator_data_type[i]) for i in
                                   range(len(accumulator_data_type))]))

split_function = udtf(f=Split(), result_types=[x.data_type for x in StandardScaler_feature_result_types])

# table = table.drop_columns(col('time_ltz'),col('__timestamp__'))

clusterinput_table = new_clustering_data.window(Tumble.over(lit(1).seconds).on(col('time_ltz')).alias("w")) \
    .group_by(col('w')).aggregate(standardscaler_feature(col('*'))) \
    .select(col('TMP_0').alias('f0'))

# output_table.print_schema()

# output_table.execute().print()
clusterinput_table = clusterinput_table.flat_map(split_function(col('f0')))

output_column_names = ['pattern', 'sample_raw', 'sample_tmp', 'gmt_create',
                       'gmt_modified', 'config_id', 'groupby_label', 'label_id',
                       'rec_label', 'top_pattern', 'sample', 'isnew', '__timestamp__', 'time_ltz',
                       'keywords', 'featuresArray']
clusterinput_table = clusterinput_table.select(
    *[col('f' + str(i)).alias(output_column_names[i]) for i in range(len(output_column_names))])
clusterinput_table = clusterinput_table.drop_columns(col('time_ltz'))
# output_table.execute().print()

# t_env.register_table('output_table', output_table)
# clusterinput_table.print_schema()
# output_table = t_env.sql_query('SELECT *, TO_TIMESTAMP_LTZ(__timestamp__ * 1000, 3) AS time_ltz FROM output_table')
clusterinput_table_stream = t_env.to_data_stream(clusterinput_table)
schema = Schema.new_builder() \
    .from_row_data_type(t_env.from_data_stream(clusterinput_table_stream).get_schema().to_row_data_type()) \
    .column_by_expression("time_ltz", "TO_TIMESTAMP_LTZ(__timestamp__ * 1000, 3)") \
    .watermark("time_ltz", "time_ltz - INTERVAL '5' SECOND") \
    .build()
clusterinput_table = t_env.from_data_stream(clusterinput_table_stream, schema)

res_sup_table = res_sup_table.drop_columns(col('time_ltz'))

res_sup_table_stream = t_env.to_data_stream(res_sup_table)
schema = Schema.new_builder() \
    .from_row_data_type(t_env.from_data_stream(res_sup_table_stream).get_schema().to_row_data_type()) \
    .column_by_expression("time_ltz", "TO_TIMESTAMP_LTZ(__timestamp__ * 1000, 3)") \
    .watermark("time_ltz", "time_ltz - INTERVAL '5' SECOND") \
    .build()
res_sup_table = t_env.from_data_stream(res_sup_table_stream, schema)
res_sup_table = res_sup_table.add_columns(null_of(DataTypes.INT()).alias('cluster'))
res_sup_table.print_schema()

clusterinput_table = clusterinput_table.select(col('*'), array_to_vector(col('featuresArray')).alias('features'))
clusterinput_table.print_schema()

agglomerative_clustering = AgglomerativeClustering().set_linkage('complete').set_distance_measure(
    'euclidean').set_prediction_col('cluster').set_features_col('features').set_windows(
    EventTimeTumblingWindows.of(Time.milliseconds(1 * 1000))).set_distance_threshold(20.0).set_num_clusters(
    None).set_compute_full_tree(True)

res_table = agglomerative_clustering.transform(clusterinput_table)[0]
# res_table = res_table.select(col('*'), get_toppattern(col('featureArray')))
res_table = res_table.drop_columns(col('features'))
res_table.print_schema()
res_table.union_all(res_sup_table)
res_table.print_schema()
column1_names = ['pattern', 'sample_raw', 'sample_tmp', 'gmt_create',
                 'gmt_modified', 'config_id', 'groupby_label', 'label_id',
                 'rec_label', 'top_pattern', 'sample',
                 'isnew', '__timestamp__', 'keywords', 'featuresArray', 'cluster', 'time_ltz']
accumulator_data_type = [DataTypes.STRING() for _ in column1_names]
accumulator_data_type[column1_names.index('featuresArray')] = DataTypes.ARRAY(DataTypes.DOUBLE())
accumulator_data_type[column1_names.index('cluster')] = DataTypes.INT()
accumulator_data_type[column1_names.index('gmt_create')] = DataTypes.BIGINT()
accumulator_data_type[column1_names.index('gmt_modified')] = DataTypes.BIGINT()
accumulator_data_type[column1_names.index('config_id')] = DataTypes.BIGINT()
accumulator_data_type[column1_names.index('__timestamp__')] = DataTypes.BIGINT()
accumulator_data_type[column1_names.index('time_ltz')] = DataTypes.TIMESTAMP_WITH_LOCAL_TIME_ZONE(3)

post_process_function_result_types = [
    RowField('pattern', DataTypes.STRING()),
    RowField('sample_raw', DataTypes.STRING()),
    RowField('sample_tmp', DataTypes.STRING()),
    RowField('keywords', DataTypes.STRING()),
    RowField('top_pattern', DataTypes.STRING()),
    RowField('label_id', DataTypes.STRING()),
    RowField('rec_label', DataTypes.STRING()),
    RowField('groupby_label', DataTypes.STRING()),
    RowField('gmt_create', DataTypes.BIGINT()),
    RowField('sample', DataTypes.STRING()),
    RowField('gmt_modified', DataTypes.BIGINT()),
    RowField('config_id', DataTypes.BIGINT()),
]
post_process_function = udaf(f=PostProcessFunction(user_defined_pattern=[]),
                             result_type=DataTypes.ARRAY(DataTypes.ROW(post_process_function_result_types)),
                             accumulator_type=DataTypes.ROW(
                                 [RowField(column1_names[i], accumulator_data_type[i]) for i in
                                  range(len(accumulator_data_type))]))

split_function = udtf(f=Split(), result_types=[x.data_type for x in post_process_function_result_types])

logging.info("schema before postprocessfunction")
res_table.print_schema()

res_table = res_table.drop_columns(col('time_ltz'))
res_stream = t_env.to_data_stream(res_table)
schema = Schema.new_builder() \
    .from_row_data_type(t_env.from_data_stream(res_stream).get_schema().to_row_data_type()) \
    .column_by_expression("time_ltz", "TO_TIMESTAMP_LTZ(__timestamp__ * 1000, 3)") \
    .watermark("time_ltz", "time_ltz - INTERVAL '5' SECOND") \
    .build()
res_table = t_env.from_data_stream(res_stream, schema)
logging.info("schema before postprocessfunction")
res_table.print_schema()

output_table = res_table.window(Tumble.over(lit(1).seconds).on(col('time_ltz')).alias("w")) \
    .group_by(col('w')).aggregate(post_process_function(col('*'))) \
    .select(col('TMP_0').alias('f0'))
output_table = output_table.flat_map(split_function(col('f0')))

output_column_names = ['pattern', 'sample_raw', 'sample_tmp', 'keywords', 'top_pattern', 'label_id', 'rec_label',
                       'groupby_label', 'gmt_create', 'sample', 'gmt_modified', 'config_id']
output_table = output_table.select(
    *[col('f' + str(i)).alias(output_column_names[i]) for i in range(len(output_column_names))])

logging.info("schema after postprocessfunction")
output_table.print_schema()

output_table.execute_insert("pattern_output").wait(1000 * 1000 * 1000)