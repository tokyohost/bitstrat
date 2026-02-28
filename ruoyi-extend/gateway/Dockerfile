# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-debian:17.0.11-cds
#FROM bellsoft/liberica-openjdk-debian:21.0.5-cds
#FROM findepi/graalvm:java17-native

LABEL maintainer="tokyohost"

RUN mkdir -p /ruoyi/gateway/logs

WORKDIR /ruoyi/gateway

#HOST_IP=$(hostname -I | awk '{print $1}')

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS="" SKYWALKING_COLLECTOR_BACKEND_SERVICE="127.0.0.1:11800" HOST_IP="unknown"

EXPOSE 9000

ADD ./target/gateway.jar ./app.jar
# SkyWalking 代理文件
# 不使用skywalking 注释以下
#ADD ./skywalking-agent/ /ruoyi/gateway/agent/

SHELL ["/bin/bash", "-c"]

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom \

            -Dskywalking.agent.service_name=bitstrat-gateway \
            # 不使用skywalking 注释以下
           #-javaagent:/ruoyi/gateway/agent/skywalking-agent.jar \
           -Dskywalking.agent.instance_name=${HOST_IP} \
           -Dskywalking.collector.backend_service=${SKYWALKING_COLLECTOR_BACKEND_SERVICE} \
           -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar
