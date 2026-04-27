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
package org.apache.camel.upgrade.customRecipes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for transforming component URIs using regular expressions with capturing groups.
 */
public class ComponentUriTransformer {

    /**
     * Transform a URI string using the provided pattern and replacement.
     *
     * @param uri The original URI string
     * @param pattern The compiled regex pattern to match
     * @param replacement The replacement string using ${1}, ${2}, etc. for capturing groups
     * @return The transformed URI, or null if the pattern doesn't match
     */
    public static String transformUri(String uri, Pattern pattern, String replacement) {
        Matcher matcher = pattern.matcher(uri);
        if (!matcher.matches()) {
            return null;
        }

        String result = replacement;

        // Replace all ${n} placeholders with corresponding capturing groups
        for (int i = 0; i <= matcher.groupCount(); i++) {
            String groupValue = matcher.group(i);
            if (groupValue != null) {
                result = result.replace("${" + i + "}", groupValue);
            }
        }

        // Apply component-specific transformations
        result = applyComponentSpecificTransformations(uri, result);

        return result;
    }

    /**
     * Apply component-specific transformations after the main pattern replacement.
     * This method handles special cases like slash replacement in Pulsar topic names.
     *
     * @param originalUri The original URI (used to detect the component type)
     * @param transformedUri The URI after pattern replacement
     * @return The URI after component-specific transformations
     */
    private static String applyComponentSpecificTransformations(String originalUri, String transformedUri) {
        // Only apply transformations if the URI actually changed
        if (originalUri.equals(transformedUri)) {
            return transformedUri;
        }

        // Pulsar: Replace forward slashes in topic names with hyphens
        if (originalUri.startsWith("pulsar:")) {
            return transformPulsarTopicSlashes(transformedUri);
        }

        return transformedUri;
    }

    /**
     * Transform forward slashes to hyphens in Pulsar topic names.
     * Pulsar V2 format doesn't support slashes in topic names.
     *
     * @param uri The Pulsar URI
     * @return The URI with slashes replaced by hyphens in the topic name
     */
    private static String transformPulsarTopicSlashes(String uri) {
        // Pattern to extract: prefix (scheme://tenant/namespace/) + topic + suffix (query params)
        // Group 1: pulsar:(persistent|non-persistent)://tenant/namespace/
        // Group 2: topic name
        // Group 3: query parameters (optional)
        Pattern topicPattern = Pattern.compile(
            "^(pulsar:(?:persistent|non-persistent)://[^/]+/[^/]+/)([^?]+)(.*)$"
        );

        Matcher topicMatcher = topicPattern.matcher(uri);
        if (topicMatcher.matches()) {
            String prefix = topicMatcher.group(1);
            String topic = topicMatcher.group(2);
            String suffix = topicMatcher.group(3);

            // Replace slashes with hyphens in the topic name only
            topic = topic.replace('/', '-');

            return prefix + topic + suffix;
        }

        return uri;
    }
}
