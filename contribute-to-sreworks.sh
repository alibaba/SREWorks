#!/bin/bash


#
# sreworks_project_path/contribute-to-sreworks.sh "paas/appmanager"
#

SW_ROOT=$(cd `dirname $0`; pwd)

IS_GIT_ROOT=$(ls -l .git|wc -l|awk '{print $1}')

if [ "$IS_GIT_ROOT" = "0" ];then
   echo ""
   echo "Please run contribute-to-sreworks.sh in Git Project root path /"
   exit 1
fi

# 判断目标路径是否存在
TARGET_PATH=$1
if [ ! -d ${SW_ROOT}/${TARGET_PATH} ];then
   echo "Target app path not found"
   exit 1
fi


# 只能拷贝到paas/saas这两个目录下
if [[ "$TARGET_PATH" =~ ^paas/.* ]] || [[ "$TARGET_PATH" =~ ^saas/.* ]] || [[ "$TARGET_PATH" =~ ^/paas/.* ]] || [[ "$TARGET_PATH" =~ ^/saas/.* ]]; then
    echo "Path check ok"
else
    echo "Please copy code to paas/* or saas/*"
    echo ""
    echo "List paas/"
    ls -l ${SW_ROOT}/${TARGET_PATH}/paas/|grep -v "total "
    echo ""
    echo "List saas/"
    ls -l ${SW_ROOT}/${TARGET_PATH}/saas/|grep -v "total "
    exit 1
fi


# 将当前代码拷贝到一个临时目录，移除.git文件
rm -rf /tmp/tmp_sw_project
mkdir -p /tmp/tmp_sw_project
which rsync
if [ $? -eq 0 ]; then
    rsync -r --exclude="node_modules" --exclude=".git" ./ /tmp/tmp_sw_project/
    if [[ "$TARGET_PATH" =~ "sw-frontend" ]]; then
        mv ${SW_ROOT}/${TARGET_PATH}/docs /tmp/tmp_sw_project/docs
    fi
else
    cp -r ./ /tmp/tmp_sw_project/
    rm -rf /tmp/tmp_sw_project/.git
    find /tmp/tmp_sw_project/ -type d -name "node_modules" | xargs rm -rf
fi

mv /tmp/tmp_sw_project ${SW_ROOT}/${TARGET_PATH}/../
mv ${SW_ROOT}/${TARGET_PATH}/../tmp_sw_project ${SW_ROOT}/${TARGET_PATH}.bak
rm -rf ${SW_ROOT}/${TARGET_PATH}
mv ${SW_ROOT}/${TARGET_PATH}.bak ${SW_ROOT}/${TARGET_PATH}
echo "Copy code ok"

