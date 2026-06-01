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

import org.apache.camel.upgrade.camel421.RenameHeaders;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate421Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_21)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_20,
                        "camel-core-model", "camel-api"))
                .typeValidationOptions(TypeValidation.none());
    }


    @DocumentExample
    @Test
    void testCompositeMigrationXmlDsl() {
        //language=xml
        rewriteRun(
                xml(
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:start"/>
                            <setHeader name="kafka.TOPIC">
                                <constant>my-topic</constant>
                            </setHeader>
                        </route>
                        """,
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:start"/>
                            <setHeader name="CamelKafkaTopic">
                                <constant>my-topic</constant>
                            </setHeader>
                        </route>
                        """
                )
        );
    }

    @Test
    void testCompositeMigrationYamlDsl() {
        //language=yaml
        rewriteRun(
                yaml(
                        """
                        - route:
                            from:
                              uri: "direct:start"
                            steps:
                              - setHeader:
                                  name: kafka.TOPIC
                                  constant: my-topic
                        """,
                        """
                        - route:
                            from:
                              uri: "direct:start"
                            steps:
                              - setHeader:
                                  name: CamelKafkaTopic
                                  constant: my-topic
                        """
                )
        );
    }

    @Test
    void testCompositeMigrationJavaMethodAndSimpleExpression() {
        // Test that the composite recipe can handle both Java method calls and Simple expressions
        // in the same file - a common real-world pattern where headers are set in Java and
        // referenced in Simple expressions
        //language=java
        rewriteRun(
                java(
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;
        
                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("kafka.TOPIC", "topic1");
                                    })
                                    .setBody(simple("${header.kafka.TOPIC}"));
                            }
                        }
                        """,
                        """
                        import org.apache.camel.Exchange;
                        import org.apache.camel.builder.RouteBuilder;
        
                        class Test extends RouteBuilder {
                            public void configure() {
                                from("direct:start")
                                    .process(exchange -> {
                                        exchange.getIn().setHeader("CamelKafkaTopic", "topic1");
                                    })
                                    .setBody(simple("${header.CamelKafkaTopic}"));
                            }
                        }
                        """
                )
        );
    }
}
