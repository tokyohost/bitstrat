docker run -d --name=node-exporter \
  -p 9990:9100 \
  --net=host \
  quay.io/prometheus/node-exporter:latest
