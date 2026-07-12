#import "../template/shared.typ": *
= Conclusion <conclusion>

This thesis set out to design and implement Graphle, a graph-oriented file management
system that lets users organize files by semantic association while being backward-compatible
with the standard #voc("filesystem"). Graphle keeps files in ordinary folders and adds a
graph layer for #voc("tag", text: "tags") and typed #voc("relationship", text: "relationships"). This lets users
search and move between files by meaning, not only by path, without replacing the
#voc("filesystem"). The introduction stated five main goals. The following paragraphs
summarize how each of them was fulfilled.

#emph[Requirements and related work.] @analysis established the functional and
qualitative requirements, user roles, security model, and use cases that guided the
design. @landscape surveyed existing file managers, knowledge-management applications,
and navigation utilities, and showed that none of them lets users freely connect
arbitrary files with typed #voc("relationship", text: "relationships") and #voc("tag", text: "tags") across
the whole #voc("filesystem") in an easily queryable way, which positions Graphle in the
gap between them.

#emph[Graph data model.] The resulting model represents files and folders as nodes in a
#voc("lpg"), while file contents, operating-system #voc("metadata"), and the native
hierarchy stay on disk. Persisted graph data is limited to the semantic information
added by the user. Parent and descendant #voc("relationship", text: "relationships") are
derived from the live #voc("filesystem") when needed, which keeps Graphle
backward-compatible with existing tools and avoids duplicating data already maintained
by the operating system. This model gives each file a stable place in the hierarchy
while still allowing it to participate in many independent associations.

#emph[Query language.] The custom #voc("dsl") provides a command-based way to search for
files and manipulate their #voc("tag", text: "tags") and #voc("relationship", text: "relationships").
It serves users who need more complex queries than the graphical interface
offers, and it can be used both from the graphical client and in the standalone form in scripts.

#emph[Backend.] The implemented backend maintains the graph, exposes it through
#voc("graphql"), #voc("rest"), and #voc("websocket") interfaces, and addresses
consistency with the live #voc("filesystem") by combining lazy loading with background
maintenance. Files are loaded into the graph only when they are visited or queried, so
startup does not require a full disk scan, and a background sweeper removes graph
entries for files that have disappeared from the underlying #voc("filesystem"),
preventing stale relationships and tags from accumulating after external changes.
Together, these mechanisms keep Graphle usable alongside ordinary filesystem tools.

#emph[Graphical client.] The implemented GUI lets users browse the filesystem, open,
move, and delete files, and edit #voc("tag", text: "tags") and #voc("relationship", text: "relationships") in
one place. Its command line stays synchronized with the graphical navigation, showing
the #voc("dsl") command corresponding to the displayed data, so the client also serves
as an entry point into the query language.

In conclusion, Graphle shows that files can stay in the existing #voc("filesystem") while being
organized and explored as a graph. This thesis provides the data model, architecture,
query language, user interface, and implementation for that approach. It also creates a base for
better clients and for testing how useful graph-based file organization is in practice.

The most important future extension is application-level authentication and authorization for remote access.
The current implementation supports remote use through SSH port forwarding and relies on operating-system accounts and filesystem permissions.
A more broadly deployed version should add a uniform authentication layer across the #voc("graphql"), #voc("rest"), and #voc("websocket") interfaces so that the backend can protect the exposed #voc("filesystem") even when it is reachable by remote clients.

Another natural extension is Windows support. As noted in the #link(label("functional_requirements"))[functional requirements],
Windows was excluded from the scope of this thesis because its path semantics and permission system differ from those of
macOS and Linux and were not tested during development, not because of a fundamental limitation of the underlying model.
Adding Windows support would mean validating path handling, file permission mapping, and the installation steps on that platform.