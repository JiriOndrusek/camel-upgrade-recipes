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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate415Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_15)
          .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_14, "camel-langchain4j-chat",
                  "camel-langchain4j-embeddings", "camel-milvus", "camel-neo4j", "camel-pinecone", "camel-qdrant", "camel-weaviate"))
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
}
