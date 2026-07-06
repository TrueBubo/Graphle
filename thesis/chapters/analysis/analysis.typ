#import "../../template/shared.typ": *
= Analysis <analysis>
A standard #voc("filesystem") organizes files in a strict hierarchy of folders.
This forces users to pick a single location for each file and makes it difficult to express a #voc("relationship") that spans multiple topics or projects.
This section provides the analytical documentation for an application designed
to extend existing filesystems with #voc("relationship", text: "relationships")
and #voc("tag") support, enabling graph-based navigation without replacing or breaking the underlying directory structure.
It targets both regular users who benefit from a more expressive GUI file browser and power users such as developers and system administrators
who can automate operations through the provided #voc("dsl").
The section defines functional and qualitative requirements, including semantic and hierarchical #voc("connection") support, 
interaction with the system, and its qualities. It explains how the system handles user accounts
and their file permissions. The last subsection shows possible interactions through use cases,
including creating and deleting semantic information, browsing through the #voc("filesystem"), and using
the #voc("dsl").

#include "requirements.typ"

#include "user-roles.typ"

#include "security.typ"

#include "use-cases.typ"
