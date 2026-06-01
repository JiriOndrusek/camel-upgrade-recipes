# Camel 4.21 Migration Setup - Summary

This document summarizes the initial setup for Camel 4.20 → 4.21 migration recipes.

## Changes Made

### 1. Recipe Structure Files

Created the basic recipe structure following the pattern from commit `f761c1c`:

#### Core Recipe File
- **File:** `camel-upgrade-recipes/src/main/resources/META-INF/rewrite/4.21.yaml`
- **Content:** Empty recipe template ready for migration rules
- **Recipe Name:** `org.apache.camel.upgrade.camel421.CamelMigrationRecipe`

#### Spring Boot Recipe File
- **File:** `camel-spring-boot-upgrade-recipes/src/main/resources/META-INF/rewrite/4.21.yaml`
- **Content:** Chains 4.20 Spring Boot recipe + 4.21 Camel recipe
- **Recipe Name:** `org.apache.camel.upgrade.camel421.CamelSpringBootMigrationRecipe`

### 2. Updated Latest Recipes

Both `latest.yaml` files updated to include 4.21 as the first recipe in the chain:

- `camel-upgrade-recipes/src/main/resources/META-INF/rewrite/latest.yaml`
- `camel-spring-boot-upgrade-recipes/src/main/resources/META-INF/rewrite/latest.yaml`

### 3. Test Infrastructure

#### Updated CamelTestUtil
- **File:** `camel-upgrade-recipes/src/test/java/org/apache/camel/upgrade/CamelTestUtil.java`
- **Change:** Added `v4_21(4, 21, 0)` to CamelVersion enum

#### Created Test Class
- **File:** `camel-upgrade-recipes/src/test/java/org/apache/camel/upgrade/CamelUpdate421Test.java`
- **Content:** Empty test class with placeholder test
- **Purpose:** Tests will be added as recipes are implemented

### 4. Release Notes

- **File:** `release_notes.adoc`
- **Content:** Added section 4.21.0 with:
  - Summary
  - Complete migration coverage table (79 topics)
  - Implementation status (all marked as "None" - to be implemented)
  - References to analysis and plan documents

## Analysis Documents Created

### camel-421-migration-analysis.md (27KB)
Comprehensive analysis of all 79 migration topics with:
- Detailed breakdown by category
- Automation feasibility assessment
- Implementation approaches for each topic
- Priority levels (Critical/High/Medium/Low)
- Summary: 42 automatable, 23 manual, 14 partial

### camel-421-migration-plan.md (50KB+)
Detailed implementation plans including:
- Recipe infrastructure design
- Concrete implementation code for each automatable topic
- Testing strategies
- 9-week phased rollout plan
- Success metrics

## Current State

✅ **Project compiles successfully** with all changes

✅ **No recipes implemented yet** - infrastructure is ready for development

✅ **Following established patterns** from previous version migrations (4.17→4.18, 4.19→4.20)

## File Structure

```
camel-upgrade-recipes/
├── camel-421-migration-analysis.md          # Analysis of all 79 topics
├── camel-421-migration-plan.md              # Implementation plans
├── CAMEL-421-SETUP-SUMMARY.md              # This file
├── release_notes.adoc                       # Updated with 4.21 section
├── camel-upgrade-recipes/
│   └── src/main/
│       └── resources/META-INF/rewrite/
│           ├── 4.21.yaml                    # Empty recipe template
│           └── latest.yaml                  # Updated to include 4.21
│   └── src/test/java/org/apache/camel/upgrade/
│       ├── CamelTestUtil.java              # Updated with v4_21
│       └── CamelUpdate421Test.java         # Empty test class
└── camel-spring-boot-upgrade-recipes/
    └── src/main/resources/META-INF/rewrite/
        ├── 4.21.yaml                        # Spring Boot recipe
        └── latest.yaml                      # Updated to include 4.21
```

## Next Steps

Based on the migration plan, recommended implementation order:

### Phase 1: Infrastructure (Weeks 1-2)
1. Create header rename infrastructure:
   - `HeaderRenameVisitor.java` for Java code
   - `SimpleExpressionHeaderRenameVisitor.java` for Simple expressions
   - `XmlHeaderRenameVisitor.java` for XML DSL
   - `YamlHeaderRenameVisitor.java` for YAML DSL
   - `AbstractHeaderRenameRecipe.java` base class

