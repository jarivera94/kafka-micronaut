package co.com.sunset.product.kafka.stream;

import co.com.sunset.product.kafka.stream.serde.Jackson2Serde;
import co.com.sunset.product.models.ProductDomain;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micronaut.configuration.kafka.streams.ConfiguredStreamBuilder;
import io.micronaut.context.annotation.Factory;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.function.Function;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
@Factory
public class KafkaStreamsConfiguration {

  private static final String APPLICATION_STREAM = "product-application-stream";
  private static final String CLIENT_ID = "product-client-id";
  private static final Function<String, String> BUILD_IDENTIFIER_BASE =
      key -> key + "-" + new SecureRandom().nextInt(100000000);

  @Singleton
  @Named(APPLICATION_STREAM)
  public KStream<String, ProductDomain> buildStream(ConfiguredStreamBuilder builder) {
    ObjectMapper OBJECT_MAPPER =
        new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .findAndRegisterModules();

    Properties props = builder.getConfiguration();
    Serde<ProductDomain> productDomainSerde =
        new Jackson2Serde<>(OBJECT_MAPPER, ProductDomain.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, BUILD_IDENTIFIER_BASE.apply(APPLICATION_STREAM));
    props.put(StreamsConfig.CLIENT_ID_CONFIG, BUILD_IDENTIFIER_BASE.apply(CLIENT_ID));

    log.info(
        "KafkaStreamsConfiguration::buildStream building property {} with value {}",
        ConsumerConfig.CLIENT_ID_CONFIG,
        props.getProperty(ConsumerConfig.CLIENT_ID_CONFIG));

    log.info(
        "KafkaStreamsConfiguration::buildStream building property {} with value {}",
        StreamsConfig.APPLICATION_ID_CONFIG,
        props.getProperty(StreamsConfig.APPLICATION_ID_CONFIG));

    KTable<String, ProductDomain> kTableProducts =
        builder.stream("products", Consumed.with(Serdes.String(), productDomainSerde))
            .groupByKey()
            .reduce(
                (value1, value2) -> {
                  value2.setMessage1("record updated");
                  return value2;
                },
                Materialized.<String, ProductDomain, KeyValueStore<Bytes, byte[]>>as(
                        "ktable-producst")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(productDomainSerde));

    return kTableProducts.toStream();
  }
}
