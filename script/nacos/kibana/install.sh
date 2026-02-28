docker run -d \
  --restart=always \
  --name kibana \
  --network sky_default \
  -p 127.0.0.1:5600:5601 \
  --privileged \
  -e ELASTICSEARCH_HOSTS=http://elasticsearch:9200 \
  kibana:7.17.6
