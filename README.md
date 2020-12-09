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

export BROKERS=localhos:9092

./bin/kafka-console-consumer.sh \
--bootstrap-server ${BROKERS} \
--topic  order.delivery \
--property print.key=true --property key.separator="[value]" \
--from-beginning

./bin/kafka-console-consumer.sh \
--bootstrap-server ${BROKERS} \
--topic  order.details \
--property print.key=true --property key.separator="[value]" \
--from-beginning

./bin/kafka-console-consumer.sh \
--bootstrap-server ${BROKERS} \
--topic  products \
--property print.key=true --property key.separator="[value]" \
--from-beginning