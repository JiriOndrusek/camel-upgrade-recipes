# Camel 4.21 Migration Plan

This document provides implementation plans for automatable migrations from the Camel 4.21 upgrade guide analysis.

---

## Table of Contents

- [Core Changes](#core-changes)
- [Component-Specific Changes](#component-specific-changes)
- [Header Renames Infrastructure](#header-renames-infrastructure)
- [Header Renames by Component](#header-renames-by-component)
- [Deprecation Warnings](#deprecation-warnings)
- [Documentation Updates](#documentation-updates)

---

# Core Changes

## Topic 3: Error Registry SPI Changes

### Properties Migration

**Goal:** Migrate `camel.main.errorRegistry*` properties to `camel.errorRegistry.*`

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.ErrorRegistryProperties
displayName: Migrate Error Registry properties
description: Migrate camel.main.errorRegistry* to camel.errorRegistry.*

recipeList:
  # application.properties
  - org.openrewrite.properties.ChangePropertyKey:
      oldPropertyKey: camel.main.errorRegistryEnabled
      newPropertyKey: camel.errorRegistry.enabled
  - org.openrewrite.properties.ChangePropertyKey:
      oldPropertyKey: camel.main.errorRegistryMaximumEntries
      newPropertyKey: camel.errorRegistry.maximumEntries
      
  # application.yml
  - org.openrewrite.yaml.ChangePropertyKey:
      oldPropertyKey: camel.main.errorRegistryEnabled
      newPropertyKey: camel.errorRegistry.enabled
  - org.openrewrite.yaml.ChangePropertyKey:
      oldPropertyKey: camel.main.errorRegistryMaximumEntries
      newPropertyKey: camel.errorRegistry.maximumEntries
```

**Testing:**
- Create test with `application.properties` containing old keys
- Verify keys are renamed correctly
- Test with YAML variant

---

### API Migration (Partial)

**Goal:** Update type references from `ErrorRegistryEntry` to `BacklogErrorEventMessage`

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.ErrorRegistryAPI
displayName: Migrate Error Registry API types
description: Update ErrorRegistryEntry to BacklogErrorEventMessage

recipeList:
  - org.openrewrite.java.ChangeType:
      oldFullyQualifiedTypeName: org.apache.camel.spi.ErrorRegistryEntry
      newFullyQualifiedTypeName: org.apache.camel.spi.BacklogErrorEventMessage
```

**Manual Steps Required:**
- Document that `ErrorRegistry.browse()` return type changed to `Collection<BacklogErrorEventMessage>`
- Users must update code that iterates over results
- Note: `stackTraceEnabled` option removed (always captures full exception)

**Testing:**
- Test with code using `ErrorRegistryEntry` type
- Verify import statements updated
- Flag files for manual review if they call `.browse()` method

---

## Topic 7: ReifierStrategy SPI Removed

**Goal:** Detect and flag usage of removed `ReifierStrategy` SPI

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.ReifierStrategyRemoved
displayName: Flag removed ReifierStrategy SPI usage
description: Detects usage of removed org.apache.camel.spi.ReifierStrategy

recipeList:
  - org.openrewrite.java.search.FindTypes:
      fullyQualifiedTypeName: org.apache.camel.spi.ReifierStrategy
```

**Output:** Search results flagging files that reference this removed SPI

**Testing:**
- Create test file importing `ReifierStrategy`
- Verify recipe detects usage
- Confirm no false positives

---

# Component-Specific Changes

## Topic 23: camel-jms ObjectMessage Disabled

**Goal:** Add `objectMessageEnabled=true` where ObjectMessage features are used

**Implementation:**

Create custom recipe `JmsObjectMessageMigration.java`:

```java
package org.apache.camel.upgrade.camel421;

public class JmsObjectMessageMigration extends Recipe {
    @Override
    public String getDisplayName() {
        return "Enable ObjectMessage support in JMS endpoints";
    }
    
    @Override
    public String getDescription() {
        return "Adds objectMessageEnabled=true to JMS endpoints using ObjectMessage features";
    }
    
    // Detect endpoints with:
    // - jmsMessageType=Object
    // - transferExchange=true
    // - transferException=true
    
    // Add objectMessageEnabled=true parameter
}
```

**Detection Patterns:**
1. Java DSL: `to("jms:queue:foo?jmsMessageType=Object")`
2. XML DSL: `<to uri="jms:queue:foo?transferExchange=true"/>`
3. YAML DSL: Same URI patterns
4. Component-level property: Add `camel.component.jms.objectMessageEnabled=true` if multiple endpoints affected

**YAML Recipe:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.JmsObjectMessage
displayName: Enable JMS ObjectMessage support
description: Adds objectMessageEnabled=true to JMS endpoints requiring ObjectMessage

recipeList:
  - org.apache.camel.upgrade.camel421.JmsObjectMessageMigration
```

**Testing:**
- Test Java DSL route with `jmsMessageType=Object`
- Test XML route with `transferExchange=true`
- Verify parameter added correctly
- Test component-level property addition

---

## Topic 24: camel-sjms/sjms2 ObjectMessage Disabled

**Goal:** Same as Topic 23 but for sjms/sjms2 components

**Implementation:**

Clone approach from Topic 23, create:
- `SjmsObjectMessageMigration.java` for sjms
- `Sjms2ObjectMessageMigration.java` for sjms2

**Recipe:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.SjmsObjectMessage
displayName: Enable SJMS ObjectMessage support
description: Adds objectMessageEnabled=true to SJMS/SJMS2 endpoints

recipeList:
  - org.apache.camel.upgrade.camel421.SjmsObjectMessageMigration
  - org.apache.camel.upgrade.camel421.Sjms2ObjectMessageMigration
```

---

## Topics 26-30: Component Removals

**Goal:** Remove dependencies for deleted components

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.ComponentRemovals
displayName: Remove deleted Camel components
description: Removes dependencies for components removed in 4.21

recipeList:
  # camel-stomp
  - org.openrewrite.maven.RemoveDependency:
      groupId: org.apache.camel
      artifactId: camel-stomp
      
  # camel-aws-xray
  - org.openrewrite.maven.RemoveDependency:
      groupId: org.apache.camel
      artifactId: camel-aws-xray
      
  # camel-guava-eventbus
  - org.openrewrite.maven.RemoveDependency:
      groupId: org.apache.camel
      artifactId: camel-guava-eventbus
      
  # camel-grape
  - org.openrewrite.maven.RemoveDependency:
      groupId: org.apache.camel
      artifactId: camel-grape
      
  # camel-elytron
  - org.openrewrite.maven.RemoveDependency:
      groupId: org.apache.camel
      artifactId: camel-elytron
```

**Additional Step:** Flag route files that reference these component URIs for manual review

**Testing:**
- Create test pom.xml with each removed component
- Verify dependencies removed
- Test with both direct and managed dependencies

---

## Topic 31: camel-github → camel-github2

**Goal:** Migrate from camel-github to camel-github2

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.GithubToGithub2
displayName: Migrate camel-github to camel-github2
description: Replaces camel-github with camel-github2 (new library)

recipeList:
  # Maven dependency
  - org.openrewrite.maven.ChangeDependency:
      oldGroupId: org.apache.camel
      oldArtifactId: camel-github
      newGroupId: org.apache.camel
      newArtifactId: camel-github2
      
  # Gradle dependency (if needed)
  - org.openrewrite.gradle.ChangeDependency:
      oldGroupId: org.apache.camel
      oldArtifactId: camel-github
      newGroupId: org.apache.camel
      newArtifactId: camel-github2
      
  # Java component class
  - org.openrewrite.java.ChangeType:
      oldFullyQualifiedTypeName: org.apache.camel.component.github.GitHubComponent
      newFullyQualifiedTypeName: org.apache.camel.component.github2.GitHubComponent
      
  # Endpoint URIs
  - org.apache.camel.upgrade.customRecipes.ChangeComponentUriRecipe:
      uriPattern: "^github:(.*)$"
      replacement: "github2:${1}"
```

**Testing:**
- Test Maven dependency change
- Test Java code using GitHubComponent
- Test route URIs `github:pullRequest` → `github2:pullRequest`

---

## Topic 35: camel-aws-bedrock Guardrail Header

**Goal:** Rename header from `CamelAwsBedrockGuardrailConfig` to `CamelAwsBedrockGuardrailIdentifier`

**Implementation:**

Custom recipe `BedrockGuardrailHeaderRename.java`:

```java
package org.apache.camel.upgrade.camel421;

// Visitor to find and replace:
// 1. Static field access: BedrockConstants.GUARDRAIL_CONFIG → GUARDRAIL_IDENTIFIER
// 2. String literals: "CamelAwsBedrockGuardrailConfig" → "CamelAwsBedrockGuardrailIdentifier"
//    in setHeader(), getHeader(), removeHeader() calls
```

**Recipe:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.BedrockGuardrailHeader
displayName: Rename Bedrock guardrail header
description: Updates header name from GuardrailConfig to GuardrailIdentifier

recipeList:
  - org.apache.camel.upgrade.camel421.BedrockGuardrailHeaderRename
```

**Testing:**
- Test with code using constant reference
- Test with string literal in setHeader()
- Test Simple expressions

---

## Topic 41: camel-aws2-s3 ListObjects API

**Goal:** Migrate from V1 to V2 ListObjects API types

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.S3ListObjectsV2
displayName: Migrate S3 ListObjects to V2 API
description: Updates ListObjectsRequest/Response to V2 variants

recipeList:
  # Request type
  - org.openrewrite.java.ChangeType:
      oldFullyQualifiedTypeName: software.amazon.awssdk.services.s3.model.ListObjectsRequest
      newFullyQualifiedTypeName: software.amazon.awssdk.services.s3.model.ListObjectsV2Request
      
  # Response type
  - org.openrewrite.java.ChangeType:
      oldFullyQualifiedTypeName: software.amazon.awssdk.services.s3.model.ListObjectsResponse
      newFullyQualifiedTypeName: software.amazon.awssdk.services.s3.model.ListObjectsV2Response
      
  # Builder methods might need updates - document for manual review
```

**Manual Review Required:** Some builder methods may differ between V1 and V2

**Testing:**
- Test code using ListObjectsRequest
- Test code using ListObjectsResponse
- Verify imports updated
- Check builder method compatibility

---

## Topic 69: camel-jooq DBCP Dependency

**Goal:** Migrate from commons-dbcp to commons-dbcp2

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.JooqDbcp2
displayName: Migrate JOOQ to commons-dbcp2
description: Updates commons-dbcp to commons-dbcp2 for camel-jooq users

recipeList:
  - org.openrewrite.maven.ChangeDependency:
      oldGroupId: commons-dbcp
      oldArtifactId: commons-dbcp
      newGroupId: org.apache.commons
      newArtifactId: commons-dbcp2
```

**Testing:**
- Test pom.xml with old commons-dbcp dependency
- Verify groupId and artifactId updated

---

## Topic 73: camel-grok Library Migration

**Goal:** Migrate from io.krakens:java-grok to io.github.whatap:java-grok

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.GrokLibraryMigration
displayName: Migrate Grok library
description: Updates java-grok dependency to maintained fork

recipeList:
  - org.openrewrite.maven.ChangeDependency:
      oldGroupId: io.krakens
      oldArtifactId: java-grok
      newGroupId: io.github.whatap
      newArtifactId: java-grok
```

**Note:** Document that small behavioral differences exist (see fork documentation)

**Testing:**
- Test dependency change
- Note in release notes about potential behavior differences

---

# Header Renames Infrastructure

## Base Infrastructure Recipe

**Goal:** Create reusable visitor infrastructure for all header renames

### Java Header Rename Visitor

Create `HeaderRenameVisitor.java`:

```java
package org.apache.camel.upgrade.camel421;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class HeaderRenameVisitor extends JavaIsoVisitor<ExecutionContext> {
    
    private final Map<String, String> headerMappings;
    
    public HeaderRenameVisitor(Map<String, String> headerMappings) {
        this.headerMappings = headerMappings;
    }
    
    @Override
    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
        // Detect setHeader(), getHeader(), removeHeader() calls
        // Replace string literal arguments matching old header names
        // Return updated method invocation
    }
}
```

### Simple Expression Header Visitor

Create `SimpleExpressionHeaderRenameVisitor.java`:

```java
package org.apache.camel.upgrade.camel421;

// Parse Simple expressions and replace header references:
// ${headers[old.name]} → ${headers[NewName]}
// ${header.old-name} → ${header.NewName}
```

### XML DSL Visitor

Create `XmlHeaderRenameVisitor.java`:

```java
package org.apache.camel.upgrade.camel421;

import org.apache.camel.upgrade.AbstractCamelXmlVisitor;

// Process <setHeader>, <removeHeader> elements
// Update headerName attributes and Simple expressions in nested elements
```

### YAML DSL Visitor

Create `YamlHeaderRenameVisitor.java`:

```java
package org.apache.camel.upgrade.camel421;

import org.apache.camel.upgrade.AbstractCamelYamlVisitor;

// Process YAML setHeader/removeHeader steps
// Update header names and Simple expressions
```

---

## Base Header Rename Recipe

Create abstract recipe `AbstractHeaderRenameRecipe.java`:

```java
package org.apache.camel.upgrade.camel421;

public abstract class AbstractHeaderRenameRecipe extends Recipe {
    
    protected abstract Map<String, String> getHeaderMappings();
    
    @Override
    public List<Recipe> getRecipeList() {
        Map<String, String> mappings = getHeaderMappings();
        
        return Arrays.asList(
            new JavaHeaderRenameRecipe(mappings),
            new XmlHeaderRenameRecipe(mappings),
            new YamlHeaderRenameRecipe(mappings)
        );
    }
}
```

**Testing Infrastructure:**
- Test Java setHeader() with string literal
- Test Simple expression `${headers[oldName]}`
- Test XML DSL `<setHeader headerName="oldName">`
- Test YAML DSL setHeader steps
- Verify no changes to code using constant references

---

# Header Renames by Component

## Topic 36: camel-jgroups Headers

**Implementation:**

```java
package org.apache.camel.upgrade.camel421;

public class JGroupsHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("JGROUPS_DEST", "CamelJGroupsDest");
        mappings.put("JGROUPS_SRC", "CamelJGroupsSrc");
        mappings.put("JGROUPS_CHANNEL_ADDRESS", "CamelJGroupsChannelAddress");
        // Add all JGroups header mappings
        return mappings;
    }
}
```

---

## Topic 37: camel-elasticsearch-rest-client Headers

**Implementation:**

```java
public class ElasticsearchRestClientHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("ID", "CamelElasticsearchId");
        mappings.put("SEARCH_QUERY", "CamelElasticsearchSearchQuery");
        // Add all elasticsearch-rest-client header mappings
        return mappings;
    }
}
```

---

## Topic 38: camel-cxf Headers (BREAKING - Transport Filtering)

**Implementation:**

```java
public class CxfHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("operationName", "CamelCxfOperationName");
        mappings.put("operationNamespace", "CamelCxfOperationNamespace");
        // Add all CXF header mappings
        return mappings;
    }
    
    @Override
    public String getDescription() {
        return "Renames CXF headers to CamelXxx convention. " +
               "WARNING: Headers are now filtered at transport boundaries. " +
               "HTTP/JMS bridges require explicit header mapping.";
    }
}
```

**Additional Recipe for Bridge Detection:**

Create `CxfTransportBridgeDetector.java` to flag routes that:
- Have both JMS/HTTP consumer and CXF producer (or vice versa)
- Use CXF headers
- May need explicit header mapping code

**Manual Steps Document:**
```markdown
# CXF Transport Bridge Migration

