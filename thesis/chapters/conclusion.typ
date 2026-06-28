#import "../template/shared.typ": *
= Conclusion <conclusion>

This thesis set out to design and implement Graphle, a graph-oriented file management
system that lets users organize files by semantic association whilst being backwards-compatible
with the standard #voc("filesystem"). Graphle keeps files in ordinary folders and adds a
graph layer for tags and typed #voc("relationship", text: "relationships"). This lets users
search and move between files by meaning, not only by path, without replacing the
#voc("filesystem").

The main contribution is a working design in which files and folders are represented as
nodes in a #voc("lpg"), while file contents, operating-system #voc("metadata"), and the
native hierarchy stay on disk. Persisted graph data is limited to the semantic
information added by the user. Parent and descendant #voc("relationship", text:
"relationships") are derived from the live #voc("filesystem") when needed, which keeps
Graphle compatible with existing tools and avoids duplicating data already maintained by
the operating system. This model gives each file a stable place in the hierarchy while
still allowing it to participate in many independent associations.

Graphle supports two ways of working with files. The GUI lets users browse the
filesystem, open, move, and delete files, and edit tags and #voc("relationship", text:
"relationships") in one place. The custom #voc("dsl") provides a command-based
alternative for users who need more complex queries.

The system addresses consistency between the graph and the live #voc("filesystem") by
combining lazy loading with background maintenance. Files are loaded into the graph only
when they are visited or queried, so startup does not require a full disk scan. A
background sweeper removes graph entries for files that have disappeared from the
underlying #voc("filesystem"), preventing stale relationships and tags from accumulating
after external changes. Together, these mechanisms keep Graphle usable alongside ordinary
filesystem tools.

In conclusion, Graphle shows that files can stay in the existing #voc("filesystem") while being
organized and explored as a graph. This thesis provides the data model, architecture,
query language, user interface, and implementation for that approach. It also creates a base for
better clients and for testing how useful graph-based file organization is in practice.

The most important future extension is application-level authentication and authorization for remote access.
The current implementation supports remote use through SSH port forwarding and relies on operating-system accounts and filesystem permissions.
A more broadly deployed version should add a uniform authentication layer across the #voc("graphql"), #voc("rest"), and #voc("websocket") interfaces so that the backend can protect the exposed #voc("filesystem") even when it is reachable by remote clients.
