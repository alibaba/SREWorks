# shellcheck disable=SC2086

set -e
set -x
mvn -f pom.xml -Dmaven.test.skip=true clean package

kubeconfig="/Users/fangzong.lyj/.kube/config-dailyrun"

pod=$(kubectl -n sreworks --kubeconfig=${kubeconfig} get pod | grep prod-health-health | grep Running | awk '{print $1}' | sed -n '1p')
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- rm -f /app/health-start.jar
time kubectl -n sreworks --kubeconfig=${kubeconfig} cp health-start/target/health-start.jar ${pod}:/app/
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- java -Xmx1g -Xms1g -XX:ActiveProcessorCount=2 -jar /app/health-start.jar
