set -x
set -e 

ROOT=$(cd `dirname $0`; pwd)

if [ "$FORCE" = "true" ];then
   bash ${ROOT}/dataops-es-migration.sh -f
else
   bash ${ROOT}/dataops-es-migration.sh
fi

if [ "$FORCE" = "true" ];then
   bash ${ROOT}/dataops-mysql-migration.sh -f
else
   bash ${ROOT}/dataops-mysql-migration.sh
fi

bash ${ROOT}/core-mysql-migration.sh 

