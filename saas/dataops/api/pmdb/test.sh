# shellcheck disable=SC2086

set -e
set -x
mvn -f pom.xml -Dmaven.test.skip=true clean package

kubeconfig="/Users/fangzong.lyj/.kube/config-dailyrun"

pod=$(kubectl -n sreworks-dataops --kubeconfig=${kubeconfig} get pod | grep prod-dataops-pmdb | grep Running | awk '{print $1}' | sed -n '1p')
kubectl -n sreworks-dataops --kubeconfig=${kubeconfig} exec -ti ${pod} -- rm -f /app/pmdb-start.jar
time kubectl -n sreworks-dataops --kubeconfig=${kubeconfig} cp pmdb-start/target/pmdb-start.jar ${pod}:/app/
kubectl -n sreworks-dataops --kubeconfig=${kubeconfig} exec -ti ${pod} -- java -Xmx1g -Xms1g -XX:ActiveProcessorCount=2 -jar /app/pmdb-start.jar
