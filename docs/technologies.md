# Technologies
Various technologies are used to help reach the best possible outcome in this project. 
The Criteria used are written next to each technology.

## Programming language: [Kotlin](https://kotlinlang.org/)
The project aims to support all the major desktop operating systems. To achieve this, the language chosen needed to be fully cross-platform.
Being fully cross-platform ensures users will not deal with configuration specific bugs.
This reduces the chance of a system failure not being revealed during routine developer testing. 

The language should be suitable for building large-scale desktop GUI applications with a large community using it. 
Other developers should be able to extend the project with new features. Hence, a niche language without a community should not be used. 
At the start of this project, the only options were [Java](https://www.java.com/) and Kotlin. 

Kotlin supports every Java library, and more. This allows the project developers to choose between a vast span of
functionalities, they will not need to implement themselves. Therefore, they can spend more time on what really matters, 
the project core domain. Kotlin's type system is also more expressive. 
This is especially useful since it can make many illegal states unrepresentable. 
Therefore, the client server communication will be less error-prone due to adhering to a more robust contract.

## GUI: [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/)
Modern GUI library in Kotlin. It is the most popular GUI library for Kotlin. Hence, it will be the easiest library to find 
other developers to help with this project. 

Another benefit of this library is sharing of code between multiple device types.
This makes it much easier for a contributor to port the front-end to web or mobile.

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

## DSL: [Kotlin](https://kotlinlang.org/)
The application provides autocompletion for the filenames, which requires character-by-character parsing of the commands.
The autocomplete needs to know about possible options. They are stored in the database, which is handled by the server.
Henceforth, the language used is the language of the API, which provides the GUI via the API.

## [Relationships](vocabulary.md/#relationship) between files: [Neo4J](https://neo4j.com/)
The software enables users to create their own web of relationships. To enable this to be done efficiently, the program uses
a graph data model to represent data. The project had two types to choose from: LPG and RDF. The problem with RDF is that 
it forces users to use URIs to identify relations****hips. This is a hurdle users should not have to deal with when they are not 
sharing their file systems with others. RDF does not store its data as graphs and hence would be [slower](https://neo4j.com/blog/knowledge-graph/rdf-vs-property-graphs-knowledge-graphs/) for
this application. The only [popular](https://survey.stackoverflow.co/2024/technology#most-popular-technologies-database)
self-hosted graph first database is Neo4J, and hence it is used.

## Autocomplete: [Valkey](https://valkey.io/)
Due to the need of a character by character autocomplete, the API must respond to a request without a user having to wait. 
The cutoff was set as the human reaction time. It hovers around [250ms](https://humanbenchmark.com/tests/reactiontime/statistics). 
To achieve this, the application uses an in-memory key-value database. The Valkey database was chosen. 
It is a fork of [Redis](https://redis.io/), hence developers knowing Redis will know how to use it.
In many benchmarks it is [faster and more memory efficient](https://redisson.pro/blog/valkey-vs-redis-comparision.html) than Redis.
After the licence change fiasco, many companies such as 
[Oracle, AWS, and Google](https://www.thestack.technology/redis-fork-valkey-linux-foundation/) embraced it. The corporate
backing gives it higher credibility as they now need to rely on it as well.

To save space and support infix completion, a modified suffix tree is used. The suffix tree saves only bottom-level names without parents
to utilize efficient prefix search. Parents are saved as a link to word-ending nodes.
