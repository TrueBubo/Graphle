# Graphle
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-8A2BE2)
![GitHub License](https://img.shields.io/github/license/TrueBubo/Graphle) 

A cross-platform file management system built on a graph-based data model, designed as an alternative to traditional hierarchical file systems.

## 🔍 Overview
Conventional file systems organize data in a strict tree hierarchy, where each file resides in a single parent directory. This model lacks flexibility when modeling complex relationships, such as cross-references, tags, or semantic links between files.

This project introduces a graph-oriented approach, where files and folders are treated as fully connected nodes in a graph. The system supports rich, many-to-many relationships, enabling more expressive organization, querying, and manipulation of data.

## 🚀 Features
- Graph-based data structure: Files and folders are nodes; relationships are typed edges
- Tagging and semantic links: Support for user-defined tags and cross-file references
- Cross-platform: Runs on major OSes with consistent functionality
    
## 📝 TODOs
- External references: Integration of links to files outside the system
- Graphical User Interface (GUI): Intuitive visualization and manipulation of the file graph
- Custom DSL: Domain-specific language for querying and interacting with the graph, including autocompletion and dynamic loading

## 🧰 Built With
You can view the rationale for technologies in [docs/technologies.md](docs/technologies.md)
- Kotlin
- Spring Boot
- KMP
- Neo4J
- Redis

## 📚 Documentation
You can view project documentation in [docs/](docs/)