If you have routes bridging JMS ↔ CXF or HTTP ↔ CXF that use operation headers:

Add explicit mapping:
<setHeader name="CamelCxfOperationName">
    <simple>${header.operationName}</simple>
</setHeader>
```

---

## Topic 39: camel-dns Headers

**Implementation:**

```java
public class DnsHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("dns.class", "CamelDnsClass");
        mappings.put("dns.name", "CamelDnsName");
        mappings.put("term", "CamelDnsTerm");
        // Add all DNS header mappings
        return mappings;
    }
}
```

---

## Topic 40: camel-solr Header Prefixes

**Implementation:**

Custom approach needed - prefix replacement not full rename:

```java
public class SolrHeaderPrefixRename extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Update Solr header prefixes";
    }
    
    @Override
    public String getDescription() {
        return "Updates SolrField. → CamelSolrField. and SolrParam. → CamelSolrParam.";
    }
    
    // Custom visitor to:
    // 1. Find string literals starting with "SolrField." or "SolrParam."
    // 2. Add "Camel" prefix: "SolrField.foo" → "CamelSolrField.foo"
}
```

---

## Topic 44: camel-lucene Headers

**Implementation:**

```java
public class LuceneHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("QUERY", "CamelLuceneQuery");
        mappings.put("RETURN_LUCENE_DOCS", "CamelLuceneReturnLuceneDocs");
        return mappings;
    }
}
```

---

## Topic 45: camel-couchdb Headers

**Implementation:**

```java
public class CouchDbHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("CouchDbDatabase", "CamelCouchDbDatabase");
        mappings.put("CouchDbSeq", "CamelCouchDbSeq");
        // Add remaining CouchDB header mappings
        return mappings;
    }
}
```

---

## Topic 46: camel-couchbase Headers

**Implementation:**

```java
public class CouchbaseHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("CCB_KEY", "CamelCouchbaseKey");
        mappings.put("CCB_ID", "CamelCouchbaseId");
        // Add remaining Couchbase header mappings
        return mappings;
    }
}
```

---

## Topic 47: camel-jgroups-raft Headers

**Implementation:**

```java
public class JGroupsRaftHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("JGROUPSRAFT_COMMIT_INDEX", "CamelJGroupsRaftCommitIndex");
        mappings.put("JGROUPSRAFT_CURRENT_TERM", "CamelJGroupsRaftCurrentTerm");
        // Add all JGROUPSRAFT_* mappings
        return mappings;
    }
}
```

---

## Topic 48: camel-jira Headers

**Implementation:**

```java
public class JiraHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("IssueAssigneeId", "CamelJiraIssueAssigneeId");
        mappings.put("IssueKey", "CamelJiraIssueKey");
        // Add all JIRA header mappings
        return mappings;
    }
}
```

---

## Topic 52: camel-shiro Security Headers (BREAKING - Transport Filtering)

**Implementation:**

```java
public class ShiroHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("SHIRO_SECURITY_TOKEN", "CamelShiroSecurityToken");
        mappings.put("SHIRO_SECURITY_USERNAME", "CamelShiroSecurityUsername");
        mappings.put("SHIRO_SECURITY_PASSWORD", "CamelShiroSecurityPassword");
        return mappings;
    }
    
    @Override
    public String getDescription() {
        return "Renames Shiro headers to CamelXxx convention. " +
               "WARNING: Headers are now filtered at transport boundaries. " +
               "Trusted Shiro-over-transport routes require custom filter.";
    }
}
```

---

## Topic 53: camel-web3j Headers

**Implementation:**

```java
public class Web3jHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("ID", "CamelWeb3jId");
        mappings.put("FROM_ADDRESS", "CamelWeb3jFromAddress");
        mappings.put("ETH_HASHRATE", "CamelWeb3jEthHashrate");
        // Add all Web3j header mappings
        return mappings;
    }
}
```

---

## Topic 54: camel-openstack Headers

**Implementation:**

```java
public class OpenstackHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // Common
        mappings.put("operation", "CamelOpenstackOperation");
        
        // Keystone
        mappings.put("domainId", "CamelOpenstackKeystoneDomainId");
        
        // Nova
        mappings.put("FlavorId", "CamelOpenstackNovaFlavorId");
        
        // Cinder
        mappings.put("size", "CamelOpenstackCinderSize");
        
        // Glance
        mappings.put("diskFormat", "CamelOpenstackGlanceDiskFormat");
        
        // Neutron
        mappings.put("tenantId", "CamelOpenstackNeutronTenantId");
        
        // Swift
        mappings.put("containerName", "CamelOpenstackSwiftContainerName");
        
        // Add all remaining OpenStack subcomponent headers
        return mappings;
    }
}
```

**Note:** This is a complex migration covering multiple OpenStack subcomponents

---

## Topic 55: camel-pdf Headers

**Implementation:**

```java
public class PdfHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("protection-policy", "CamelPdfProtectionPolicy");
        mappings.put("pdf-document", "CamelPdfDocument");
        // Add all PDF header mappings
        return mappings;
    }
}
```

---

## Topic 56: camel-elasticsearch / camel-opensearch Headers

**Implementation:**

```java
public class ElasticsearchHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("operation", "CamelElasticsearchOperation");
        mappings.put("indexId", "CamelElasticsearchIndexId");
        // Add all Elasticsearch header mappings
        return mappings;
    }
}

