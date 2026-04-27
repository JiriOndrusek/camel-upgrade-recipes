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
package org.apache.camel.upgrade.camel420;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_20.html#_camel_pulsar">camel-pulsar</a>
 * </p>
 * Migrates Pulsar component URIs from V1 format (4 path segments) to V2 format (3 path segments).
 * Per PIP-457, V1 topic names are no longer supported.
 *
 * Transforms:
 * - V1: pulsar:persistent://tenant/cluster/namespace/topic
 * - V2: pulsar:persistent://tenant/namespace/topic (removes cluster segment)
 *
 * Also replaces forward slashes in topic names with hyphens.
 */
@EqualsAndHashCode(callSuper = false)
@Value
public class Pulsar420Recipe extends Recipe {

    // Pattern to match pulsar URIs with V1 format (4 path segments after persistent://)
    // Groups: 1=scheme(persistent/non-persistent), 2=tenant, 3=cluster, 4=namespace, 5=topic+params
    private static final Pattern PULSAR_V1_PATTERN = Pattern.compile(
            "^pulsar:((persistent|non-persistent)://([^/]+)/([^/]+)/([^/]+)/(.+))$");

    @Override
    public String getDisplayName() {
        return "Camel Pulsar URI migration from V1 to V2 format";
    }

    @Override
    public String getDescription() {
        return "Migrates Apache Camel Pulsar component URIs from V1 format (persistent://tenant/cluster/namespace/topic) " +
               "to V2 format (persistent://tenant/namespace/topic) as required by PIP-457. " +
               "Also replaces forward slashes in topic names with hyphens.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelJavaVisitor() {

            @Override
            protected J.Literal doVisitLiteral(J.Literal literal, ExecutionContext ctx) {
                J.Literal l = super.doVisitLiteral(literal, ctx);

                // Only handle String literals
                if (JavaType.Primitive.String == l.getType() && l.getValue() != null) {
                    String value = (String) l.getValue();
                    Matcher matcher = PULSAR_V1_PATTERN.matcher(value);

                    if (matcher.matches()) {
                        String scheme = matcher.group(2);  // persistent or non-persistent
                        String tenant = matcher.group(3);
                        // String cluster = matcher.group(4);  // This is removed in V2
                        String namespace = matcher.group(5);
                        String topicAndParams = matcher.group(6);

                        // Split topic from any query parameters
                        String topic;
                        String params = "";
                        int queryIndex = topicAndParams.indexOf('?');
                        if (queryIndex > 0) {
                            topic = topicAndParams.substring(0, queryIndex);
                            params = topicAndParams.substring(queryIndex);
                        } else {
                            topic = topicAndParams;
                        }

                        // Replace forward slashes in topic name with hyphens
                        topic = topic.replace('/', '-');

                        // Construct V2 URI (3 path segments: tenant/namespace/topic)
                        String v2Uri = String.format("pulsar:%s://%s/%s/%s%s",
                                scheme, tenant, namespace, topic, params);

                        return RecipesUtil.createStringLiteral(v2Uri).withPrefix(literal.getPrefix());
                    }
                }

                return l;
            }
        };
    }
}
