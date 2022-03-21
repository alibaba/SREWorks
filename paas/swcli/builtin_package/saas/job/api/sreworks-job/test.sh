# shellcheck disable=SC2086

set -e
set -x
mvn clean package -U

kubeconfig="/Users/fangzong.lyj/.kube/config-dailyrun"

pod=$(kubectl --kubeconfig=${kubeconfig} -n sreworks get pod | grep prod-job-job-master | grep Running | awk '{print $1}')

kubectl --kubeconfig=${kubeconfig} -n sreworks exec -ti ${pod} -- rm -f /app/sreworks-job.jar
time kubectl --kubeconfig=${kubeconfig}  -n sreworks cp sreworks-job-master/target/sreworks-job.jar ${pod}:/app/
kubectl --kubeconfig=${kubeconfig}  -n sreworks exec -ti ${pod} -- java -Xmx1g -Xms1g -XX:ActiveProcessorCount=2 -jar /app/sreworks-job.jar
