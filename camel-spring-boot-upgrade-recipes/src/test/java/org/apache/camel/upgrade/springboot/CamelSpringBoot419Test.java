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
package org.apache.camel.upgrade.springboot;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.config.Environment;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class CamelSpringBoot419Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(Environment.builder()
                .scanYamlResources()
                .build()
                .activateRecipes("org.apache.camel.upgrade.camel419.CamelSpringBootMigrationRecipe"));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_junit_5_to_6">JUnit 5 to 6 migration</a>
     */
    @DocumentExample
    @Test
    void junitFiveToSixDependency() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.upgradeJUnit5To6"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-junit5</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-junit6</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    // NOTE: Java package change tests are in the core module where Java parsing is available

    /**
     * Test JUnit 5 to 6 migration for camel-test-main-junit5
     */
    @Test
    void junitFiveToSixMainDependency() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.upgradeJUnit5To6"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-main-junit5</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-main-junit6</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }


    /**
     * Test JUnit 5 to 6 migration for camel-test-spring-junit5
     */
    @Test
    void junitFiveToSixSpringDependency() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.upgradeJUnit5To6"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-spring-junit5</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-spring-junit6</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }


    /**
     * Test JUnit version upgrade to 6.0.3
     */
    @Test
    void junitVersionUpgrade() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.upgradeJUnit5To6"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.junit.jupiter</groupId>
                           <artifactId>junit-jupiter</artifactId>
                           <version>5.10.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.junit.jupiter</groupId>
                           <artifactId>junit-jupiter</artifactId>
                           <version>6.0.3</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }


    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_mdc_older_logic_deprecation">MDC logging migration</a>
     */
    @Test
    void mdcLoggingPropertiesRemoval() {
        //language=properties
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateMdcLoggingSpringBoot"),
                properties(
                """
                camel.main.name=MyApp
                camel.main.useMdcLogging=true
                camel.main.duration-max-seconds=60
                """,
                """
                camel.main.name=MyApp
                camel.main.duration-max-seconds=60
                """
                ));
    }

    /**
     * Test MDC logging property removal from YAML
     */
    @Test
    void mdcLoggingYamlRemoval() {
        //language=yaml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateMdcLoggingSpringBoot"),
                yaml(
                """
                camel:
                  main:
                    name: MyApp
                    useMdcLogging: true
                    duration-max-seconds: 60
                """,
                """
                camel:
                  main:
                    name: MyApp
                    duration-max-seconds: 60
                """
                ));
    }

    /**
     * Test MDC logging property removal with nested YAML structure
     */
    @Test
    void mdcLoggingYamlRemovalNested() {
        //language=yaml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateMdcLoggingSpringBoot"),
                yaml(
                """
                spring:
                  application:
                    name: my-app
                camel:
                  main:
                    useMdcLogging: true
                    name: CamelApp
                logging:
                  level:
                    root: INFO
                """,
                """
                spring:
                  application:
                    name: my-app
                camel:
                  main:
                    name: CamelApp
                logging:
                  level:
                    root: INFO
                """
                ));
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_camel_groovy_xml">camel-groovy-xml-starter migration</a>
     */
    @Test
    void groovyXmlStarterToGroovyStarter() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateGroovyXmlStarter"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-groovy-xml-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-groovy-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Test groovy-xml-starter migration with multiple starters
     */
    @Test
    void groovyXmlStarterToGroovyStarterMultiple() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateGroovyXmlStarter"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-http-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-groovy-xml-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-jackson-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-http-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-groovy-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-jackson-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Test full Spring Boot migration recipe with version upgrades
     */
    @Test
    void fullMigrationUpgradesCamelSpringBootVersion() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-spring-boot-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-spring-boot-starter</artifactId>
                           <version>4.19.0-SNAPSHOT</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Test artifact name changes together (without version upgrades)
     * Note: Version upgrades are tested separately to avoid Maven resolution issues with unreleased versions
     */
    @Test
    void comprehensiveMigrationWithMultipleChanges() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources(
                        "org.apache.camel.upgrade.camel419.migrateGroovyXmlStarter",
                        "org.apache.camel.upgrade.camel419.upgradeJUnit5To6"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-spring-boot-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-groovy-xml-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-spring-junit5</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-spring-boot-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-groovy-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-test-spring-junit6</artifactId>
                           <version>4.18.0</version>
                           <scope>test</scope>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Edge case: Properties file without MDC logging property should not change
     */
    @Test
    void mdcLoggingPropertiesNoChange() {
        //language=properties
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateMdcLoggingSpringBoot"),
                properties(
                """
                camel.main.name=MyApp
                camel.main.duration-max-seconds=60
                """
                ));
    }

    /**
     * Edge case: YAML file without MDC logging property should not change
     */
    @Test
    void mdcLoggingYamlNoChange() {
        //language=yaml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateMdcLoggingSpringBoot"),
                yaml(
                """
                camel:
                  main:
                    name: MyApp
                    duration-max-seconds: 60
                """
                ));
    }

    /**
     * Edge case: POM without groovy-xml-starter should not change
     */
    @Test
    void groovyXmlStarterNoChange() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateGroovyXmlStarter"),
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel.springboot</groupId>
                           <artifactId>camel-http-starter</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }
}
