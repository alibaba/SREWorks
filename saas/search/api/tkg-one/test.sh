# shellcheck disable=SC2086

set -e
set -x
mvn -f pom.xml -Dmaven.test.skip=true clean package

kubeconfig="/Users/fangzong.lyj/.kube/config-dailyrun"

pod=$(kubectl -n sreworks --kubeconfig=${kubeconfig} get pod | grep prod-search-tkgone | grep Running | awk '{print $1}' | sed -n '1p')
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- rm -f /app/tkg-one.jar
time kubectl -n sreworks --kubeconfig=${kubeconfig} cp tkg-one-start/target/tkg-one.jar ${pod}:/app/
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- java -Xmx1g -Xms1g -XX:ActiveProcessorCount=2 -jar /app/tkg-one.jar
