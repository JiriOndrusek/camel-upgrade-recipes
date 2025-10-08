/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.upgrade;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate415Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_15)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_14, "camel-langchain4j-chat",
                  "camel-langchain4j-embeddings", "camel-milvus", "camel-neo4j", "camel-pinecone", "camel-qdrant", "camel-weaviate",
                  "camel-core-model", "camel-spi", "camel-api", "camel-base-engine", "jakarta.xml.bind-api"))
          .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_camel_ai_nested_headers_classes">Camel AI Nested Headers classes</a>
     */
    @Test
    void aiNestedHeadersTest() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.langchain4j.chat.LangChain4jChat.Headers;
                  import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddings.Headers;
                  import org.apache.camel.component.milvus.Milvus.Headers;
                  import org.apache.camel.component.neo4j.Neo4jConstants.Headers;
                  import org.apache.camel.component.qdrant.Qdrant.Headers;
                  import org.apache.camel.component.pinecone.PineconeVectorDb.Headers;
                  import org.apache.camel.component.weaviate.WeaviateVectorDb.Headers;
      
                  public class AiHeaders{
                      public void method() {
                          org.apache.camel.component.langchain4j.chat.LangChain4jChat.Headers h1 = null;
                          org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddings.Headers h2 = null;
                          org.apache.camel.component.milvus.Milvus.Headers h4 = null;
                          org.apache.camel.component.neo4j.Neo4jConstants.Headers h5 = null;
                          org.apache.camel.component.qdrant.Qdrant.Headers h6 = null;
                          org.apache.camel.component.pinecone.PineconeVectorDb.Headers h7 = null;
                          org.apache.camel.component.weaviate.WeaviateVectorDb.Headers h8 = null;
                      }
                  }
                  """,
                """
                import org.apache.camel.component.langchain4j.chat.LangChain4jChatHeaders;
                import org.apache.camel.component.langchain4j.embeddings.LangChain4jEmbeddingsHeaders;
                import org.apache.camel.component.milvus.MilvusHeaders;
                import org.apache.camel.component.neo4j.Neo4jHeaders;
                import org.apache.camel.component.qdrant.QdrantHeaders;
                import org.apache.camel.component.pinecone.PineconeVectorDbHeaders;
                import org.apache.camel.component.weaviate.WeaviateVectorDbHeaders;
                
                public class AiHeaders{
                    public void method() {
                        LangChain4jChatHeaders h1 = null;
                        LangChain4jEmbeddingsHeaders h2 = null;
                        MilvusHeaders h4 = null;
                        Neo4jHeaders h5 = null;
                        QdrantHeaders h6 = null;
                        PineconeVectorDbHeaders h7 = null;
                        WeaviateVectorDbHeaders h8 = null;
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a>
     */
    @Test
    void dataformatCryptoTest() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.model.dataformat.CryptoDataFormat;
                  import org.apache.camel.model.dataformat.CsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackCsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackDataFormat;
                  import org.apache.camel.model.dataformat.JaxbDataFormat;
                  import org.apache.camel.model.dataformat.SoapDataFormat;
                  import org.apache.camel.model.dataformat.SwiftMxDataFormat;
                  import org.apache.camel.model.dataformat.XmlSecurityDataFormat;
      
                  public class Cr {
                    public void cryptoDataFormat() {
                        CryptoDataFormat cfd = new CryptoDataFormat();
                        cfd.setAlgorithmParameterRef("111");
                        cfd.setKeyRef("222");
                        cfd.setInitVectorRef("333");

                        CsvDataFormat csv = new CsvDataFormat();
                        csv.setFormatRef("444");
                        csv.setFormatName("555");

                        new FlatpackDataFormat().setParserFactoryRef("666");

                        new JaxbDataFormat().setNamespacePrefixRef("777");

                        SoapDataFormat soap = new SoapDataFormat();;

                        soap.setNamespacePrefixRef("888");
                        soap.setElementNameStrategyRef("999");

                        SwiftMxDataFormat swift = new SwiftMxDataFormat();
                        swift.setReadConfigRef("1111");
                        swift.setWriteConfigRef("1222");
                        
//                        new XMLSecurityDataFormat().setKeyOrTrustStoreParametersRef("1333");
                    }
                  }
                  """, //there is missing a reference for proper parsing of XMLSecurityDataFormat
                """

                  import org.apache.camel.model.dataformat.CryptoDataFormat;
                  import org.apache.camel.model.dataformat.CsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackCsvDataFormat;
                  import org.apache.camel.model.dataformat.FlatpackDataFormat;
                  import org.apache.camel.model.dataformat.JaxbDataFormat;
                  import org.apache.camel.model.dataformat.SoapDataFormat;
                  import org.apache.camel.model.dataformat.SwiftMxDataFormat;
                  import org.apache.camel.model.dataformat.XmlSecurityDataFormat;
      
                  public class Cr {
                    public void cryptoDataFormat() {
                        CryptoDataFormat cfd = new CryptoDataFormat();
                        cfd.setAlgorithmParameterSpec("111");
                        cfd.setKey("222");
                        cfd.setInitVector("333");
                        
                        CsvDataFormat csv = new CsvDataFormat();
                        csv.setFormat("444");
                        csv.setFormat("555");
                        
                        new FlatpackDataFormat().setParserFactory("666");
                        
                        new JaxbDataFormat().setNamespacePrefix("777");
                        
                        SoapDataFormat soap = new SoapDataFormat();;
                      
                        soap.setNamespacePrefix("888");
                        soap.setElementNameStrategy("999");
                        
                        SwiftMxDataFormat swift = new SwiftMxDataFormat();
                        swift.setReadConfig("1111");
                        swift.setWriteConfig("1222");
                        
//                        new XMLSecurityDataFormat().setKeyOrTrustStoreParametersRef("1333");
                    }
                  }
                """));
    }
//
//
//    /**
//     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_15.html#_data_formats">Data Formats</a>
//     */
//    @Test
//    void dataFormatsCryptoYamlTest() {
//        //language=yaml
//        rewriteRun(yaml(
//                """
//                       route:
//                         id: route-2366
//                         from:
//                           id: start
//                           uri: direct
//                         parameters:
//                           name: start
//                           steps:
//                             - to:
//                               id: to-4212
//                               uri: crypto
//                               parameters:
//                                 algorithmParameterRef: 111
//                                 initVectorRef: 222
//                                 keyRef: 333
//                             - to:
//                               uri: csv
//                               parameters:
//                                 formatRef: 444
//                             - to:
//                               uri: csv
//                               parameters:
//                                 formatName: 555
//                             - to:
//                               uri: flatpack
//                               parameters:
//                                 parserFactoryRef: 666
//                             - to:
//                               uri: jaxb
//                               parameters:
//                                 namespacePrefixRef: 777
//                             - to:
//                               uri: soap
//                               parameters:
//                                 namespacePrefixRef: 888
//                                 elementNameStrategyRef: 999
//                             - to:
//                               uri: swiftMx
//                               parameters:
//                                 readConfigRef: 1111
//                                 writeConfigRef: 1222
//                             - to:
//                               uri: xmlSecurity
//                               parameters:
//                                 keyOrTrustStoreParametersRef: 1333
//                  """,
//                """
//                       route:
//                         id: route-2366
//                         from:
//                           id: start
//                           uri: direct
//                         parameters:
//                           name: start
//                           steps:
//                             - to:
//                               id: to-4212
//                               uri: crypto
//                               parameters:
//                                 algorithmParameterSpec: 111
//                                 initVector: 222
//                                 key: 333
//                             - to:
//                               uri: csv
//                               parameters:
//                                 format: 444
//                             - to:
//                               uri: csv
//                               parameters:
//                                 format: 555
//                             - to:
//                               uri: flatpack
//                               parameters:
//                                 parserFactory: 666
//                             - to:
//                               uri: jaxb
//                               parameters:
//                                 namespacePrefix: 777
//                             - to:
//                               uri: soap
//                               parameters:
//                                 namespacePrefix: 888
//                                 elementNameStrategy: 999
//                             - to:
//                               uri: swiftMx
//                               parameters:
//                                 readConfig: 1111
//                                 writeConfig: 1222
//                             - to:
//                               uri: xmlSecurity
//                               parameters:
//                                 keyOrTrustStoreParameters: 1333
//                  """));
//    }
//
//
//    /**
//     *
//     */
//    @Test
//    void dataFormatsCryptoXmlTest() {
//        //language=xml
//        rewriteRun(xml(
//                """
//                  <camelContext>
//                    <route>
//                      <from uri="direct:start"/>
//                      <to uri="crypto:something" algorithmParameterRef='111' initVectorRef='222' keyRef='333'/>
//                      <to uri="csv" formatRef='444'/>
//                      <to uri="csv" formatName='555'/>
//                      <to uri="flatpack" parserFactoryRef='666'/>
//                      <to uri="jaxb" namespacePrefixRef='777'/>
//                      <to uri="soap" namespacePrefixRef='888' elementNameStrategyRef='999'/>
//                      <to uri="swiftMx" readConfigRef='1111' writeConfigRef='1222'/>
//                      <to uri="xmlSecurity" keyOrTrustStoreParametersRef='1333'/>
//                    </route>
//                  </camelContext>
//                  """,
//                """
//                  <camelContext>
//                    <route>
//                      <from uri="direct:start"/>
//                      <to uri="crypto:something" algorithmParameterSpec='111' key='333' initVector='222'/>
//                      <to uri="csv" format='444'/>
//                      <to uri="csv" format='555'/>
//                      <to uri="flatpack" parserFactory='666'/>
//                      <to uri="jaxb" namespacePrefix='777'/>
//                      <to uri="soap" namespacePrefix='888' elementNameStrategy='999'/>
//                      <to uri="swiftMx" readConfig='1111' writeConfig='1222'/>
//                      <to uri="xmlSecurity" keyOrTrustStoreParameters='1333'/>
//                    </route>
//                  </camelContext>
//                  """));
//
//    }

}