public class OpensearchHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("operation", "CamelOpensearchOperation");
        mappings.put("indexId", "CamelOpensearchIndexId");
        // Add all OpenSearch header mappings
        return mappings;
    }
}
```

---

## Topic 57: camel-github2 Producer Headers

**Implementation:**

```java
public class GitHub2HeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("GitHubPullRequest", "CamelGitHubPullRequest");
        mappings.put("GitHubInResponseTo", "CamelGitHubInResponseTo");
        // Add all GitHub2 header mappings
        return mappings;
    }
}
```

---

## Topic 58: camel-google-cloud Headers

**Implementation:**

```java
public class GoogleCloudHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // Functions
        mappings.put("GoogleCloudFunctionsOperation", "CamelGoogleCloudFunctionsOperation");
        
        // Secret Manager
        mappings.put("GoogleCloudSecretManagerOperation", "CamelGoogleCloudSecretManagerOperation");
        
        // Vision
        mappings.put("GoogleCloudVisionOperation", "CamelGoogleCloudVisionOperation");
        
        // Text-to-Speech
        mappings.put("GoogleCloudTextToSpeechOperation", "CamelGoogleCloudTextToSpeechOperation");
        
        // Speech-to-Text
        mappings.put("GoogleCloudSpeechToTextOperation", "CamelGoogleCloudSpeechToTextOperation");
        
        // Add all remaining Google Cloud header mappings
        return mappings;
    }
}
```

---

## Topic 59: camel-arangodb Headers

**Implementation:**

```java
public class ArangoDbHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("key", "CamelArangoDbKey");
        mappings.put("ResultClassType", "CamelArangoDbResultClassType");
        return mappings;
    }
}
```

---

## Topic 60: camel-jt400 Headers

**Implementation:**

```java
public class Jt400HeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("KEY", "CamelJt400Key");
        mappings.put("SENDER_INFORMATION", "CamelJt400SenderInformation");
        return mappings;
    }
}
```

---

## Topic 61: camel-mail Consumer Headers

**Implementation:**

```java
public class MailHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("copyTo", "CamelMailCopyTo");
        mappings.put("moveTo", "CamelMailMoveTo");
        mappings.put("delete", "CamelMailDelete");
        // Note: RFC 5322 headers (Subject, From, etc.) unchanged
        return mappings;
    }
}
```

---

## Topic 62: camel-milo Header

**Implementation:**

```java
public class MiloHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("await", "CamelMiloAwait");
        return mappings;
    }
}
```

---

## Topic 63: camel-kafka Headers (CRITICAL PRIORITY)

**Implementation:**

```java
public class KafkaHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("kafka.PARTITION_KEY", "CamelKafkaPartitionKey");
        mappings.put("kafka.OVERRIDE_TOPIC", "CamelKafkaOverrideTopic");
        mappings.put("kafka.TOPIC", "CamelKafkaTopic");
        mappings.put("kafka.KEY", "CamelKafkaKey");
        mappings.put("kafka.OFFSET", "CamelKafkaOffset");
        mappings.put("kafka.PARTITION", "CamelKafkaPartition");
        mappings.put("kafka.TIMESTAMP", "CamelKafkaTimestamp");
        // Add ALL kafka.* header mappings
        return mappings;
    }
}
```

**Special Handling for Simple Expressions:**

Ensure Simple expression visitor handles:
```java
// Before
${headers[kafka.TOPIC]}

