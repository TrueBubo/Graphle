#import "../template/shared.typ": *

= Introduction

Operating systems organize files as a strict tree of directories, where every
file resides at exactly one location. This imposes a single, rigid classification
onto data where the user has to choose the most appropriate aspect of the file and place it
into a corresponding folder. However, this is often difficult. A photograph may belong at once to a holiday,
a person, and a specific project. A tree cannot express such many-to-many associations, which makes organizing files
harder than it should be. Some tools try to bridge this gap, but each does so only
in its own limited way. They do not let users freely connect arbitrary files with typed
#voc("relationship", text: "relationships") and tags across the whole filesystem
in an easily queryable way.

People associate one thing with many areas at once, which a graph expresses more naturally than a tree.
If we treat files and folders as nodes joined by typed #voc("relationship", text: "relationships") and
annotated with #voc("tag", text: "tags"), a file still
resides in a single location, yet it can be referenced from anywhere in the graph.

The objective of this thesis is to design and implement Graphle, a graph-oriented
file management system that lets users organize their files the way they think.
This objective breaks down into five main goals:

- #emph[Requirements and related work.] Analyze the functional and qualitative
  requirements for graph-based file management, and survey existing file managers,
  knowledge-management applications, and navigation utilities in order to position
  Graphle among them and justify its design decisions.
- #emph[Graph data model.] Design a data model that represents files and folders as
  nodes in a labeled property graph connected by arbitrary, user-defined
  #voc("relationship", text: "relationships") and #voc("tag", text: "tags"), while
  remaining backward-compatible with the existing #voc("filesystem") so that files
  stay usable by ordinary tools.
- #emph[Query language.] Design a custom query #voc("dsl") that lets users search
  for files and manipulate their #voc("tag", text: "tags") and
  #voc("relationship", text: "relationships") by the graph structure rather than by
  paths alone.
- #emph[Backend.] Select and justify a suitable technology stack, and develop a
  backend that maintains the graph, exposes it through an API, and keeps it
  consistent with a live #voc("filesystem") that continues to be modified outside
  Graphle.
- #emph[Graphical client.] Develop an interactive graphical client that provides
  effective browsing of the #voc("filesystem"), management of files, editing of
  #voc("tag", text: "tags") and #voc("relationship", text: "relationships"), and
  execution of #voc("dsl") queries in one place.

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
