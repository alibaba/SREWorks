from pyflink.common.typeinfo import Types
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment
from pyflink.common import Row
from pyflink.table.expressions import col


env = StreamExecutionEnvironment.get_execution_environment()
t_env = StreamTableEnvironment.create(stream_execution_environment=env)

if __name__ == '__main__':

    # SOURCE_BLOCK #

    # SINK_BLOCK #

    # EXEC_BLOCK #