// After
${headers[CamelKafkaTopic]}
```

**Testing Priority:**
- Test all DSL variants (Java, XML, YAML)
- Test Simple expressions extensively
- Test both square bracket and dot notation
- Verify backward compatibility when using constants

---

## Topic 64: camel-irc Headers (BREAKING - Transport Filtering, Deprecated)

**Implementation:**

```java
public class IrcHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("irc.messageType", "CamelIrcMessageType");
        mappings.put("irc.sendTo", "CamelIrcSendTo");
        // Add all irc.* header mappings
        return mappings;
    }
    
    @Override
    public String getDescription() {
        return "Renames IRC headers to CamelXxx convention. " +
               "WARNING: Headers now filtered at transport boundaries. " +
               "NOTE: camel-irc is deprecated (Topic 76).";
    }
}
```

---

## Topic 65: camel-mongodb-gridfs Headers

**Implementation:**

```java
public class MongoDbGridFsHeaderRename extends AbstractHeaderRenameRecipe {
    
    @Override
    protected Map<String, String> getHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("gridfs.operation", "CamelGridFsOperation");
        mappings.put("gridfs.metadata", "CamelGridFsMetadata");
        // Add all gridfs.* header mappings
        return mappings;
    }
}
```

---

# Deprecation Warnings

## Topics 74-79: Component Deprecations

**Goal:** Flag usage of deprecated components with warnings

**Implementation:**

Create `DeprecatedComponentDetector.java`:

```java
package org.apache.camel.upgrade.camel421;

