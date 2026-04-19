#import "../template/shared.typ": *

= Current landscape <landscape>

This section examines existing tools that partially address the same user needs as Graphle.
The comparison is structured around the functional requirements established in the
#link(label("functional_requirements"))[Functional Requirements] section.
The criteria used for comparison are those that most directly distinguish the tools.
Those include arbitrary #link(label("voc_relationship"))[relationship] support between files,
#link(label("voc_tag"))[tag] support,
a query #link(label("voc_dsl"))[DSL],
and remote access, which is relevant to users who manage files on headless servers or NAS devices.

== Traditional File Managers

To understand the problem space, we need to assess the current solutions of file managers. As representative of
file managers I have chosen Finder for macOS @findermac, and Nautilus @gnomefiles and Dolphin @dolphin for Linux desktop
environments.
All three provide a graphical directory browser with file manipulation support.
Finder exposes the macOS tagging system, which persists tags as extended filesystem attributes.
Nautilus and Dolphin offer no comparable built-in tagging.
All three support remote filesystem access. Finder connects via _Connect to Server_ (supporting SFTP and SMB),
while Nautilus and Dolphin accept `sftp://` URIs directly, allowing users to browse files on remote machines
and NAS devices as if they were local.

The closest approximation to relationships available in all three managers is the symbolic link,
a pointer that makes a file appear at a location not adjacent to the linked file.
Symbolic links require no special application support.
However, they are untyped, as they carry no label describing why the
two locations are related, and the filesystem keeps no index of what points to a given target,
making them non-queryable as a first-class concept.
Finding all files symbolically linked to a given target requires a full recursive filesystem scan,
which does not scale.

Dolphin exposes an integrated Konsole panel @dolphinkonsole that can be toggled inside the file manager window,
giving users a terminal session whose working directory follows the GUI's current folder.
The standard shell commands available, such as `cd`, `mv`, `find`, can be used to navigate the filesystem,
however they only operate on paths and are not able to navigate based on semantic information. Therefore, it
receives a partial rating for DSL part.

== Obsidian

Obsidian @obsidian is a personal knowledge management application built on top of a local folder
of Markdown files.
Its central feature is a graph view that visualises the `wikilink` connections users embed
in their notes, establishing explicit, browsable relationships between documents.
Because links are stored as plain text, the vault remains readable without the application.
Tags are supported via `#hashtag` syntax or YAML front matter.

Despite the graph view, Obsidian is scoped to Markdown files.
It cannot express relationships between arbitrary file types such as PDFs, images,
or source code files, and folders cannot be tagged or linked as first-class entities.
Because Obsidian stores its link index only for Markdown vaults, non-Markdown files in the same
folder are invisible to the graph, hence the _Partial_ rating for _Existing FS_ in the
comparison table below.
The Dataview @dataview community plugin adds a query language over file metadata,
but its queries are still confined to Markdown files. PDFs, images, or a binary cannot
appear as a node in a Dataview query.

== TagSpaces

TagSpaces @tagspaces is a cross-platform, open-source file manager that adds a tagging
layer directly on top of the existing filesystem.
Rather than maintaining a separate database, it encodes tags either in the file name
itself (e.g.~`report[tax 2024].pdf`) or in a sidecar JSON file placed next to the target,
keeping the metadata portable and readable without the application.
It supports any file type and includes a built-in viewer for common formats such as images,
PDFs, and plain text.

TagSpaces has no concept of directed, typed connections between individual files.
Files that share a tag can be gathered through a tag-intersection search, but there is no way
to express that _document A cites document B_ or that _image X was exported from project Y_.
Its query model is limited to tag and filename matching, and there is no composable DSL for
queries spaning multiple edges. The application has no remote-access capability.

== autojump

autojump @autojump is a shell utility that learns which directories a user visits frequently
and allows rapid navigation to them by typing only a partial directory name.
Internally it maintains a weighted database of visited paths. The `j <query>` command
jumps to the highest-ranked directory whose name contains the query string.
This directly addresses quick navigation without requiring the user to remember or type full paths.

autojump is limited to directories and operates only on what the shell has explicitly visited.
It carries no concept of relationships between files or directories, no tagging, and no means of
querying the filesystem beyond frequency-ranked prefix matching.
It is a navigation aid rather than a file management tool, and it provides no graphical interface.

== Comparison

#figure(
  table(
    columns: (auto, 1fr, 1fr, 1fr, 1fr, 1fr, 1fr),
    align: (left, center, center, center, center, center, center),
    table.header(
      [*Tool*],
      [*Relationships*],
      [*Tags*],
      [*Existing FS*],
      [*DSL*],
      [*Autocomplete*],
      [*Remote Access*],
    ),
    [Finder],            [No],      [Yes],     [Yes],     [No],      [No],    [Yes],
    [Nautilus],          [No],      [No],      [Yes],     [No],      [No],    [Yes],
    [Dolphin],           [No],      [No],      [Yes],     [Partial], [No],    [Yes],
    [Obsidian],          [Partial], [Yes],     [Partial], [Plugin],  [No],    [No],
    [TagSpaces],         [No],      [Yes],     [Yes],     [No],      [No],    [No],
    [autojump],          [No],      [No],      [Yes],     [No],      [Yes],   [No],
    [*Graphle*],         [*Yes*],   [*Yes*],   [*Yes*],   [*Yes*],   [*Yes*], [*Yes*],
  ),
  caption: [
    Comparison of existing tools against selected requirements.
  ]
)

None of the surveyed tools satisfies the full requirement set.
Standard file managers provide no relationship or tagging model beyond OS-level tags on macOS

Obsidian is the closest conceptual match, a graph of linked documents with tags and partial query
support. However, it is constrained to Markdown notes and cannot work with arbitrary file types in its graph.
TagSpaces adds tagging to any file type but has no relationship model and no query DSL beyond
tag and filename matching.
autojump demonstrates that traversing through links through the whole filesystem is useful for
navigation, but it has no file management capabilities, no relationship model, and no GUI.

Graphle addresses the gap left by all three by extending a standard filesystem with #link(label("voc_relationship"))[relationships],
tags, a #link(label("voc_dsl"))[DSL] with filename autocompletion, and remote access.