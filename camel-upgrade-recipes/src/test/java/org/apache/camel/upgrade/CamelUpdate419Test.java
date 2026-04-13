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

import org.apache.camel.upgrade.camel419.*;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate419Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.apache.camel.upgrade.camel419.CamelMigrationRecipe")
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_18,
                        "camel-core-model", "camel-api"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_wiretap_eip">WireTap EIP</a>
     */
    @DocumentExample
    @Test
    void wireTapPatternXml() {
        //language=xml
        rewriteRun(xml(
          """
            <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
                <route>
                    <from uri="direct:start"/>
                    <wireTap uri="direct:tap" pattern="InOnly">
                        <body><constant>tapped</constant></body>
                    </wireTap>
                    <to uri="mock:result"/>
                </route>
            </camelContext>
            """,
          """
            <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
                <route>
                    <from uri="direct:start"/>
                    <wireTap uri="direct:tap">
                        <body><constant>tapped</constant></body>
                    </wireTap>
                    <to uri="mock:result"/>
                </route>
            </camelContext>
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_wiretap_eip">WireTap EIP</a>
     */
    @Test
    void wireTapPatternYaml() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route:
                from:
                  uri: direct:start
                steps:
                  - wireTap:
                      uri: direct:tap
                      pattern: InOnly
                  - to:
                      uri: mock:result
            """,
          """
            - route:
                from:
                  uri: direct:start
                steps:
                  - wireTap:
                      uri: direct:tap
                  - to:
                      uri: mock:result
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
     */
    @Test
    void sagaEipXml() {
        //language=xml
        rewriteRun(xml(
          """
            <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
                <route>
                    <from uri="direct:start"/>
                    <saga>
                        <compensation>direct:compensate</compensation>
                        <completion>direct:complete</completion>
                        <to uri="direct:action"/>
                    </saga>
                </route>
            </camelContext>
            """,
          """
            <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
                <route>
                    <from uri="direct:start"/>
                    <saga compensation="direct:compensate" completion="direct:complete">
                        <to uri="direct:action"/>
                    </saga>
                </route>
            </camelContext>
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
     */
    @Test
    void sagaEipYaml() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route:
                from:
                  uri: direct:start
                steps:
                  - saga:
                      compensation:
                        uri: direct:compensate
                      completion:
                        uri: direct:complete
                      steps:
                        - to:
                            uri: direct:action
            """,
          """
            - route:
                from:
                  uri: direct:start
                steps:
                  - saga:
                      compensation: direct:compensate
                      completion: direct:complete
                      steps:
                        - to:
                            uri: direct:action
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_yaml_dsl">YAML DSL</a>
     */
    @Test
    void yamlRoutePolicyRename() {
        //language=yaml
        rewriteRun(yaml(
          """
            - route:
                id: myRoute
                routePolicy: myPolicy
                from:
                  uri: direct:start
                steps:
                  - to:
                      uri: mock:result
            """,
          """
            - route:
                id: myRoute
                routePolicyRef: myPolicy
                from:
                  uri: direct:start
                steps:
                  - to:
                      uri: mock:result
            """));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_camel_test_infra">camel-test-infra</a>
     */
    @Test
    void testInfraTestJarRemoval() {
        //language=xml
        rewriteRun(spec -> spec.recipe(new Pom419TestInfraRecipe()),
          pomXml(
          """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0</version>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.camel</groupId>
                        <artifactId>camel-test-infra-kafka</artifactId>
                        <version>4.18.0</version>
                        <type>test-jar</type>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.camel</groupId>
                        <artifactId>camel-test-infra-common</artifactId>
                        <version>4.18.0</version>
                        <type>test-jar</type>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.camel</groupId>
                        <artifactId>camel-core</artifactId>
                        <version>4.18.0</version>
                    </dependency>
                </dependencies>
            </project>
            """,
          """
            <project>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0</version>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.camel</groupId>
                        <artifactId>camel-test-infra-kafka</artifactId>
                        <version>4.18.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.camel</groupId>
                        <artifactId>camel-test-infra-common</artifactId>
                        <version>4.18.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.camel</groupId>
                        <artifactId>camel-core</artifactId>
                        <version>4.18.0</version>
                    </dependency>
                </dependencies>
            </project>
            """));
    }

}
