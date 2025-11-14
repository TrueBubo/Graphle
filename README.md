# Graphle
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-8A2BE2)
![GitHub License](https://img.shields.io/github/license/TrueBubo/Graphle)

**Organize your files the way you think** - A graph-based file management system that breaks free from traditional folder hierarchies.

## 🔍 Overview

Conventional file systems force you into rigid tree structures where you can access each file from exactly one location.
Graphle handles your data with a **graph-based approach** where files and folders become interconnected nodes with rich, many-to-many relationships.

Create meaningful connections between your files, add custom tags, build semantic networks, and query your data in ways 
traditional file systems never could.

## ✨ Features

### Graph-Based Organization
- **Expressive relationships**: Connect files and folders with typed edges (related-to, depends-on, contains, references, etc.)
- **No single-parent limitation**: Files can exist in multiple contexts simultaneously
- **Rich metadata**: Attach custom properties and attributes to any node
- **Flexible hierarchy**: Build organizational structures that match your mental model

### Smart Connectivity
- **Tagging system**: Create and apply custom tags for flexible categorization
- **Semantic links**: Build meaningful connections between related files and concepts
- **Bidirectional relationships**: Navigate connections in any direction
- **Relationship types**: Define custom relationship types for your specific use cases

### Powerful Querying
- **Custom DSL**: Domain-specific language designed for intuitive graph queries
- **GraphQL API**: Modern, flexible API for programmatic access and integrations with your own apps
- **Complex searches**: Find files by relationships, tags, properties, and connection patterns
- **Path discovery**: Explore indirect connections between files

### Performance & Scalability
- **Neo4j graph database**: Industry-leading graph database for efficient relationship traversal
- **Valkey caching**: High-performance caching layer for fast queries
- **Optimized queries**: Built for speed even with large file collections

### Cross-Platform Support
- **Consistent experience**: Works seamlessly across Linux, macOS, and FreeBSD
- **Kotlin Multiplatform**: Shared codebase ensures feature parity across platforms
- **Modern architecture**: Built with Spring Boot for reliability and extensibility

## 🎯 Use Cases

**For Researchers & Academics**
- Link papers, citations, and research notes across multiple projects
- Build knowledge graphs from your literature reviews
- Track methodologies and findings with semantic relationships

**For Creative Professionals**
- Connect assets, references, and deliverables across projects

**For Developers**
- Link code to documentation, issues, and related modules
- Model dependencies and architectural relationships
- Organize projects by feature, technology, or any custom taxonomy

**For everyone else**
- Build personal knowledge management systems
- Create interconnected note networks
- Discover hidden connections in your information landscape

## 🚀 Getting Started

### Prerequisites
- JDK 21 or higher
- Neo4j database
- Valkey

### Installation

```bash
# Clone the repository
git clone https://github.com/TrueBubo/Graphle.git
cd Graphle

# Start Neo4j and Valkey

# Build and run the backend
cd GraphleManager
docker compose up -d # Skip if you got the database already running
./gradlew bootRun

# To run the GUI
# Go to the directory to where you have cloned the repository
cd GraphleUI
./gradlew run

```

The GraphQL API will be available at `http://localhost:5824/graphql`. To change the port, change it on the 
[server](GraphleManager/src/main/resources/application.properties), and on 
[client](GraphleUI/src/main/resources/config.yaml)

For detailed setup instructions and configuration options, see the [documentation](docs/).

## 🧰 Technology Stack

Built with modern, battle-tested technologies. View the detailed rationale in [docs/technologies.md](docs/technologies.md).

- **[Kotlin](https://kotlinlang.org/)** - Type-safe, expressive language for robust development
- **[Spring Boot](https://spring.io/projects/spring-boot)** - Enterprise-grade backend framework
- **[Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html)** - Cross-platform UI with code sharing
- **[Neo4j](https://neo4j.com/)** - World's leading graph database
- **[Valkey](https://valkey.io/)** - High-performance caching for faster responses
- **[GraphQL](https://graphql.org/)** - Flexible, efficient API layer

## 📚 Documentation

Comprehensive documentation to help you get the most out of Graphle:

- **[API Reference](docs/api/)** - Complete GraphQL schema and query examples
- **[Architecture](docs/architecture/)** - Diagrams of the system design
- **[Use Cases](docs/use-cases/)** - Real-world scenarios and workflows
- **[Requirements](docs/requirements/)** - Detailed functional and qualitative specifications
- **[Vocabulary](docs/vocabulary.md)** - Key concepts and terminology
- **[Technologies](docs/technologies.md)** - In-depth rationale for our tech choices

## 🤝 Contributing

Contributions are welcome! Whether it's bug reports, feature requests, or code contributions, we appreciate your input.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the terms in the [LICENSE](LICENSE) file.

---

**Think in graphs. Organize in graphs.** Try Graphle.
