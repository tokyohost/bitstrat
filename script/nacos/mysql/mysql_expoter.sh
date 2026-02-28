CREATE USER 'exporter'@'%' IDENTIFIED BY '8ac9bb8e2c' WITH MAX_USER_CONNECTIONS 10;
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';

CREATE USER 'exporter'@'%' IDENTIFIED BY '8ac9bb8e2c' WITH MAX_USER_CONNECTIONS 10;
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
docker run -d \
  --name mysqld-exporter \
  --restart unless-stopped \
  -p 9104:9104 \
  -v /bitstrat/exporter/mysqld_exporter.cnf:/.my.cnf:ro \
  --network my-mysql-network \
  prom/mysqld-exporter \
  --mysqld.address=nacos:3306

docker network create my-mysql-network

docker run -d \
  --name mysqld-exporter \
  --restart unless-stopped \
  -p 9104:9104 \
  -v /root/bitstrat/exporter/mysqld_exporter.cnf:/.my.cnf:ro \
  --network my-mysql-network \
  prom/mysqld-exporter \
  --mysqld.address=74.48.108.111:3306
