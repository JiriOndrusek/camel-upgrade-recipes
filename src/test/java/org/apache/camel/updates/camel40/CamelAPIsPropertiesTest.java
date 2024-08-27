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
package org.apache.camel.updates.camel40;

import org.apache.camel.updates.AbstractUpdateTest;
import org.apache.camel.updates.CamelTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelAPIsPropertiesTest extends AbstractUpdateTest {

    @Override
    protected CamelTestUtil.CamelVersion recipe() {
        return CamelTestUtil.CamelVersion.v4_0;
    }

    @Override
    protected Parser.Builder parser() {
        return JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true);
    }

    @Test
    public void testRejectedPolicyDiscardOldeste() {
        rewriteRun(Assertions.properties("""
                   #test
                   camel.threadpool.rejectedPolicy=DiscardOldest
                """,
                """
                            #test
                            camel.threadpool.rejectedPolicy=Abort #DiscardOldest has been removed, consider Abort
                        """));
    }

    @Test
    public void testRejectedPolicyDiscard() {
        rewriteRun(Assertions.properties("""
                   #test
                   camel.threadpool.rejectedPolicy=Discard
                """,
                """
                            #test
                            camel.threadpool.rejectedPolicy=Abort #Discard has been removed, consider Abort
                        """));
    }

}