public class DeprecatedComponentDetector extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Detect deprecated Camel 4.21 components";
    }
    
    @Override
    public String getDescription() {
        return "Flags usage of components deprecated in Camel 4.21";
    }
    
    // Detect Maven/Gradle dependencies for:
    // - camel-ironmq (Topic 74)
    // - camel-digitalocean (Topic 75)
    // - camel-irc (Topic 76)
    // - camel-iec-60870 (Topic 77)
    // - camel-paho (Topic 79)
    
    // Output warnings with deprecation reason and future removal version
}
```

**YAML Recipe:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.DeprecatedComponents
displayName: Flag deprecated components
description: Warns about components deprecated in 4.21

recipeList:
  - org.openrewrite.maven.search.DependencyInsight:
      groupId: org.apache.camel
      artifactId: camel-ironmq
  - org.openrewrite.maven.search.DependencyInsight:
      groupId: org.apache.camel
      artifactId: camel-digitalocean
  - org.openrewrite.maven.search.DependencyInsight:
      groupId: org.apache.camel
      artifactId: camel-irc
  - org.openrewrite.maven.search.DependencyInsight:
      groupId: org.apache.camel
      artifactId: camel-iec-60870
  - org.openrewrite.maven.search.DependencyInsight:
      groupId: org.apache.camel
      artifactId: camel-paho
```

