package org.apache.camel.upgrade.camel415;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.upgrade.AbstractCamelJavaVisitor;
import org.apache.camel.upgrade.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_16.html#_subscription_monitoring_api_changes">Java Milo Subscription API changes</a>
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class Camel415MiloLambdaRecipe extends Recipe {

    private static final MethodMatcher MATCHER =
            new MethodMatcher("org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem setValueConsumer(..)");

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Milo: The monitored item data value listener API has changed";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Milo: The monitored item data value listener API has changed.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return RecipesUtil.newVisitor(new AbstractCamelJavaVisitor() {

            private final JavaTemplate template = JavaTemplate.builder(
                    "#{any(org.optaplanner.core.api.score.stream.ConstraintStream)}" +
                            ".penalize(#{any(org.optaplanner.core.api.score.Score)})" +
                            ".asConstraint(#{any(java.lang.String)})"
            ).build();

            @Override
            public J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation methodInvocation, ExecutionContext executionContext) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(methodInvocation, executionContext);
                if (MATCHER.matches(mi)) {
                    mi = template
                            .apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());
                }
                return mi;
            }
        });
    }
}
