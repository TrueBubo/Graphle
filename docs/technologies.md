# Technologies
Various technologies are used to help reach the best possible outcome in this project. 
The Criteria used are written next to each technology.

## Programming language: [Java](https://www.java.com/)
The project aims to support all the major desktop operating systems. To achieve this, the language chosen needed to be fully cross-platform.
Being fully cross-platform enables the users to not have to deal with bugs, that only happen on specific configurations.
This reduces the chance of a system failure not being revealed during routine developer testing. 
The language should be suitable for building large-scale desktop GUI applications with a large community using it. 
Others should be able to extend the project. Hence, the language used should not be a niche language without a community. 
At the start of this project, the only options were Java and [Kotlin](https://kotlinlang.org/). 
The most famous GUI library for Kotlin is [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), which can be used also for developing desktop apps.
However, its look gives an android vibe. That is undesirable for an application, that will be run only on desktop computers. 
Other large GUI libraries for Kotlin are mostly ports of Java GUI libraries. Hence, 
the developers would get a better experience developing it directly in Java.

## GUI: [JavaFX](https://openjfx.io/)
Modern GUI library in Java. It is highly customizable using CSS. 
The project uses [AtlantaFX](https://mkpaz.github.io/atlantafx/).
It increases accessibility by providing more themes for a user to choose or create their own.

## API: [Spring](https://spring.io/)
To decouple the GUI from the logic as much as possible, the application would be separated into a GraphQL endpoint and
a GUI. This enables the application to query remote filesystems and enables 3rd party developers to develop their 
application on this application protocol.

GraphQL is used, so the application can request exactly what they want. This is crucial since the result can contain
millions of files, and it would be wasteful to query everything when we only need a name. It is possible for the project
to change the structure of a response, which would require a new version of an API if the project used REST. GraphQL
provides a schema that ensures a total transparency with the users.

Spring provides a whole ecosystem for creating APIs, including GraphQL support and security management. It is a popular framework,
meaning it is easy to find developers who can work on this project.

## DSL: [Java](https://www.java.com/)
The application provides autocompletion for the filenames, which requires character-by-character parsing of the commands.
The autocomplete needs to know about possible options. They are stored in the database, which is handled by the server.
Henceforth, the language used is the language of the API, which provides the GUI via the API.

## [Relationships](vocabulary.md/#relationship) between files: [Neo4J](https://neo4j.com/)
The software enables users to create their own web of relationships. To enable this to be done efficiently, the program uses
a graph data model to represent data. The project had two types to choose from: LPG and RDF. The problem with RDF is that 
it forces users to use URIs to identify relationships. This is a hurdle users should not have to deal with when they are not 
sharing their file systems with others. RDF does not store its data as graphs and hence would be [slower](https://neo4j.com/blog/knowledge-graph/rdf-vs-property-graphs-knowledge-graphs/) for
this application. The only [popular](https://survey.stackoverflow.co/2024/technology#most-popular-technologies-database)
self-hosted graph first database is Neo4J, and hence it is used.

## Autocomplete: [Kryo](https://github.com/EsotericSoftware/kryo)
Due to the need of a character by character autocomplete, the API must respond to a request without a user having to wait. 
The cutoff was set as the human reaction time. It hovers around [250ms](https://humanbenchmark.com/tests/reactiontime/statistics). To achieve this, the application uses an
in-memory data structure. To save space, the suffix tree is used. The suffix tree saves only bottom-level names without parents
to utilize efficient prefix search. Parents are saved as a link in word-ending nodes.

In-memory databases such as Redis were not used due to the bottleneck of verifying the files still existing. Therefore,
it would not significantly boost performance, even though it would require more space.

Serialization is used to save the suffix tree. Since the memory consumption and serialization performance are of
paramount importance, the Kryo serialization is used.