**Output:** List of files with deprecation warnings

---

# Documentation Updates

## Topic 66: Jackson Dataformat Documentation Links

**Goal:** Update xref links in AsciiDoc files

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.JacksonDataformatDocLinks
displayName: Update Jackson dataformat doc links
description: Updates xref links to use versioned jackson dataformat docs

recipeList:
  - org.openrewrite.text.FindAndReplace:
      find: "xref:jackson-dataformat.adoc"
      replace: "xref:jackson2-dataformat.adoc"
      filePattern: "**/*.adoc"
```

**Note:** May need variant for jackson3-dataformat.adoc depending on context

**Testing:**
- Test with .adoc files containing old xref links
- Verify links updated correctly

---

# Partially Automatable Topics

## Topic 2: DefaultHeaderFilterStrategy Lowercase Default

**Goal:** Detect usage and optionally add explicit configuration

**Implementation:**

Detection recipe:

```java
public class HeaderFilterStrategyLowercaseDetector extends Recipe {
    
    // Find: new DefaultHeaderFilterStrategy()
    // Flag for manual review with note about default change
    // Optional: Add .setLowerCase(false) to maintain old behavior
    //           (but document that new default is likely better)
}
```

**Recommendation:** Document only, don't automate adding `.setLowerCase(false)`

---

## Topic 6: Java Serialization Type Converters Removed

**Goal:** Detect usage and flag for manual review

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.SerializationConvertersDetector
displayName: Detect removed serialization converters
description: Flags usage of removed ObjectInput/ObjectOutput type converters

recipeList:
  - org.openrewrite.java.search.FindTypes:
      fullyQualifiedTypeName: java.io.ObjectInput
  - org.openrewrite.java.search.FindTypes:
      fullyQualifiedTypeName: java.io.ObjectOutput
```

**Output:** Files potentially affected by removed converters

---

## Topic 20: YAML DSL Canonical Schema

**Goal:** Suppress compact notation warnings or normalize YAML

**Implementation:**

