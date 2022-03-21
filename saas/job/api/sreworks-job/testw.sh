# shellcheck disable=SC2086

set -e
set -x
mvn clean package

kubeconfig="/Users/jinghua.yjh/.kube/config.js"

pod=$(kubectl -n sreworks --kubeconfig=${kubeconfig} get pod | grep prod-job-job-worker | grep Running | awk '{print $1}' | sed -n '1p')
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- rm -f /app/sreworks-job.jar
time kubectl -n sreworks --kubeconfig=${kubeconfig} cp sreworks-job-worker/target/sreworks-job.jar ${pod}:/app/
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- java -Xmx1g -Xms1g -XX:ActiveProcessorCount=2 -jar /app/sreworks-job.jar
