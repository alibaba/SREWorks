set -x
set -e

script_dir=$(cd $(dirname $0);pwd)

# 获取目标集群sreworks包的values.yaml
values=$(helm get values sreworks -n sreworks $*)

echo "$values"|grep -v "USER-SUPPLIED VALUES" > /tmp/sreworks-values.yaml 

cat /tmp/sreworks-values.yaml

kubectl get job -nsreworks $*|grep init-job|awk '{print $1}'|while read line
do
kubectl delete job $line -nsreworks $*
done

helm upgrade sreworks $script_dir/../chart/sreworks-chart --namespace sreworks -f /tmp/sreworks-values.yaml $*