Option 1 - Suppress warnings (properties):
```yaml
- org.openrewrite.properties.CreateOrUpdateProperty:
    property: camel.main.yamlDslCompactNotationWarn
    value: "false"
```

Option 2 - Normalize YAML (complex, low priority):
- Would require implementing YAML normalization logic similar to `camel yaml normalize` command
- Defer until user demand justifies effort

**Recommendation:** Document property to suppress warnings

---

## Topic 21: Kafka Spring Boot Property Bridging

**Goal:** Remove redundant `camel.component.kafka.*` when `spring.kafka.*` exists

**Implementation:**

```java
public class KafkaPropertyBridgingCleanup extends Recipe {
    
    // Detect:
    // 1. spring.kafka.bootstrap-servers present
    // 2. camel.component.kafka.bootstrap-servers with same value
    
    // Action:
    // - Flag for manual review (not auto-remove due to precedence rules)
    // - Document that explicit Camel settings take precedence
}
```

**Recommendation:** Detection + documentation only (precedence makes auto-removal risky)

---

## Topic 32: Telemetry Span Interface Changes

**Goal:** Detect custom implementations and flag for manual update

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.TelemetrySpanInterfaceDetector
displayName: Detect custom telemetry span implementations
description: Flags custom SpanLifecycleManager/SpanDecorator implementations

recipeList:
  - org.openrewrite.java.search.FindImplementations:
      interfaceName: org.apache.camel.opentelemetry.SpanLifecycleManager
  - org.openrewrite.java.search.FindImplementations:
      interfaceName: org.apache.camel.opentelemetry.SpanDecorator
```

**Manual Steps Document:**
```markdown
# Telemetry Span Interface Migration

Update method signature:

// Before
Span create(String spanName, Span parent, SpanContextPropagationExtractor extractor)

// After
Span create(String spanName, String spanKind, Span parent, SpanContextPropagationExtractor extractor)

Determine appropriate spanKind value based on context.
```

---

## Topic 34: DJL Word Embedding Removed

**Goal:** Detect usage and flag for manual migration

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.DjlWordEmbeddingDetector
displayName: Detect removed DJL word embedding
description: Flags usage of removed nlp/word_embedding predictor

recipeList:
  - org.openrewrite.text.FindAndReplace:
      find: "nlp/word_embedding"
      replace: "nlp/word_embedding"  # Just to detect, not change
      filePattern: "**/*.{java,xml,yaml,properties}"
```

**Output:** Files using removed predictor

**Manual Migration:** Users must provide custom model with explicit `model` and `translator`

---

## Topic 72: Langchain4j/Spring AI Parameter Filtering

**Goal:** Detect endpoints missing parameter declarations

**Implementation:**

```java
public class AiToolsParameterDetector extends Recipe {
    
    // Detect endpoints:
    // - langchain4j-tools:*
    // - spring-ai-tools:*
    
    // Check for missing parameter.* options
    
    // Flag for manual review:
    // "This endpoint receives parameters from LLM but has no parameter declarations. 
    //  Add parameter.xxx=type for each expected parameter."
}
```

**Manual Steps:** Users must review LLM tool contract and declare parameters explicitly

---

## Topic 78: Jasypt CLI Deprecated

**Goal:** Detect usage of deprecated CLI entrypoint

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.JasyptCliDeprecated
displayName: Detect deprecated Jasypt CLI
description: Flags usage of deprecated org.apache.camel.component.jasypt.Main

recipeList:
  - org.openrewrite.java.search.FindTypes:
      fullyQualifiedTypeName: org.apache.camel.component.jasypt.Main
```

**Manual Migration:** Use standalone Jasypt CLI scripts instead

---

# Master Recipe

## Camel 4.21 Migration Recipe

**Goal:** Composite recipe that applies all automatable migrations

**Implementation:**

```yaml
---
type: specs.openrewrite.org/v1beta/recipe
name: org.apache.camel.upgrade.camel421.CamelMigrationRecipe
displayName: Migrates Camel 4.20 application to Camel 4.21
description: Applies all automated migrations for Camel 4.21 upgrade

