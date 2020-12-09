package co.com.sunset.product.service;

import co.com.sunset.product.kafka.producer.ProductKafkaProducer;
import co.com.sunset.product.models.ProductDomain;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.reactivex.Single;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProductManagementServiceSave implements DataFetcher<CompletionStage<ProductDomain>> {

  private final ProductKafkaProducer productKafkaProducer;
  private final ObjectMapper objectMapper;

  public ProductManagementServiceSave(ProductKafkaProducer productKafkaProducer) {
    this.productKafkaProducer = productKafkaProducer;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public CompletionStage<ProductDomain> get(DataFetchingEnvironment environment) throws Exception {
    final CompletableFuture<ProductDomain> future = new CompletableFuture<>();

    Single.just(environment.getArgument("productDomainInput"))
        .map(input -> objectMapper.writeValueAsString(input))
        .map(inputString -> objectMapper.readValue(inputString, ProductDomain.class))
        .map(
            productDomain -> {
              productDomain.setKey(productDomain.getId() + "-" + productDomain.getName());
              return productDomain;
            })
        .flatMap(
            productDomain ->
                productKafkaProducer
                    .saveCustomer(productDomain.getKey(), productDomain)
                    .doOnSuccess(
                        productDomainSaved ->
                            log.info(
                                "ProductManagementServiceSave::get product {} saved successful",
                                productDomainSaved.getKey()))
                    .doOnError(
                        throwable ->
                            log.error(
                                "ProductManagementServiceSave::get product {} was not saved see error",
                                productDomain.getKey(),
                                throwable.getMessage())))
        .subscribe(future::complete, future::completeExceptionally);
    return future;
  }
}
