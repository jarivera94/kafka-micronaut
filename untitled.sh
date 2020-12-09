docker-compose up -d

./bin/kafka-topics.sh  --create --zookeeper localhost:9092 \
--replication-factor 1 \
--partitions 1 \
--config retention.ms=-1 \
--topic order.delivery

./bin/kafka-topics.sh  --create --zookeeper localhost:9092 \
--replication-factor 1 \
--partitions 1 \
--config retention.ms=-1 \
--topic order.details

./bin/kafka-topics.sh  --create --zookeeper localhost:9092 \
--replication-factor 1 \
--partitions 1 \
--config retention.ms=-1 \
--topic products

 mn create-app oder-delivery-services --features kafka