recipeList:
  # Core changes
  - org.apache.camel.upgrade.camel421.ErrorRegistryProperties
  - org.apache.camel.upgrade.camel421.ErrorRegistryAPI
  - org.apache.camel.upgrade.camel421.ReifierStrategyRemoved
  
  # Component changes
  - org.apache.camel.upgrade.camel421.JmsObjectMessage
  - org.apache.camel.upgrade.camel421.SjmsObjectMessage
  - org.apache.camel.upgrade.camel421.ComponentRemovals
  - org.apache.camel.upgrade.camel421.GithubToGithub2
  - org.apache.camel.upgrade.camel421.BedrockGuardrailHeader
  - org.apache.camel.upgrade.camel421.S3ListObjectsV2
  - org.apache.camel.upgrade.camel421.JooqDbcp2
  - org.apache.camel.upgrade.camel421.GrokLibraryMigration
  
  # Header renames (CRITICAL FIRST)
  - org.apache.camel.upgrade.camel421.KafkaHeaderRename
  
  # Header renames (alphabetical by component)
  - org.apache.camel.upgrade.camel421.ArangoDbHeaderRename
  - org.apache.camel.upgrade.camel421.CouchbaseHeaderRename
  - org.apache.camel.upgrade.camel421.CouchDbHeaderRename
  - org.apache.camel.upgrade.camel421.CxfHeaderRename
  - org.apache.camel.upgrade.camel421.DnsHeaderRename
  - org.apache.camel.upgrade.camel421.ElasticsearchHeaderRename
  - org.apache.camel.upgrade.camel421.ElasticsearchRestClientHeaderRename
  - org.apache.camel.upgrade.camel421.GitHub2HeaderRename
  - org.apache.camel.upgrade.camel421.GoogleCloudHeaderRename
  - org.apache.camel.upgrade.camel421.IrcHeaderRename
  - org.apache.camel.upgrade.camel421.JGroupsHeaderRename
  - org.apache.camel.upgrade.camel421.JGroupsRaftHeaderRename
  - org.apache.camel.upgrade.camel421.JiraHeaderRename
  - org.apache.camel.upgrade.camel421.Jt400HeaderRename
  - org.apache.camel.upgrade.camel421.LuceneHeaderRename
  - org.apache.camel.upgrade.camel421.MailHeaderRename
  - org.apache.camel.upgrade.camel421.MiloHeaderRename
  - org.apache.camel.upgrade.camel421.MongoDbGridFsHeaderRename
  - org.apache.camel.upgrade.camel421.OpensearchHeaderRename
  - org.apache.camel.upgrade.camel421.OpenstackHeaderRename
  - org.apache.camel.upgrade.camel421.PdfHeaderRename
  - org.apache.camel.upgrade.camel421.ShiroHeaderRename
  - org.apache.camel.upgrade.camel421.SolrHeaderPrefixRename
  - org.apache.camel.upgrade.camel421.Web3jHeaderRename
  
  # Deprecation warnings
  - org.apache.camel.upgrade.camel421.DeprecatedComponents
  
  # Documentation updates
  - org.apache.camel.upgrade.camel421.JacksonDataformatDocLinks
```

---

# Testing Strategy

## Unit Tests

For each recipe:
1. Create test input files (Java, XML, YAML)
2. Run recipe transformation
3. Assert expected output
4. Verify no unintended changes

## Integration Tests

1. Test on real Camel projects
2. Verify builds succeed after migration
3. Run application tests
4. Check for runtime regressions

## Test Coverage Requirements

- All header rename recipes: 100% coverage of DSL variants
- Component removals: Test with direct and managed dependencies
- Property migrations: Test both .properties and .yml files
- API changes: Test with various usage patterns

---

# Implementation Priority

## Phase 1: Infrastructure (Week 1-2)
1. Header rename infrastructure (Java, Simple, XML, YAML visitors)
2. Base AbstractHeaderRenameRecipe
3. Testing framework

## Phase 2: Critical Migrations (Week 3-4)
1. Kafka headers (Topic 63)
2. JMS/SJMS ObjectMessage (Topics 23-24)
3. Component removals (Topics 26-31)
4. Error Registry (Topic 3)

## Phase 3: High-Impact Headers (Week 5-6)
1. Elasticsearch/OpenSearch (Topics 37, 56)
2. Google Cloud (Topic 58)
3. CXF (Topic 38 - with transport filtering warnings)
4. AWS components (Topics 35, 41)

## Phase 4: Remaining Headers (Week 7-8)
1. Batch-create remaining 20+ header rename recipes
2. Use validated infrastructure
3. Focus on consistent patterns

## Phase 5: Polish (Week 9)
1. Deprecation warnings (Topics 74-79)
2. Documentation updates (Topic 66)
3. Detection-only recipes (Topics 2, 6, 20, etc.)
4. Master recipe composition

---

# Rollout Strategy

1. **Alpha Release**: Infrastructure + Kafka headers only
2. **Beta Release**: Add critical migrations (JMS, component removals)
3. **RC Release**: All header renames completed
4. **GA Release**: Full test coverage, documentation complete

---

# Documentation Requirements

For each recipe, provide:

1. **Migration Guide Section**: Explain what changed and why
2. **Before/After Examples**: Show code transformation
3. **Manual Steps**: Document what cannot be automated
4. **Breaking Change Warnings**: Highlight transport filtering, API changes
5. **Testing Recommendations**: How users should verify migration

---

# Success Metrics

- **Coverage**: % of 4.21 changes with automated migration
- **Accuracy**: % of automated changes that are correct
- **Adoption**: # of projects successfully migrated
- **Time Savings**: Hours saved vs manual migration

**Target Goals:**
- 90%+ coverage of automatable changes
- 99%+ accuracy (no incorrect transformations)
- <5% manual review required after automation
