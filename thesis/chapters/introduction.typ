#import "../template/shared.typ": *

= Introduction

#todo[Include above includes this function]

Operating systems organize files as a strict tree of directories, where every
file resides at exactly one location. This imposes a single, rigid classification
onto data where the user has to choose the most appropriate aspect of the file and place it
into a corresponding folder. However, this is not always so easy, for example
a photograph may belong at once to a holiday, a person, and a specific project.
A tree cannot express such many-to-many associations, which makes organizing files
harder than it should be. Some tools try to bridge this gap, but each does so only
in their own limited way. They do not let a user freely connect any files with typed
#voc("relationship", text: "relationships") and tags across the whole filesystem
which would be easily queryable.

People associate one thing with many areas at once, which a graph expresses more naturally than a tree.
If we treat files and folders as nodes joined by typed #voc("relationship", text: "relationships") and
annotated with #voc("tag", text: "tags"), a file still
resides in a single location, yet it can be referenced from anywhere in the graph.

The objective of this thesis is to design and implement Graphle, a graph-oriented
file management system that lets users organize their files the way they think.
Graphle represents files and folders as nodes in a labeled property graph connected by
arbitrary, user-defined #voc("relationship", text: "relationships") and
#voc("tag", text: "tags"), while remaining backwards compatible with the existing
#voc("filesystem"). It enables two modes of interaction, either a graphical client or a
custom query #voc("dsl"). The thesis delivers a working client and backend, a graph
data model and query language for files, while simultaneously being backward-compatible with directory hierarchy.

#emph[Outline.] The remainder of this thesis is organized into six chapters.
@analysis establishes the functional and qualitative requirements, user roles,
security model, and use cases. @landscape surveys existing file managers,
knowledge-management applications, and navigation utilities, and compares them with Graphle.
@design covers the graph data model,
architecture, module decomposition, technologies, and mockups of the user interface.
@developer documents the backend and client implementations, the API, and the key
algorithms. @user covers installation, the user
interface, and a tutorial for both the graphical and DSL workflows. Finally,
@conclusion summarizes the contributions and outlines future work.
