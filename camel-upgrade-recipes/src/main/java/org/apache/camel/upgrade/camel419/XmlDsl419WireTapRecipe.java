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
package org.apache.camel.upgrade.camel419;

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_wiretap_eip">WireTap EIP</a>
 * </p>
 * The WireTap EIP pattern option has been removed.
 */
public class XmlDsl419WireTapRecipe extends Recipe {

    private static final XPathMatcher WIRETAP_MATCHER = new XPathMatcher("//wireTap");

    @Override
    public String getDisplayName() {
        return "Camel XML DSL WireTap pattern removal";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 4.18 to 4.19. Removes the unused pattern attribute from wireTap elements.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelXmlVisitor() {

            @Override
            public Xml.Tag doVisitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                if (WIRETAP_MATCHER.matches(getCursor())) {
                    List<Xml.Attribute> attributes = new ArrayList<>(t.getAttributes());
                    boolean removed = attributes.removeIf(a -> "pattern".equals(a.getKeyAsString()));

                    if (removed) {
                        t = t.withAttributes(attributes);
                    }
                }

                return t;
            }
        };
    }
}
