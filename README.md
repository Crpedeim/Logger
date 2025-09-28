# Logger (Java)

A lightweight, thread-aware logging utility packaged as a Java library. It buffers `Log` entries in-memory and persists them asynchronously to a file datastore.

- Group: `com.github.Crpedeim`
- Artifact: `logger`
- Version: `1.0.0`
- Repo: `https://github.com/Crpedeim/Logger`
- Packages: `https://maven.pkg.github.com/Crpedeim/logger`

## Requirements
- Java 17+
- Maven or Gradle

## Features
- Simple API: `Logger.getInstance().addLog(...);` then `appendLog()`
- Structured entries: message, timestamp, thread id/name, severity, stack trace
- Async persistence with a fixed thread pool
- File-backed store to `log.txt` using Java serialization

## Installation

### Maven
```xml
<repositories>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/Crpedeim/logger</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.Crpedeim</groupId>
    <artifactId>logger</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

If GitHub Packages requires authentication, add credentials to `~/.m2/settings.xml`:
```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```
`YOUR_GITHUB_TOKEN` must have `read:packages`.

### Gradle (Kotlin DSL)
```kotlin
repositories {
  maven {
    url = uri("https://maven.pkg.github.com/Crpedeim/logger")
    credentials {
      username = System.getenv("GITHUB_USER")
      password = System.getenv("GITHUB_TOKEN")
    }
  }
}

dependencies {
  implementation("com.github.Crpedeim:logger:1.0.0")
}
```

## Quick start
```java
import logger.enums.Severity;
import logger.pojo.Log;
import logger.service.Logger;
public class Demo {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();

        logger.addLog(new Log("This is log 1",Severity.WARN));
        logger.addLog(new Log("This is log 2", Severity.LOW));
        logger.addLog(new Log("This is log 3", Severity.MEDIUM));
        logger.addLog(new Log("This is log 4", Severity.HIGH));
        logger.addLog(new Log("This is log 5", Severity.CRITICAL));

        logger.appendLog();

        logger.addLog(new Log("This is log 6", Severity.WARN));
        logger.addLog(new Log("This is log 7", Severity.LOW));
        logger.addLog(new Log("This is log 8", Severity.MEDIUM));
        logger.addLog(new Log("This is log 9", Severity.HIGH));
        logger.addLog(new Log("This is log 10", Severity.CRITICAL));

        logger.appendLog();

        logger.shutdown();


    }
}
```

## API overview
- `logger.service.Logger`
  - `static Logger getInstance()`
  - `void addLog(Log log)`
  - `void appendLog()`
- `logger.pojo.Log`
  - Fields: `data`, `timestamp`, `threadId`, `threadName`, `severity`, `stackTrace`
  - Ctors: `Log(String data)`, `Log(String data, Severity severity)`
- `logger.enums.Severity`: `LOW`, `HIGH`, `WARN`, `MEDIUM` , `CRITICAL` , `UNDEFINED`

## Behavior and data format
- `addLog` enriches entries with UTC timestamp, thread info, stack trace, default `LOW` severity if missing.
- `appendLog` deep-copies the in-memory set and writes asynchronously via `FileStore`.
- Persistence uses Java serialization to `log.txt` (binary, not line-oriented/human-readable).

## Example app
See `src/main/java/logger/App.java` for a runnable example that adds a few logs and flushes them.


## Caveats
- File format is Java serialization; inspect with `ObjectInputStream`, not a text editor.
- In-memory buffer is a `Set<Log>`; duplicates by identity are not stored twice.

## License 
MIT License. See `LICENSE` for full text.
