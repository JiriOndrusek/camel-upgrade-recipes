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
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

public class CamelUpdate422Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_22, true)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_21,
                        "camel-core-model", "camel-api", "camel-azure-storage-blob", "minio"))
                .typeValidationOptions(TypeValidation.none())
                .expectedCyclesThatMakeChanges(1);
    }

    @DocumentExample
    @Test
    void migrateAzureStorageBlobCredentialType() {
        //language=java
        rewriteRun(
                java(
                        """
                        import org.apache.camel.component.azure.storage.blob.CredentialType;

                        public class BlobExample {
                            public void example() {
                                CredentialType type = CredentialType.SHARED_KEY_CREDENTIAL;
                            }
                        }
                        """,
                        """
                        import org.apache.camel.component.azure.common.CredentialType;

                        public class BlobExample {
                            public void example() {
                                CredentialType type = CredentialType.SHARED_KEY_CREDENTIAL;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void migrateAwsApacheClient() {
        //language=xml
        rewriteRun(
                pomXml(
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>software.amazon.awssdk</groupId>
                                    <artifactId>apache-client</artifactId>
                                    <version>2.46.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """,
                        """
                        <project>
                            <groupId>com.example</groupId>
                            <artifactId>test</artifactId>
                            <version>1.0.0</version>
                            <properties>
                                <maven.compiler.release>17</maven.compiler.release>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>software.amazon.awssdk</groupId>
                                    <artifactId>apache5-client</artifactId>
                                    <version>2.46.0</version>
                                </dependency>
                            </dependencies>
                        </project>
                        """
                )
        );
    }

    @Test
    void migrateMinioCopySource() {
        //language=java
        rewriteRun(
                mavenProject("test-minio",
                        CamelTestUtil.pomXmlSpec("camel-minio", CamelTestUtil.CamelVersion.v4_21),
                        java(
                                """
                                import io.minio.CopySource;

                                public class MinioExample {
                                    public void example() {
                                        CopySource src = null;
                                    }
                                }
                                """,
                                """
                                import io.minio.SourceObject;

                                public class MinioExample {
                                    public void example() {
                                        SourceObject src = null;
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void preconditionBlocksMinioWithoutDependency() {
        //language=java
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(CamelTestUtil.isRecipeOverridden() ? 1 : 0),
                mavenProject("test-negative",
                        CamelTestUtil.pomXmlSpec("camel-core", CamelTestUtil.CamelVersion.v4_21),
                        java(
                                """
                                import io.minio.CopySource;

                                public class NegativeMinioExample {
                                    public void example() {
                                        CopySource src = null;
                                    }
                                }
                                """
                        )
                )
        );
    }

}
