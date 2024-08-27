package org.apache.camel.updates;

import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractUpdateTest implements RewriteTest {

    private boolean skipUnknownDependencies;

    protected abstract CamelTestUtil.CamelVersion recipe();
    protected abstract Parser.Builder parser();

    @Override
    public void defaults(RecipeSpec spec) {
        CamelTestUtil.recipe(spec, recipe())
                .parser(parser())
                .typeValidationOptions(TypeValidation.none());
    }

    protected Parser.Builder parserFromClasspath(CamelTestUtil.CamelVersion from, String... classpath) {
        List<String> resources = Arrays.stream(classpath).map(cl -> {
                    if (cl.startsWith("camel-")) {
                        String maxVersion = cl + "-" +  from.getVersion();
                        //find the highest version lesser or equals the required one
                        Optional<String> dependency = Arrays.stream(Paths.get("target/test-classes/META-INF/rewrite/classpath").toFile().listFiles())
                                .filter(f -> f.getName().startsWith(cl))
                                .map(f -> f.getName().substring(0, f.getName().lastIndexOf(".")))
                                //filter out or higher version the requested
                                .filter(f -> f.compareTo(maxVersion) <= 0)
                                .sorted(Comparator.reverseOrder())
                                .findFirst();

                        return dependency.orElse(skipUnknownDependencies ? "<does not exist>" : cl);
                    }
                    return cl;
                })
                .filter(s -> !"<does not exist>".equals(s))
                .collect(Collectors.toList());

        return JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true)
                .classpathFromResources(new InMemoryExecutionContext(), resources.toArray(new String[resources.size()]));
    }

    public void setSkipUnknownDependencies(boolean skipUnknownDependencies) {
        this.skipUnknownDependencies = skipUnknownDependencies;
    }

}
