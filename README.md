docker-compose up -d

./bin/kafka-topics.sh  --create --zookeeper localhost:2181 \
--replication-factor 1 \
--partitions 1 \
--config retention.ms=-1 \
--topic order.delivery

./bin/kafka-topics.sh  --create --zookeeper localhost:2181 \
--replication-factor 1 \
--partitions 1 \
--config retention.ms=-1 \
--topic order.details

./bin/kafka-topics.sh  --create --zookeeper localhost:2181 \
--replication-factor 1 \
--partitions 1 \
--config retention.ms=-1 \
--topic products
