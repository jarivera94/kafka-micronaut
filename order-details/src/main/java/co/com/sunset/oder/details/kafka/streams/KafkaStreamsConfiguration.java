package co.com.sunset.oder.details.kafka.streams;

import co.com.sunset.oder.details.kafka.streams.serde.Jackson2Serde;
import co.com.sunset.oder.details.models.OrderDetail;
import co.com.sunset.oder.details.models.OrderDomain;
import co.com.sunset.oder.details.models.ProductDomain;
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
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

@Slf4j
@Factory
public class KafkaStreamsConfiguration {

  private static final String APPLICATION_STREAM = "order-detail-application-stream";
  private static final String CLIENT_ID = "order-details-client-id";
  private static final Function<String, String> BUILD_IDENTIFIER_BASE =
      key -> key + "-" + new SecureRandom().nextInt(100000000);

  @Singleton
  @Named(APPLICATION_STREAM)
  public KStream<String, OrderDetail> buildStream(ConfiguredStreamBuilder builder) {
    ObjectMapper OBJECT_MAPPER =
        new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .findAndRegisterModules();

    Properties props = builder.getConfiguration();
    Serde<ProductDomain> productDomainSerde =
        new Jackson2Serde<>(OBJECT_MAPPER, ProductDomain.class);
    Serde<OrderDetail> orderDetailSerde = new Jackson2Serde<>(OBJECT_MAPPER, OrderDetail.class);
    Serde<OrderDomain> orderDomainSerde = new Jackson2Serde<>(OBJECT_MAPPER, OrderDomain.class);
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

    final GlobalKTable<String, ProductDomain> productDomainGlobalKTable =
        builder.globalTable("products", Consumed.with(Serdes.String(), productDomainSerde));
    KStream<String, OrderDomain> kStreamOrderDomain =
        builder.stream("order.delivery", Consumed.with(Serdes.String(), orderDomainSerde));

    KStream<String, OrderDetail> kStreamOrderDetails =
        kStreamOrderDomain
            .join(
                productDomainGlobalKTable,
                (orderDomainKey, orderDomain) -> orderDomain.getProduct(),
                (orderDomain, productDomain) -> {
                  OrderDetail orderDetail = new OrderDetail();
                  orderDetail.setUser(orderDomain.getUser());
                  orderDetail.setProduct(orderDomain.getProduct());
                  orderDetail.setKeyOrder(orderDomain.getKey());
                  orderDetail.setProductId(productDomain.getId());
                  orderDetail.setProductName(productDomain.getName());
                  orderDetail.setProductPrice(productDomain.getPrice());
                  orderDetail.setProductDescription(productDomain.getDescription());
                  orderDetail.setMessage2(productDomain.getMessage1());
                  orderDetail.setKeyProduct(productDomain.getKey());
                  orderDetail.setMessage2("order joined from stream");
                  return orderDetail;
                })
            .selectKey((key, value) -> value.getKeyOrder());

    kStreamOrderDetails.to("order.details", Produced.with(Serdes.String(), orderDetailSerde));

    return kStreamOrderDetails;
  }
}
