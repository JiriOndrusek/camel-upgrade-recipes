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

import org.apache.camel.upgrade.camel420.*;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate420Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.apache.camel.upgrade.camel420.CamelMigrationRecipe")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(false))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     */
    @Test
    void pulsarPersistentV1toV2() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/cluster1/default/my-topic")
                            .to("mock:result");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/default/my-topic")
                            .to("mock:result");
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     */
    @Test
    void pulsarNonPersistentV1toV2() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:non-persistent://tenant1/cluster2/namespace1/topic1")
                            .to("mock:result");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:non-persistent://tenant1/namespace1/topic1")
                            .to("mock:result");
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     */
    @Test
    void pulsarWithQueryParameters() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/cluster1/default/my-topic?numberOfConsumers=5&subscriptionType=Shared")
                            .to("mock:result");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/default/my-topic?numberOfConsumers=5&subscriptionType=Shared")
                            .to("mock:result");
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     */
    @Test
    void pulsarTopicWithSlashesReplacedByHyphens() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/cluster1/default/my-topic/sub-path")
                            .to("mock:result");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/default/my-topic-sub-path")
                            .to("mock:result");
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     */
    @Test
    void pulsarTopicWithSlashesAndQueryParameters() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://tenant/cluster/ns/topic/path/more?subscriptionName=sub1")
                            .to("mock:result");
                    }
                }
                """,
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://tenant/ns/topic-path-more?subscriptionName=sub1")
                            .to("mock:result");
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     * V2 format (3 segments) should not be changed
     */
    @Test
    void pulsarV2FormatUnchanged() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("pulsar:persistent://public/default/my-topic")
                            .to("mock:result");
                    }
                }
                """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
     * Non-pulsar URIs should not be changed
     */
    @Test
    void nonPulsarUriUnchanged() {
        //language=java
        rewriteRun(java(
                """
                import org.apache.camel.builder.RouteBuilder;

                public class MyRoute extends RouteBuilder {
                    @Override
                    public void configure() {
                        from("kafka:my-topic")
                            .to("mock:result");
                    }
                }
                """));
    }
}
