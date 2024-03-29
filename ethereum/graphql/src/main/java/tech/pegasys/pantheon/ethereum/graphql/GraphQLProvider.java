/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.graphql;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import tech.pegasys.pantheon.ethereum.graphql.internal.scalar.AddressScalar;
import tech.pegasys.pantheon.ethereum.graphql.internal.scalar.BigIntScalar;
import tech.pegasys.pantheon.ethereum.graphql.internal.scalar.Bytes32Scalar;
import tech.pegasys.pantheon.ethereum.graphql.internal.scalar.BytesScalar;
import tech.pegasys.pantheon.ethereum.graphql.internal.scalar.LongScalar;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLProvider {

  private GraphQLProvider() {}

  public static GraphQL buildGraphQL(final GraphQLDataFetchers graphQLDataFetchers)
      throws IOException {
    final URL url = Resources.getResource("schema.graphqls");
    final String sdl = Resources.toString(url, Charsets.UTF_8);
    final GraphQLSchema graphQLSchema = buildSchema(sdl, graphQLDataFetchers);
    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private static GraphQLSchema buildSchema(
      final String sdl, final GraphQLDataFetchers graphQLDataFetchers) {
    final TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
    final RuntimeWiring runtimeWiring = buildWiring(graphQLDataFetchers);
    final SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
  }

  private static RuntimeWiring buildWiring(final GraphQLDataFetchers graphQLDataFetchers) {
    return RuntimeWiring.newRuntimeWiring()
        .scalar(new AddressScalar())
        .scalar(new Bytes32Scalar())
        .scalar(new BytesScalar())
        .scalar(new LongScalar())
        .scalar(new BigIntScalar())
        .type(
            newTypeWiring("Query")
                .dataFetcher("account", graphQLDataFetchers.getAccountDataFetcher())
                .dataFetcher("block", graphQLDataFetchers.getBlockDataFetcher())
                .dataFetcher("blocks", graphQLDataFetchers.getRangeBlockDataFetcher())
                .dataFetcher("transaction", graphQLDataFetchers.getTransactionDataFetcher())
                .dataFetcher("gasPrice", graphQLDataFetchers.getGasPriceDataFetcher())
                .dataFetcher("syncing", graphQLDataFetchers.getSyncingDataFetcher())
                .dataFetcher("pending", graphQLDataFetchers.getPendingStateDataFetcher())
                .dataFetcher(
                    "protocolVersion", graphQLDataFetchers.getProtocolVersionDataFetcher()))
        .type(
            newTypeWiring("Mutation")
                .dataFetcher(
                    "sendRawTransaction", graphQLDataFetchers.getSendRawTransactionDataFetcher()))
        .build();
  }
}
