package co.com.sunset.order.kafka.producer;

import co.com.sunset.order.models.OrderDomain;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.messaging.annotation.Body;
import io.reactivex.Single;
import org.apache.kafka.clients.producer.ProducerConfig;

@KafkaClient(
    id = "order-service",
    acks = KafkaClient.Acknowledge.ALL,
    properties = @Property(name = ProducerConfig.RETRIES_CONFIG, value = "5"))
public interface OderKafkaProducer {

  @Topic("order.delivery")
  Single<OrderDomain> saveCustomer(@KafkaKey String key, @Body OrderDomain customer);
}
