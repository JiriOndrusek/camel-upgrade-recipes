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
import static org.openrewrite.maven.Assertions.pomXml;

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
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_camel_groovy_xml">camel-groovy-xml migration</a>
     */
    @DocumentExample
    @Test
    void groovyXmlToGroovy() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateGroovyXml"),
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
                           <artifactId>camel-groovy-xml</artifactId>
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
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-groovy</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """));
    }

    /**
     * Test groovy-xml migration with multiple dependencies in pom
     */
    @Test
    void groovyXmlToGroovyWithMultipleDependencies() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateGroovyXml"),
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
                           <artifactId>camel-core</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-groovy-xml</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-http</artifactId>
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
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-core</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-groovy</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-http</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """));
    }

    /**
     * Test that pom without groovy-xml dependency is not modified
     */
    @Test
    void groovyXmlToGroovyNoChange() {
        //language=xml
        rewriteRun(spec -> spec.recipeFromResources("org.apache.camel.upgrade.camel419.migrateGroovyXml"),
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
                           <artifactId>camel-core</artifactId>
                           <version>4.18.0</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Test full migration recipe upgrades Camel version
     */
    @Test
    void fullMigrationUpgradesCamelVersion() {
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
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-core</artifactId>
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
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-core</artifactId>
                           <version>4.19.0-SNAPSHOT</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Test full migration recipe with camel.version property
     */
    @Test
    void fullMigrationUpgradesCamelVersionProperty() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <properties>
                       <camel.version>4.18.0</camel.version>
                   </properties>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-core</artifactId>
                           <version>${camel.version}</version>
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

                   <properties>
                       <camel.version>4.19.0-SNAPSHOT</camel.version>
                   </properties>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-core</artifactId>
                           <version>${camel.version}</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }

    /**
     * Test full migration upgrades Camel plugin version
     */
    @Test
    void fullMigrationUpgradesCamelPlugin() {
        //language=xml
        rewriteRun(
                pomXml(
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <build>
                       <plugins>
                           <plugin>
                               <groupId>org.apache.camel</groupId>
                               <artifactId>camel-maven-plugin</artifactId>
                               <version>4.18.0</version>
                           </plugin>
                       </plugins>
                   </build>
                </project>
                """,
                """
                <project>
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <build>
                       <plugins>
                           <plugin>
                               <groupId>org.apache.camel</groupId>
                               <artifactId>camel-maven-plugin</artifactId>
                               <version>4.19.0-SNAPSHOT</version>
                           </plugin>
                       </plugins>
                   </build>
                </project>
                """
                ));
    }

    /**
     * Test combined migration - groovy-xml dependency and version upgrade
     */
    @Test
    void fullMigrationWithGroovyXml() {
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
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-groovy-xml</artifactId>
                           <version>4.18.0</version>
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
                   <modelVersion>4.0.0</modelVersion>

                   <artifactId>test</artifactId>
                   <groupId>org.apache.camel.test</groupId>
                   <version>1.0.0</version>

                   <dependencies>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-groovy</artifactId>
                           <version>4.19.0-SNAPSHOT</version>
                       </dependency>
                       <dependency>
                           <groupId>org.apache.camel</groupId>
                           <artifactId>camel-core</artifactId>
                           <version>4.19.0-SNAPSHOT</version>
                       </dependency>
                   </dependencies>
                </project>
                """
                ));
    }
}
