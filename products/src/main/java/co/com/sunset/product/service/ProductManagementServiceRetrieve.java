package co.com.sunset.product.service;

import co.com.sunset.product.models.ProductDomain;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.reactivex.Single;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Singleton;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@Singleton
public class ProductManagementServiceRetrieve
    implements DataFetcher<CompletionStage<ProductDomain>> {

  private final KafkaStreams kafkaStreams;

  public ProductManagementServiceRetrieve(KafkaStreams kafkaStreams) {
    this.kafkaStreams = kafkaStreams;
  }

  @Override
  public CompletionStage<ProductDomain> get(DataFetchingEnvironment environment) throws Exception {
    final CompletableFuture<ProductDomain> future = new CompletableFuture<>();
    Single.just(environment.getArgument("productKey"))
        .map(String::valueOf)
        .map(
            productKey -> {
              QueryableStoreType<?> queryableStoreType;
              ReadOnlyKeyValueStore<Object, Object> materializedView =
                  kafkaStreams.store(
                      StoreQueryParameters.fromNameAndType(
                              "ktable-producst", QueryableStoreTypes.keyValueStore())
                          .enableStaleStores());
              return (ProductDomain) materializedView.get(productKey);
            })
        .subscribe(future::complete, future::completeExceptionally);
    return future;
  }
}
