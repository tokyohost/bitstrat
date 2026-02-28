# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-debian:17.0.11-cds
#FROM bellsoft/liberica-openjdk-debian:21.0.5-cds
#FROM findepi/graalvm:java17-native

LABEL maintainer="Lion Li"

RUN mkdir -p /ruoyi/server/logs \
    /ruoyi/server/temp \
    /ruoyi/skywalking/agent

WORKDIR /ruoyi/server
#HOST_IP=$(hostname -I | awk '{print $1}')
ENV SERVER_PORT=8080 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS="" SKYWALKING_COLLECTOR_BACKEND_SERVICE="127.0.0.1:11800" HOST_IP="unknown"

EXPOSE ${SERVER_PORT}

ADD ./target/ruoyi-admin.jar ./app.jar
# 工作流字体文件
ADD ./zhFonts/ /usr/share/fonts/zhFonts/
# SkyWalking 代理文件
#ADD ./skywalking-agent/ /ruoyi/skywalking/agent/

SHELL ["/bin/bash", "-c"]

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
#            应用名称 如果想区分集群节点监控 改成不同的名称即可
#           -Dskywalking.agent.service_name=bitstrat-server \
#           -Dskywalking.agent.instance_name=${HOST_IP} \
#           -javaagent:/ruoyi/skywalking/agent/skywalking-agent.jar \
#           -Dskywalking.collector.backend_service=${SKYWALKING_COLLECTOR_BACKEND_SERVICE} \
           -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar

