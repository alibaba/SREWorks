#!/bin/sh

set -x

export BASE_DIR="/home/nacos"
export DEFAULT_SEARCH_LOCATIONS="classpath:/,classpath:/config/,file:./,file:./config/"
export CUSTOM_SEARCH_LOCATIONS=${DEFAULT_SEARCH_LOCATIONS},file:${BASE_DIR}/conf/,${BASE_DIR}/init.d/
export CUSTOM_SEARCH_NAMES="application,custom"
PLUGINS_DIR="/home/nacos/plugins/peer-finder"
function print_servers(){
   if [[ ! -d "${PLUGINS_DIR}" ]]; then
    echo "" > "$CLUSTER_CONF"
    export HOST_IP_LIST=$(echo "${NACOS_SERVERS}" | sed -n 1'p' | tr ',' '\n')
    for server in ${HOST_IP_LIST}; do
            echo "$server"":${NACOS_SERVER_PORT}" >> "$CLUSTER_CONF"
    done
   else
    bash $PLUGINS_DIR/plugin.sh
   sleep 30
	fi
}

#===========================================================================================
# JVM Configuration
#===========================================================================================
export JVM_XMX="256m"
if [[ "${MODE}" == "standalone" ]]; then

    JAVA_OPT="${JAVA_OPT} -Xmx${JVM_XMX} -Xms${JVM_XMX}"
    JAVA_OPT="${JAVA_OPT} -Dnacos.standalone=true"
else

  JAVA_OPT="${JAVA_OPT} -server -Xmx${JVM_XMX} -Xms${JVM_XMX}"

  JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs/java_heapdump.hprof"
  JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"
  print_servers
fi

# 天基的雷，必须加这个

JAVA_OPT="${JAVA_OPT} -XX:ActiveProcessorCount=2"


#===========================================================================================
# Setting system properties
#===========================================================================================
# set  mode that Nacos Server function of split
if [[ "${FUNCTION_MODE}" == "config" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.functionMode=config"
elif [[ "${FUNCTION_MODE}" == "naming" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.functionMode=naming"
fi

# set nacos server ip

if [[ ! -z "${NACOS_SERVER_IP}" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.server.ip=${NACOS_SERVER_IP}"
fi

if [[ ! -z "${USE_ONLY_SITE_INTERFACES}" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.inetutils.use-only-site-local-interfaces=${USE_ONLY_SITE_INTERFACES}"
fi

if [[ ! -z "${PREFERRED_NETWORKS}" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.inetutils.preferred-networks=${PREFERRED_NETWORKS}"
fi

if [[ ! -z "${IGNORED_INTERFACES}" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.inetutils.ignored-interfaces=${IGNORED_INTERFACES}"
fi

if [[ "${PREFER_HOST_MODE}" == "ip" ]]; then
    JAVA_OPT="${JAVA_OPT} -Dnacos.preferHostnameOverIp=true"
fi


## gc log
JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext"
JAVA_OPT="${JAVA_OPT} -Xloggc:${BASE_DIR}/logs/nacos_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"



JAVA_OPT="${JAVA_OPT} -Dnacos.home=${BASE_DIR}"
JAVA_OPT="${JAVA_OPT} -jar ${BASE_DIR}/tesla-nacos.jar"
JAVA_OPT="${JAVA_OPT} ${JAVA_OPT_EXT}"
JAVA_OPT="${JAVA_OPT} --spring.config.location=${CUSTOM_SEARCH_LOCATIONS}"
JAVA_OPT="${JAVA_OPT} --spring.config.name=${CUSTOM_SEARCH_NAMES}"
JAVA_OPT="${JAVA_OPT} --logging.config=${BASE_DIR}/conf/nacos-logback.xml"
JAVA_OPT="${JAVA_OPT} --server.max-http-header-size=524288"



# 显示当前 ENV 变量
DOCKER_COMMAND='docker run -it --entrypoint "bash" '
for item in $(env); do
  DOCKER_COMMAND+="-e ${item} "
done
DOCKER_COMMAND+="IMAGE_ID"
echo "Docker Command: ${DOCKER_COMMAND}"

echo "nacos is starting,you can check the ${BASE_DIR}/logs/start.out"
echo "java ${JAVA_OPT}"
exec java ${JAVA_OPT}