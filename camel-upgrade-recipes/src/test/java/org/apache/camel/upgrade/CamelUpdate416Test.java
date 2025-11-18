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
import static org.openrewrite.xml.Assertions.xml;
import static org.openrewrite.yaml.Assertions.yaml;

//class has to stay public, because test is extended in project quarkus-updates
public class CamelUpdate416Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, CamelTestUtil.CamelVersion.v4_16)
                .parser(CamelTestUtil.parserFromClasspath(CamelTestUtil.CamelVersion.v4_15,
                        "camel-api",
                        "camel-core-model", "camel-support", "camel-base-engine", "camel-base"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_16.html#_camel_flink">camel-flink</a>
     */
    @Test
    void flink() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.builder.RouteBuilder;
                  
                  public class FlinkTest extends RouteBuilder {
                      @Override
                      public void configure()  {
                          from("direct:start")
                              .to("flink:dataset?dataSet=#myDataSet&dataSetCallback=#myCallback");
                      }
                  }
                  """,
                """
                  import org.apache.camel.builder.RouteBuilder;
                  
                  public class FlinkTest extends RouteBuilder {
                      @Override
                      public void configure()  {
                          from("direct:start")
                              .to("flink:datastream?dataStream=#myDataStream&dataStreamCallback=#myCallback");
                      }
                  }
                  """));
    }


}
