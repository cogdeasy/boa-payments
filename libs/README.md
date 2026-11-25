# Vendored platform libraries

| directory | coordinates | upstream |
|---|---|---|
| `nb-common/` | `com.northbridge:nb-common:4.11.3` | Northbridge Core Platform, `nb-common` module, release 4.11.3 |

The payments hub is built and deployed outside the platform release train, and the build agents in the
payments zone cannot reach the platform Nexus. The pinned release is therefore kept here as source and
built as part of this reactor (`mvn install` installs it into the local repository).

Rules:

- Do not patch these sources. Fixes go to the platform repository; pick them up by replacing the
  directory with the new release and bumping the version in `payments-hub/pom.xml` (`nb-common.version`).
- `nb-common` 4.11.x targets Java 8 bytecode and uses `javax.xml.bind` (JAXB) and log4j 1.2. The hub
  supplies a JAXB runtime because it runs on Java 11.
- The hub uses `Money`, `AccountNumbers`, `BankDates`, `AuditLogger`, `StatementXml` and
  `LegacyTokenCipher` directly. Any platform release that changes their package names, bytecode level or
  XML binding API needs a matching change in the hub.
