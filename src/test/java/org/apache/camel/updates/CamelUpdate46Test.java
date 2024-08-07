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
package org.apache.camel.updates;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.openrewrite.yaml.Assertions;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate46Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_6)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_5,
                        "camel-api", "camel-base-engine", "camel-spring-redis", "camel-opensearch", "camel-elasticsearch"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_6.html#_camel_elasticsearch">CAMEL-ELASTICSEARCH</a>
     */
    @Test
    void testSearch() {
        //language=java
        rewriteRun(java(
                """
                            import org.apache.camel.component.es.aggregation.ElastichsearchBulkRequestAggregationStrategy;
                            
                            public class SearchTest {
                                public void test() {
                                    ElastichsearchBulkRequestAggregationStrategy str = null;
                                }
                            }
                        """,
                """
                            import org.apache.camel.component.es.aggregation.ElasticsearchBulkRequestAggregationStrategy;
                            
                            public class SearchTest {
                                public void test() {
                                    ElasticsearchBulkRequestAggregationStrategy str = null;
                                }
                            }
                        """));
    }


    @Test
    void testBeanPropertyToProperties() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                beans:
                  - name: "myProcessor"
                    type: "#class:com.foo.MyClass"
                    property:
                      - key: "payload"
                        value: "test-payload"
                """, """
                beans:
                  - name: "myProcessor"
                    type: "#class:com.foo.MyClass"
                    properties:
                      payload: "test-payload"
                """));
    }

    @Test
    void testBeanPropertyToProperties2() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - beans:
                  - name: beanFromMap
                    type: com.acme.MyBean
                    property:
                      - key: foo
                        value: bar
                      - key: foo2
                        value: bar2
                """, """
                beans:
                  - name: "myProcessor"
                    type: "#class:com.foo.MyClass"
                    properties:
                      payload: "test-payload"
                """));
    }

}
