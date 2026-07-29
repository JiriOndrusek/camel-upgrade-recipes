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
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.properties.Assertions.properties;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate422Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_22, true)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_21,
                        "camel-core-model", "camel-api", "camel-azure-storage-blob"))
                .typeValidationOptions(TypeValidation.none())
                .expectedCyclesThatMakeChanges(1);
    }

    // ===== camel-microprofile-fault-tolerance: timeoutPoolSize removed =====

    @DocumentExample
    @Test
    void removeResilience4jTimeoutPoolSizeProperty() {
        //language=properties
        rewriteRun(
                properties(
                """
                camel.resilience4j.timeout-pool-size=10
                camel.resilience4j.timeout-enabled=true
                """,
                """
                camel.resilience4j.timeout-enabled=true
                """,
                spec -> spec.path("application.properties")
                )
        );
    }

    @Test
    void removeResilience4jTimeoutPoolSizeCamelCaseProperty() {
        //language=properties
        rewriteRun(
                properties(
                """
                camel.resilience4j.timeoutPoolSize=10
                camel.resilience4j.timeoutEnabled=true
                """,
                """
                camel.resilience4j.timeoutEnabled=true
                """,
                spec -> spec.path("application.properties")
                )
        );
    }

    // ===== camel-azure: component-specific CredentialType enums removed =====

    @Test
    void migrateAzureStorageBlobCredentialType() {
        //language=java
        rewriteRun(
                mavenProject("test-azure-storage-blob",
                        CamelTestUtil.pomXmlSpec("camel-azure-storage-blob", CamelTestUtil.CamelVersion.v4_21),
                        java(
                        """
                        import org.apache.camel.component.azure.storage.blob.CredentialType;

                        class Test {
                            void configure() {
                                CredentialType type = CredentialType.SHARED_ACCOUNT_KEY;
                            }
                        }
                        """,
                        """
                        import org.apache.camel.component.azure.common.CredentialType;

                        class Test {
                            void configure() {
                                CredentialType type = CredentialType.SHARED_ACCOUNT_KEY;
                            }
                        }
                        """
                        )
                )
        );
    }

    // ===== camel-minio: Upgraded to minio 9.0.3 - Breaking Changes =====

    @Test
    void migrateMinioServerSideEncryptionCustomerKey() {
        //language=java
        // OpenRewrite resolves the inner class name to its simple form without the outer class qualifier
        rewriteRun(
                spec -> spec.parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_21,
                        "camel-core-model", "camel-api", "camel-azure-storage-blob", "minio")),
                mavenProject("test-minio",
                        CamelTestUtil.pomXmlSpec("camel-minio", CamelTestUtil.CamelVersion.v4_21),
                        java(
                        """
                        import io.minio.ServerSideEncryptionCustomerKey;

                        class Test {
                            void configure() {
                                ServerSideEncryptionCustomerKey ssec = null;
                            }
                        }
                        """,
                        """
                        class Test {
                            void configure() {
                                CustomerKey ssec = null;
                            }
                        }
                        """
                        )
                )
        );
    }

    @Test
    void migrateMinioHttpMethod() {
        //language=java
        // OpenRewrite resolves io.minio.http.Method → io.minio.Http.Method using inner class simple name
        rewriteRun(
                spec -> spec.parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_21,
                        "camel-core-model", "camel-api", "camel-azure-storage-blob", "minio")),
                mavenProject("test-minio-method",
                        CamelTestUtil.pomXmlSpec("camel-minio", CamelTestUtil.CamelVersion.v4_21),
                        java(
                        """
                        import io.minio.http.Method;

                        class Test {
                            void configure() {
                                Method m = Method.GET;
                            }
                        }
                        """,
                        """
                        class Test {
                            void configure() {
                                Method m = Method.GET;
                            }
                        }
                        """
                        )
                )
        );
    }

    // ===== camel-debezium: Oracle connector options removed =====

    @Test
    void removeDebeziumOracleOptionsProperties() {
        //language=properties
        rewriteRun(
                mavenProject("test-debezium",
                        CamelTestUtil.pomXmlSpec("camel-debezium-oracle", CamelTestUtil.CamelVersion.v4_21),
                        properties(
                        """
                        camel.component.debezium-oracle.log-mining-batch-size-default=100
                        camel.component.debezium-oracle.log-mining-batch-size-max=1000
                        camel.component.debezium-oracle.log-mining-batch-size-min=10
                        camel.component.debezium-oracle.log-mining-scn-gap-detection-gap-size-min=1000000
                        camel.component.debezium-oracle.log-mining-scn-gap-detection-time-interval-max-ms=60000
                        camel.component.debezium-oracle.host=mydb.example.com
                        """,
                        """
                        camel.component.debezium-oracle.host=mydb.example.com
                        """,
                        spec -> spec.path("application.properties")
                        )
                )
        );
    }

    // ===== Precondition guard: Azure recipe does not fire without Azure dependency =====

    @DisabledIfSystemProperty(named = CamelTestUtil.PROPERTY_USE_RECIPE, matches = ".+")
    @Test
    void preconditionBlocksAzureCredentialTypeWithoutDependency() {
        // Without a pom.xml (no Maven module), the ModuleHasDependency precondition blocks the recipe.
        // This verifies that the Azure CredentialType recipe only fires when an Azure component is on the classpath.
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(0),
                java(
                """
                import org.apache.camel.component.azure.storage.blob.CredentialType;

                class Test {
                    void configure() {
                        CredentialType type = CredentialType.SHARED_ACCOUNT_KEY;
                    }
                }
                """
                )
        );
    }
}
