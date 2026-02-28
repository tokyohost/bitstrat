docker compose -p nacos-prometheus -f standalone-mysql-pro.yaml up -d
# 只重建nacos
docker compose -p nacos-prometheus -f standalone-mysql-pro.yaml up -d --force-recreate nacos


docker compose -p nacos-derby -f standalone-derby.yaml up -d

#强制下载最新的 latest
docker-compose up -d --pull always --force-recreate bitstrat-web-server
docker-compose up -d --pull always --force-recreate bitstrat-server
docker-compose up -d --pull always --force-recreate bitstrat-gateway-server
docker-compose up -d --pull always --force-recreate bitstrat-snailjob-server


docker-compose --f - up -d --pull always --force-recreate bitstrat-server

#低版本docker-compose
docker rm -f bitstrat-server
docker-compose -f docker-compose.yml up -d --pull always --force-recreate
docker-compose -f docker-compose.yml up -d  --force-recreate

docker pull harbor.mzlblog.com/bitstrat/bitstrat-server
#旧版本
 docker compose -f docker-compose.yml up -d --force-recreate --remove-orphans
