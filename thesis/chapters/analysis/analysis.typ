#import "../../template/shared.typ": *
= Analysis
Standard #link(label("voc_filesystem"))[filesystems] organize files in a strict hierarchy of folders, forcing users to pick a single location 
for each file and making it difficult to express #link(label("voc_relationship"))[relationships] that span multiple topics or projects.
This section provides the analytical documentation for an application designed
to extend existing filesystems with #link(label("voc_relationship"))[relationships]
and #link(label("voc_tag"))[tags], enabling graph based navigation without replacing or breaking the underlying directory structure.
It targets both regular users who benefit from a more expressive GUI file browser, and power users such as developers and system administrators 
who can automate operations via the provided #link(label("voc_dsl"))[DSL].
The section defines functional and qualitative requirements, including work with semantic and hierarchical #link(label("voc_connection"))[connections], 
the interaction with the system, and its qualities. It explains how the system handles user accounts
and their file permissions. In the last subsection, I show possible interactions via use cases,
including creating and deleting semantic information, browsing through the #link(label("voc_filesystem"))[filesystem], and using
the #link(label("voc_dsl"))[DSL].

#include "requirements.typ"

#include "data-model.typ"

#include "architecture.typ"

#include "user-roles.typ"

#include "security.typ"

#include "use-cases.typ"