### Phase 2: Critical Migrations (Weeks 3-4)
1. **Topic 63: Kafka Headers** (CRITICAL - most widely used)
2. **Topics 23-24: JMS/SJMS ObjectMessage** (security breaking change)
3. **Topics 26-31: Component Removals** (clean dependency management)
4. **Topic 3: Error Registry Properties** (property migration)

### Phase 3: High-Impact Migrations (Weeks 5-6)
1. **Topic 37, 56: Elasticsearch/OpenSearch Headers**
2. **Topic 58: Google Cloud Headers**
3. **Topic 38: CXF Headers** (with transport filtering warnings)
4. **Topic 41: AWS S3 ListObjects API**

### Phase 4: Remaining Headers (Weeks 7-8)
Implement remaining 20+ header rename recipes using validated infrastructure

### Phase 5: Polish (Week 9)
1. Deprecation warnings (Topics 74-79)
2. Documentation updates
3. Detection-only recipes
4. Master recipe composition

## Migration Coverage Summary

From the analysis of 79 topics:

| Status | Count | Topics |
|--------|-------|--------|
| ✅ Automatable | 42 | Full automation via OpenRewrite recipes |
| ⚠️ Partially Automatable | 14 | Detection + flagging, or limited automation |
| ❌ Manual Only | 23 | Runtime behavior, CLI changes, requires manual review |

### Highest Priority Automations

1. **CRITICAL: Kafka Headers (Topic 63)**
   - Most widely used component
   - `kafka.*` → `CamelKafka*` pattern affects many projects

2. **HIGH: Component Removals (Topics 26-31)**
   - 6 components removed/replaced
   - Clean Maven dependency recipes

3. **HIGH: JMS/SJMS ObjectMessage (Topics 23-24)**
   - Security-motivated breaking change
   - Requires explicit opt-in configuration

4. **HIGH: Error Registry, CXF, AWS S3, Shiro**
   - Various breaking changes
   - Clear migration paths identified

## Key Implementation Patterns

### Header Renames (30 topics)
All follow the same pattern:
```java
public class KafkaHeaderRename extends AbstractHeaderRenameRecipe {
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("kafka.TOPIC", "CamelKafkaTopic");
        // ... more mappings
        return mappings;
    }
}
```

### Component Removals (6 topics)
Standard OpenRewrite Maven recipes:
```yaml
- org.openrewrite.maven.RemoveDependency:
    groupId: org.apache.camel
    artifactId: camel-stomp
```

### Property Migrations (Topic 3)
Using existing property change recipes:
```yaml
- org.openrewrite.properties.ChangePropertyKey:
    oldPropertyKey: camel.main.errorRegistryEnabled
    newPropertyKey: camel.errorRegistry.enabled
```

## Testing Strategy

Each recipe requires:
1. **Unit tests** with before/after examples
2. **Integration tests** on real Camel projects
3. **Coverage** across Java, XML, and YAML DSLs
4. **Validation** that builds succeed after migration

## Documentation Requirements

For each implemented recipe:
1. Before/after code examples
2. Manual steps documentation (what cannot be automated)
3. Breaking change warnings
4. Testing recommendations

## Success Criteria

- ✅ Project structure ready for recipe development
- ✅ All placeholder files created and compiling
- ✅ Analysis complete (79 topics evaluated)
- ✅ Implementation plan documented
- ⏳ Recipe implementation (to be done)
- ⏳ Test coverage (to be done)
- ⏳ Documentation updates (to be done)

## How to Start Implementing

1. Choose a topic from the plan (recommend starting with infrastructure or Kafka headers)
2. Implement the recipe in Java or YAML as specified in the plan
3. Add corresponding test in `CamelUpdate421Test.java`
4. Update `4.21.yaml` recipeList to include the new recipe
5. Run tests: `mvn test -Dtest=CamelUpdate421Test`
6. Update release notes to mark the topic as implemented

## References

- [Camel 4.21 Upgrade Guide](https://camel.apache.org/manual/camel-4x-upgrade-guide-4_21.html)
- [OpenRewrite Java Recipes](https://docs.openrewrite.org/recipes/java)
- [OpenRewrite Maven Recipes](https://docs.openrewrite.org/recipes/maven)
- Previous migration commit: f761c1c70a0adb7b50a5b31d955cd37c31688761
- Analysis document: `camel-421-migration-analysis.md`
- Implementation plan: `camel-421-migration-plan.md`
