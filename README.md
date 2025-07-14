# WebProbe

A modular keyword-based web crawler built with Kotlin, Spring Boot, and Docker.  
WebProbe crawls websites based on given keywords, stores results in MongoDB, and can use a local LLM to summarize the content.

## 🔧 Features

- Modular architecture (`application`, `crawler`, `data`, `llm`, `common`)
- Keyword-based crawling using OkHttp + Jsoup
- MongoDB integration for storage
- Spring Boot-based backend
- Docker Compose support for development & deployment
- Optional integration with local LLMs (e.g. Mistral via llama.cpp)

## 🗂️ Project Structure

<pre>
webprobe/
    ├── application     # Spring Boot entry point
    ├── crawler         # Keyword-based crawling logic
    ├── data            # MongoDB I/O and entities
    ├── llm             # Summarization with local LLM
    ├── common          # Shared models and constants
    └── docker          # Docker Compose setup
</pre>


## 🚀 Getting Started

```bash
# 1. Clone the repository
git clone https://github.com/your-username/webprobe.git
cd webprobe

# 2. Build the application JAR
./gradlew build

# 3. Run everything with Docker
docker compose up
```

## 🧪 Running Tests
```bash
./gradlew test
```

## 📄 License
This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for details.