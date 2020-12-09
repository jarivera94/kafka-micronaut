package co.com.sunset.product.service.graphql;

import co.com.sunset.product.service.ProductManagementServiceRetrieve;
import co.com.sunset.product.service.ProductManagementServiceSave;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.visibility.NoIntrospectionGraphqlFieldVisibility;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Factory
@Slf4j
public class GraphQLFactory {

  @Bean
  @Singleton
  public GraphQL graphQL(
      ResourceResolver resourceResolver,
      ProductManagementServiceSave productManagementServiceSave,
      ProductManagementServiceRetrieve productManagementServiceRetrieve) {

    SchemaParser schemaParser = new SchemaParser();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
    typeRegistry.merge(
        schemaParser.parse(
            new BufferedReader(
                new InputStreamReader(
                    resourceResolver.getResourceAsStream("classpath:schema.graphqls").get()))));
    RuntimeWiring runtimeWiring =
        RuntimeWiring.newRuntimeWiring()
            .type(
                "Mutation",
                typeWiring -> typeWiring.dataFetcher("saveProduct", productManagementServiceSave))
            .type(
                "Query",
                typeWiring ->
                    typeWiring.dataFetcher("retrieveProduct", productManagementServiceRetrieve))
            .fieldVisibility(
                NoIntrospectionGraphqlFieldVisibility.NO_INTROSPECTION_FIELD_VISIBILITY)
            .build();

    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema).build();
  }
}
