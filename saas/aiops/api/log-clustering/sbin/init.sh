
ROOT=$(cd `dirname $0`; pwd)

$ROOT/mc alias set sw http://${MINIO_ENDPOINT} ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY}
$ROOT/mc cp /app/sreworks-job-resource/venv5-2.2.zip sw/vvp/artifacts/namespaces/default/venv5-2.2.zip
$ROOT/mc cp /app/sreworks-job-resource/flink-ml-uber-2.2-SNAPSHOT.jar sw/vvp/artifacts/namespaces/default/flink-ml-uber-2.2-SNAPSHOT.jar
$ROOT/mc cp /app/sreworks-job-resource/flink-python_2.12-1.15.2.jar sw/vvp/artifacts/namespaces/default/flink-python_2.12-1.15.2.jar




