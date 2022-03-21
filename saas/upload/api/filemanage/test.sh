set -e
set -x
mvn clean package

kubeconfig="/Users/jinghua.yjh/.kube/config.js"

pod=`kubectl -n sreworks --kubeconfig=${kubeconfig} get pod | grep prod-upload-filemanage | grep Running | awk '{print $1}'`
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- rm -f /app/filemanage.jar
time kubectl -n sreworks --kubeconfig=${kubeconfig} cp filemanage-start/target/filemanage.jar ${pod}:/app/
kubectl -n sreworks --kubeconfig=${kubeconfig} exec -ti ${pod} -- java -Xmx1g -Xms1g -XX:ActiveProcessorCount=2 -jar /app/filemanage.jar
