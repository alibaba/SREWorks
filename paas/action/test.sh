set -e
set -x
mvn clean package -DskipTests

pod=`kubectl --kubeconfig=/Users/jinghua.yjh/.kube/config.js -n sreworks get pod | grep paas-action | grep Running | grep -v appmanager | awk '{print $1}'`
kubectl --kubeconfig=/Users/jinghua.yjh/.kube/config.js -n sreworks exec -ti ${pod} -- rm -f /app/action.jar
time kubectl --kubeconfig=/Users/jinghua.yjh/.kube/config.js -n sreworks cp target/action-0.0.1-SNAPSHOT.jar ${pod}:/app/action.jar
kubectl --kubeconfig=/Users/jinghua.yjh/.kube/config.js -n sreworks exec -ti ${pod} -- java -XX:ActiveProcessorCount=2 -Dloader.path=/app/ -jar /app/action.jar --spring.config.location=/app/

