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
import java.util.Optional;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_19.html#_saga_eip">Saga EIP</a>
 * </p>
 * Changed model for configuring completion and compensation URIs in XML DSL.
 * Converts child elements to attributes.
 */
public class XmlDsl419SagaRecipe extends Recipe {

    private static final XPathMatcher SAGA_MATCHER = new XPathMatcher("//saga");

    @Override
    public String getDisplayName() {
        return "Camel XML DSL Saga EIP restructuring";
    }

    @Override
    public String getDescription() {
        return "Apache Camel XML DSL migration from version 4.18 to 4.19. Converts saga compensation and completion child elements to attributes.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelXmlVisitor() {

            @Override
            public Xml.Tag doVisitTag(final Xml.Tag tag, final ExecutionContext ctx) {
                Xml.Tag t = super.doVisitTag(tag, ctx);

                if (SAGA_MATCHER.matches(getCursor())) {
                    List<Xml.Tag> children = new ArrayList<>(t.getChildren());
                    List<Xml.Attribute> attributes = new ArrayList<>(t.getAttributes());

                    // Find and remove compensation child element, add as attribute
                    Optional<Xml.Tag> compensationTag = children.stream()
                            .filter(child -> "compensation".equals(child.getName()))
                            .findFirst();

                    if (compensationTag.isPresent() && compensationTag.get().getValue().isPresent()) {
                        String compensationUri = compensationTag.get().getValue().get().trim();
                        children.removeIf(child -> "compensation".equals(child.getName()));

                        // Add compensation attribute if not already present
                        if (attributes.stream().noneMatch(a -> "compensation".equals(a.getKeyAsString()))) {
                            attributes.add(new Xml.Attribute(
                                    org.openrewrite.Tree.randomId(),
                                    " ",
                                    org.openrewrite.marker.Markers.EMPTY,
                                    new Xml.Ident(org.openrewrite.Tree.randomId(), "", org.openrewrite.marker.Markers.EMPTY, "compensation"),
                                    "",
                                    new Xml.Attribute.Value(
                                            org.openrewrite.Tree.randomId(),
                                            "",
                                            org.openrewrite.marker.Markers.EMPTY,
                                            Xml.Attribute.Value.Quote.Double,
                                            compensationUri
                                    )
                            ));
                        }
                    }

                    // Find and remove completion child element, add as attribute
                    Optional<Xml.Tag> completionTag = children.stream()
                            .filter(child -> "completion".equals(child.getName()))
                            .findFirst();

                    if (completionTag.isPresent() && completionTag.get().getValue().isPresent()) {
                        String completionUri = completionTag.get().getValue().get().trim();
                        children.removeIf(child -> "completion".equals(child.getName()));

                        // Add completion attribute if not already present
                        if (attributes.stream().noneMatch(a -> "completion".equals(a.getKeyAsString()))) {
                            attributes.add(new Xml.Attribute(
                                    org.openrewrite.Tree.randomId(),
                                    " ",
                                    org.openrewrite.marker.Markers.EMPTY,
                                    new Xml.Ident(org.openrewrite.Tree.randomId(), "", org.openrewrite.marker.Markers.EMPTY, "completion"),
                                    "",
                                    new Xml.Attribute.Value(
                                            org.openrewrite.Tree.randomId(),
                                            "",
                                            org.openrewrite.marker.Markers.EMPTY,
                                            Xml.Attribute.Value.Quote.Double,
                                            completionUri
                                    )
                            ));
                        }
                    }

                    if (compensationTag.isPresent() || completionTag.isPresent()) {
                        t = t.withContent(children);
                        t = t.withAttributes(attributes);
                    }
                }

                return t;
            }
        };
    }
}
