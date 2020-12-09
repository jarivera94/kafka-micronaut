package co.com.sunset.product.kafka.producer;

import co.com.sunset.product.models.ProductDomain;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.messaging.annotation.Body;
import io.reactivex.Single;
import org.apache.kafka.clients.producer.ProducerConfig;

@KafkaClient(
    id = "products-service",
    acks = KafkaClient.Acknowledge.ALL,
    properties = @Property(name = ProducerConfig.RETRIES_CONFIG, value = "5"))
public interface ProductKafkaProducer {

  @Topic("products")
  Single<ProductDomain> saveCustomer(@KafkaKey String key, @Body ProductDomain customer);
}
