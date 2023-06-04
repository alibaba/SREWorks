t_env.from_path('my_source').select(
    col('a'),
    col("b"),
    col("c")
).execute_insert('my_sink').print()