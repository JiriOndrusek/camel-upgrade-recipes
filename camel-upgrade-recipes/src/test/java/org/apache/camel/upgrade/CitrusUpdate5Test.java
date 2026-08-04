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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

public class CitrusUpdate5Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource(
                "/META-INF/rewrite/citrus5.yaml",
                "org.apache.camel.upgrade.citrus5.CamelCitrusMigrationRecipe")
          .typeValidationOptions(TypeValidation.none());
    }

    @Disabled("This test is dependent on the user settings.xml, if multiple repositories in the settings xml are" +
            " configured, the test fails")
    @Test
    void citrusArtifactRename() {
        //language=xml
        rewriteRun(pomXml(
          """
            <project>
               <modelVersion>4.0.0</modelVersion>
               <artifactId>test</artifactId>
               <groupId>com.example</groupId>
               <version>1.0.0</version>
               <dependencies>
                   <dependency>
                       <groupId>org.citrusframework</groupId>
                       <artifactId>citrus-junit5</artifactId>
                       <version>4.4.0</version>
                   </dependency>
               </dependencies>
            </project>
            """,
          """
            <project>
               <modelVersion>4.0.0</modelVersion>
               <artifactId>test</artifactId>
               <groupId>com.example</groupId>
               <version>1.0.0</version>
               <dependencies>
                   <dependency>
                       <groupId>org.citrusframework</groupId>
                       <artifactId>citrus-junit-jupiter</artifactId>
                       <version>4.4.0</version>
                   </dependency>
               </dependencies>
            </project>
            """));
    }

    @Test
    void camelJBangClassRenames() {
        //language=java
        rewriteRun(
          java(
            """
              package org.citrusframework.camel.jbang;
              public class CamelJBang {}
              """,
            """
              package org.citrusframework.camel.cli;
              public class CamelCli {}
              """),
          java(
            """
              package org.citrusframework.camel.jbang;
              public class CamelJBangSettings {}
              """,
            """
              package org.citrusframework.camel.cli;
              public class CamelCliSettings {}
              """),
          java(
            """
              import org.citrusframework.camel.jbang.CamelJBang;
              import org.citrusframework.camel.jbang.CamelJBangSettings;

              public class MyCitrusTest {
                  public void test() {
                      CamelJBang jbang = null;
                      CamelJBangSettings settings = null;
                  }
              }
              """,
            """
              import org.citrusframework.camel.cli.CamelCli;
              import org.citrusframework.camel.cli.CamelCliSettings;

              public class MyCitrusTest {
                  public void test() {
                      CamelCli jbang = null;
                      CamelCliSettings settings = null;
                  }
              }
              """));
    }

    @Test
    void camelJBangTestActorRename() {
        //language=java
        rewriteRun(
          java(
            """
              package org.citrusframework.camel.jbang;
              public class CamelJBangTestActor {}
              """,
            """
              package org.citrusframework.camel.cli;
              public class CamelCliTestActor {}
              """),
          java(
            """
              import org.citrusframework.camel.jbang.CamelJBangTestActor;

              public class MyCitrusTest {
                  public void test() {
                      CamelJBangTestActor actor = null;
                  }
              }
              """,
            """
              import org.citrusframework.camel.cli.CamelCliTestActor;

              public class MyCitrusTest {
                  public void test() {
                      CamelCliTestActor actor = null;
                  }
              }
              """));
    }

    @Test
    void abstractCamelJBangActionRename() {
        //language=java
        rewriteRun(
          java(
            """
              package org.citrusframework.camel.actions;
              public abstract class AbstractCamelJBangAction {}
              """,
            """
              package org.citrusframework.camel.actions;
              public abstract class AbstractCamelCliAction {}
              """),
          java(
            """
              import org.citrusframework.camel.actions.AbstractCamelJBangAction;

              public class MyAction extends AbstractCamelJBangAction {
              }
              """,
            """
              import org.citrusframework.camel.actions.AbstractCamelCliAction;

              public class MyAction extends AbstractCamelCliAction {
              }
              """));
    }

    @Test
    void camelCliXmlDsl() {
        //language=xml
        rewriteRun(xml(
          """
            <camel>
              <jbang camel-version="${camelVersion}">
                <run>
                  <integration name="myRoute"
                               file="classpath:my-route.yaml"/>
                </run>
              </jbang>
            </camel>
            """,
          """
            <camel>
              <cli camel-version="${camelVersion}">
                <run>
                  <integration name="myRoute"
                               file="classpath:my-route.yaml"/>
                </run>
              </cli>
            </camel>
            """));
    }

    @Test
    void camelCliYamlDsl() {
        //language=yaml
        rewriteRun(yaml(
          """
            - camel:
                jbang:
                  run:
                    integration:
                      name: "myRoute"
                      file: "classpath:my-route.yaml"
            """,
          """
            - camel:
                cli:
                  run:
                    integration:
                      name: "myRoute"
                      file: "classpath:my-route.yaml"
            """));
    }

    @Test
    void camelCliProperties() {
        //language=properties
        rewriteRun(properties(
          """
            citrus.camel.jbang.version=4.8.0
            citrus.camel.jbang.work.dir=/tmp/citrus
            citrus.camel.jbang.app=camel
            citrus.camel.jbang.kamelets.version=4.8.0
            citrus.camel.jbang.trust.url=https://example.com
            citrus.camel.jbang.verbose=true
            citrus.camel.jbang.auto.remove.resources=true
            citrus.camel.jbang.auto.remove.plugins=false
            citrus.camel.jbang.dump.integration.output=true
            citrus.camel.jbang.wait.for.running.state=true
            """,
          """
            citrus.camel.cli.version=4.8.0
            citrus.camel.cli.work.dir=/tmp/citrus
            citrus.camel.cli.app=camel
            citrus.camel.cli.kamelets.version=4.8.0
            citrus.camel.cli.trust.url=https://example.com
            citrus.camel.cli.verbose=true
            citrus.camel.cli.auto.remove.resources=true
            citrus.camel.cli.auto.remove.plugins=false
            citrus.camel.cli.dump.integration.output=true
            citrus.camel.cli.wait.for.running.state=true
            """));
    }
}
