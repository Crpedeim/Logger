# Logger - AI-Powered Log Management System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.x-blue.svg)](https://maven.apache.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-0.32.0-green.svg)](https://github.com/langchain4j/langchain4j)

A sophisticated Java logging system that leverages **vector embeddings** and **Retrieval-Augmented Generation (RAG)** to enable semantic search and AI-powered log analysis. Ask natural language questions about your logs and get intelligent, context-aware answers.

## 🌟 Features

- **Semantic Log Search**: Query logs using natural language instead of complex regex patterns
- **AI-Powered Analysis**: Get intelligent answers to questions about your logs using Mistral AI
- **Vector Embeddings**: Automatically converts logs into vector embeddings for similarity search
- **Asynchronous Processing**: Non-blocking log ingestion with thread pool executor
- **Severity Categorization**: Built-in severity levels (CRITICAL, HIGH, MEDIUM, LOW, WARN)
- **Rich Metadata**: Automatically captures timestamp, thread ID, thread name, and stack traces
- **Singleton Pattern**: Thread-safe logger instance management
- **Multiple Storage Options**: Support for both file-based and vector store datastores

## 📋 Table of Contents

- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [How It Works](#how-it-works)
- [Dependencies](#dependencies)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## 🏗️ Architecture

```
┌─────────────────┐
│  Application    │
└────────┬────────┘
         │ addLog()
         ▼
┌─────────────────┐
│     Logger      │◄── Singleton Pattern
│   (Service)     │
└────────┬────────┘
         │ appendLog() (async)
         ▼
┌─────────────────┐
│ VectorStore     │
│  Datastore      │◄── Embeds logs as vectors
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌──────────────────┐
│  Embedding      │────►│ InMemory Vector  │
│    Model        │     │      Store       │
│ (AllMiniLm-L6)  │     └──────────────────┘
└─────────────────┘
         │
         │ Query with natural language
         ▼
┌─────────────────┐     ┌──────────────────┐
│  RAG Query      │────►│  Log Assistant   │
│   Processor     │     │  (Mistral AI)    │
└─────────────────┘     └──────────────────┘
         │
         │ Returns relevant logs + AI answer
         ▼
┌─────────────────┐
│   Application   │
└─────────────────┘
```

### Core Components

1. **Logger**: Singleton service that manages log collection and storage
2. **VectorStoreDatastore**: Converts logs into vector embeddings and stores them
3. **RAGqueryProcessor**: Implements semantic search over log embeddings
4. **LogAssistant**: Uses LLM to provide intelligent answers about logs
5. **Datastore Interface**: Abstraction for different storage backends

## ✅ Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6.x or higher
- **API Key**: Mistral AI API key (for log assistant features)

## 📦 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Crpedeim/Logger.git
cd Logger
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Configure API Key

The project uses Mistral AI for log analysis. You can:

**Option A**: Set environment variable
```bash
export MISTRAL_API_KEY="your-api-key-here"
```

**Option B**: Update the API key in `LogAssistant.java` (line 23)
```java
this.chatModel = MistralAiChatModel.builder()
    .apiKey(System.getenv("MISTRAL_API_KEY")) // Use environment variable
    .modelName("mistral-small-latest")
    .timeout(Duration.ofSeconds(60))
    .build();
```

## 🚀 Quick Start

```java
import logger.service.Logger;
import logger.service.LogAssistant;
import logger.pojo.Log;
import logger.enums.Severity;
import logger.data.VectorStoreDatastore;
import queryProcessor.RAGqueryProcessor;

public class QuickStartExample {
    public static void main(String[] args) {
        // 1. Get logger instance
        Logger logger = Logger.getInstance();

        // 2. Add logs
        logger.addLog(new Log(
            "Database connection failed after 3 retries", 
            Severity.CRITICAL
        ));
        logger.addLog(new Log(
            "User authentication successful", 
            Severity.LOW
        ));

        // 3. Persist logs asynchronously
        Future<?> future = logger.appendLog();
        future.get(); // Wait for completion

        // 4. Query logs with natural language
        QueryProcessor processor = new RAGqueryProcessor(
            (VectorStoreDatastore) logger.getVectorStore()
        );
        LogAssistant assistant = new LogAssistant(processor);

        // 5. Ask questions!
        String answer = assistant.answer("What database issues occurred?");
        System.out.println(answer);
    }
}
```

## 💡 Usage Examples

### Example 1: Basic Logging

```java
Logger logger = Logger.getInstance();

// Log with severity
logger.addLog(new Log("Payment gateway timeout", Severity.HIGH));
logger.addLog(new Log("Cache miss on user profile", Severity.WARN));
logger.addLog(new Log("Daily backup completed", Severity.LOW));

// Flush logs to vector store
Future<?> task = logger.appendLog();
task.get(); // Wait for async operation
```

### Example 2: Semantic Search

```java
// Setup query processor
VectorStoreDatastore datastore = (VectorStoreDatastore) logger.getVectorStore();
QueryProcessor processor = new RAGqueryProcessor(datastore);

// Search for similar logs
String query = "connection problems";
List<EmbeddingMatch<TextSegment>> results = processor.process(query);

// Print results
for (EmbeddingMatch<TextSegment> match : results) {
    System.out.printf("Similarity: %.2f%%\n", match.score() * 100);
    System.out.println("Log: " + match.embedded().text());
}
```

### Example 3: AI-Powered Log Analysis

```java
LogAssistant assistant = new LogAssistant(processor);

// Ask complex questions
String answer1 = assistant.answer(
    "What are the most critical issues in the last batch of logs?"
);

String answer2 = assistant.answer(
    "Are there any patterns in the authentication failures?"
);

String answer3 = assistant.answer(
    "Summarize all database-related errors"
);

System.out.println(answer1);
```

### Example 4: Batch Processing

```java
Logger logger = Logger.getInstance();

// Add multiple logs
for (int i = 0; i < 100; i++) {
    logger.addLog(new Log("Processing record " + i, Severity.LOW));
}

// Flush first batch
Future<?> batch1 = logger.appendLog();

// Add more logs while first batch is processing
for (int i = 100; i < 200; i++) {
    logger.addLog(new Log("Processing record " + i, Severity.LOW));
}

// Flush second batch
Future<?> batch2 = logger.appendLog();

// Wait for both batches
batch1.get();
batch2.get();
```

## ⚙️ Configuration

### Severity Levels

```java
public enum Severity {
    UNDEFINED,  // Default when not specified
    CRITICAL,   // System-breaking errors
    HIGH,       // Major issues requiring attention
    MEDIUM,     // Important but not urgent
    LOW,        // Informational messages
    WARN        // Warning conditions
}
```

### Query Processor Settings

Edit `RAGqueryProcessor.java` to customize:

```java
private static final int MAX_RESULTS_TO_RETRIEVE = 3;     // Top K results
private static final double MINIMUM_SIMILARITY_SCORE = 0.6; // Threshold
```

### Embedding Model

The system uses **AllMiniLmL6V2EmbeddingModel** by default. To change:

```java
// In VectorStoreDatastore.java
this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

// Or use OpenAI embeddings:
this.embeddingModel = OpenAiEmbeddingModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName("text-embedding-3-small")
    .build();
```

### Thread Pool Configuration

Modify in `Logger.java`:

```java
ExecutorService service = Executors.newFixedThreadPool(10); // 10 threads
```

## 📚 API Reference

### Logger Class

#### `Logger.getInstance()`
Returns the singleton logger instance.

#### `void addLog(Log log)`
Adds a log to the current batch. Automatically enriches with metadata.

**Parameters:**
- `log`: Log object containing message and severity

#### `Future<?> appendLog()`
Asynchronously persists the current batch of logs to the vector store.

**Returns:** Future object for tracking completion

#### `void shutdown()`
Gracefully shuts down the thread pool and closes resources.

### Log Class

#### Constructor
```java
Log(String message, Severity severity)
```

#### Automatically Captured Fields
- `timestamp`: When the log was created
- `threadId`: ID of the thread that created the log
- `threadName`: Name of the thread
- `stackTrace`: Full stack trace at log creation

### LogAssistant Class

#### `String answer(String query)`
Processes a natural language query and returns an AI-generated answer.

**Parameters:**
- `query`: Natural language question about logs

**Returns:** AI-generated answer based on relevant logs

## 🔍 How It Works

### 1. Log Ingestion
When you call `addLog()`, the logger:
- Captures current timestamp
- Records thread information
- Captures stack trace
- Adds the log to an in-memory batch

### 2. Vectorization
When you call `appendLog()`:
- Logs are converted to text segments with metadata
- The embedding model creates vector representations
- Vectors are stored in the in-memory vector database
- Process happens asynchronously

### 3. Semantic Search
When you query:
- Your query is converted to a vector embedding
- The system finds logs with similar vector representations
- Results are ranked by cosine similarity
- Top matches above threshold are returned

### 4. AI Analysis
The LogAssistant:
- Retrieves semantically similar logs
- Creates a context-rich prompt
- Sends to Mistral AI for analysis
- Returns natural language answer

## 📦 Dependencies

### Core Dependencies
- **LangChain4j** (0.32.0): Framework for LLM integration
- **Mistral AI** (0.32.0): Chat completion model
- **AllMiniLm-L6-V2**: Lightweight embedding model
- **Logback** (1.5.13): Logging implementation

### Optional Dependencies
- **OpenAI**: Alternative embedding/chat models
- **Chroma**: Persistent vector database
- **Ollama**: Local LLM support

## 🐛 Troubleshooting

### Common Issues

**Issue**: `IllegalStateException: Logger has not been initialized`
```java
// Solution: Call getInstance() before using
Logger logger = Logger.getInstance();
```

**Issue**: No relevant logs found
```java
// Solution: Lower the similarity threshold
private static final double MINIMUM_SIMILARITY_SCORE = 0.4;
```

**Issue**: Slow query response
```java
// Solution: Reduce max results
private static final int MAX_RESULTS_TO_RETRIEVE = 1;
```

**Issue**: API rate limits
```java
// Solution: Increase timeout or reduce requests
.timeout(Duration.ofSeconds(120))
```

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/Logger.git

# Create branch
git checkout -b feature/my-feature

# Make changes and test
mvn test

# Submit PR
```

## 📝 License

This project is part of MIT License. Please check the repository for license details.

## 🔗 Links

- **GitHub**: [https://github.com/Crpedeim/Logger](https://github.com/Crpedeim/Logger)
- **LangChain4j Docs**: [https://docs.langchain4j.dev/](https://docs.langchain4j.dev/)
- **Mistral AI**: [https://mistral.ai/](https://mistral.ai/)

