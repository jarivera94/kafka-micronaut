package co.com.sunset.order.service;

import co.com.sunset.order.kafka.producer.OderKafkaProducer;
import co.com.sunset.order.models.OderDomain;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.reactivex.Single;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class OderManagementService implements DataFetcher<CompletionStage<OderDomain>> {

  private final OderKafkaProducer orderKafkaProducer;
  private ObjectMapper objectMapper;

  public OderManagementService(OderKafkaProducer orderKafkaProducer) {
    this.orderKafkaProducer = orderKafkaProducer;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public CompletionStage<OderDomain> get(DataFetchingEnvironment environment) throws Exception {
    final CompletableFuture<OderDomain> future = new CompletableFuture<>();

    Single.just(environment.getArgument("orderInput"))
        .map(input -> objectMapper.writeValueAsString(input))
        .map(inputString -> objectMapper.readValue(inputString, OderDomain.class))
        .map(
            orderDomain -> {
              orderDomain.setKey(UUID.randomUUID().toString());
              return orderDomain;
            })
        .flatMap(
            orderDomain ->
                orderKafkaProducer
                    .saveCustomer(orderDomain.getKey(), orderDomain)
                    .doOnSuccess(
                        productDomainSaved ->
                            log.info(
                                "OderManagementService::get order {} saved successful",
                                productDomainSaved.getUser()))
                    .doOnError(
                        throwable ->
                            log.error(
                                "OderManagementService::get order {} was not saved see error",
                                orderDomain.getKey(),
                                throwable.getMessage())))
        .subscribe(future::complete, future::completeExceptionally);
    return future;
  }
}
