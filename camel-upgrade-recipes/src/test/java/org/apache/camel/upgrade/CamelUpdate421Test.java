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

    @Test
    void testDnsHeadersMigrationJava() {
        // Test camel-dns header migration in Java - Message API and Simple expressions
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
                                        exchange.getIn().setHeader("dns.name", "www.example.com");
                                        exchange.getIn().setHeader("dns.server", "8.8.8.8");
                                    })
                                    .setBody(simple("${header.dns.name}"))
                                    .to("dns:lookup");
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
                                        exchange.getIn().setHeader("CamelDnsName", "www.example.com");
                                        exchange.getIn().setHeader("CamelDnsServer", "8.8.8.8");
                                    })
                                    .setBody(simple("${header.CamelDnsName}"))
                                    .to("dns:lookup");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void testDnsHeadersMigrationXml() {
        // Test camel-dns header migration in XML DSL - setHeader name attributes
        //language=xml
        rewriteRun(
                xml(
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:start"/>
                            <setHeader name="dns.name">
                                <constant>www.example.com</constant>
                            </setHeader>
                            <setHeader name="dns.type">
                                <constant>A</constant>
                            </setHeader>
                            <setHeader name="term">
                                <constant>search-term</constant>
                            </setHeader>
                            <to uri="dns:lookup"/>
                        </route>
                        """,
                        """
                        <route xmlns="http://camel.apache.org/schema/spring">
                            <from uri="direct:start"/>
                            <setHeader name="CamelDnsName">
                                <constant>www.example.com</constant>
                            </setHeader>
                            <setHeader name="CamelDnsType">
                                <constant>A</constant>
                            </setHeader>
                            <setHeader name="CamelDnsTerm">
                                <constant>search-term</constant>
                            </setHeader>
                            <to uri="dns:lookup"/>
                        </route>
                        """
                )
        );
    }

    @Test
    void testDnsHeadersMigrationYaml() {
        // Test camel-dns header migration in YAML DSL - setHeader name
        //language=yaml
        rewriteRun(
                yaml(
                        """
                        - route:
                            from:
                              uri: "direct:start"
                              steps:
                              - setHeader:
                                  name: dns.name
                                  constant: www.example.com
                              - setHeader:
                                  name: dns.server
                                  constant: 8.8.8.8
                              - setHeader:
                                  name: term
                                  constant: search-term
                              - to:
                                  uri: "dns:lookup"
                        """,
                        """
                        - route:
                            from:
                              uri: "direct:start"
                              steps:
                              - setHeader:
                                  name: CamelDnsName
                                  constant: www.example.com
                              - setHeader:
                                  name: CamelDnsServer
                                  constant: 8.8.8.8
                              - setHeader:
                                  name: CamelDnsTerm
                                  constant: search-term
                              - to:
                                  uri: "dns:lookup"
                        """
                )
        );
    }

    @Test
    void testLuceneHeadersMigration() {
        // Test camel-lucene header migration
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
                                        exchange.getIn().setHeader("QUERY", "test query");
                                        exchange.getIn().setHeader("RETURN_LUCENE_DOCS", true);
                                    })
                                    .to("lucene:search");
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
                                        exchange.getIn().setHeader("CamelLuceneQuery", "test query");
                                        exchange.getIn().setHeader("CamelLuceneReturnLuceneDocs", true);
                                    })
                                    .to("lucene:search");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void testCouchdbHeadersMigration() {
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
                                        exchange.getIn().setHeader("CouchDbDatabase", "mydb");
                                        exchange.getIn().setHeader("CouchDbId", "doc1");
                                    })
                                    .to("couchdb:http://localhost:5984");
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
                                        exchange.getIn().setHeader("CamelCouchDbDatabase", "mydb");
                                        exchange.getIn().setHeader("CamelCouchDbId", "doc1");
                                    })
                                    .to("couchdb:http://localhost:5984");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void testCouchbaseHeadersMigration() {
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
                                        exchange.getIn().setHeader("CCB_KEY", "key1");
                                        exchange.getIn().setHeader("CCB_TTL", 3600);
                                    })
                                    .to("couchbase:http://localhost");
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
                                        exchange.getIn().setHeader("CamelCouchbaseKey", "key1");
                                        exchange.getIn().setHeader("CamelCouchbaseTtl", 3600);
                                    })
                                    .to("couchbase:http://localhost");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void testJGroupsRaftHeadersMigration() {
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
                                        exchange.getIn().setHeader("JGROUPSRAFT_SET_OFFSET", 10);
                                        exchange.getIn().setHeader("JGROUPSRAFT_SET_TIMEOUT", 5000);
                                    })
                                    .to("jgroups-raft:cluster");
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
                                        exchange.getIn().setHeader("CamelJGroupsRaftSetOffset", 10);
                                        exchange.getIn().setHeader("CamelJGroupsRaftSetTimeout", 5000);
                                    })
                                    .to("jgroups-raft:cluster");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void testShiroHeadersMigration() {
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
                                        exchange.getIn().setHeader("SHIRO_SECURITY_USERNAME", "admin");
                                        exchange.getIn().setHeader("SHIRO_SECURITY_PASSWORD", "secret");
                                    })
                                    .to("shiro:login");
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
                                        exchange.getIn().setHeader("CamelShiroSecurityUsername", "admin");
                                        exchange.getIn().setHeader("CamelShiroSecurityPassword", "secret");
                                    })
                                    .to("shiro:login");
                            }
                        }
                        """
                )
        );
    }
}
