#import "../template/shared.typ": *

= Current landscape <landscape>

This section examines existing tools that partially address the same user needs as Graphle.
The comparison is structured around both the functional requirements established in the
#link(label("functional_requirements"))[Functional Requirements] section and the qualitative constraints established in
#link(label("qualitative_requirements"))[Qualitative Requirements].
The criteria therefore cover two groups.
The first group consists of support for typed #voc("relationship", text: "relationships"), #voc("tag", text: "tags"),
queries or a query #voc("dsl"), autocomplete, and whether the tool can operate on arbitrary files and folders rather than only on a special document type.
The second group consists of cross-platform availability, remote use, multiuser support, and storage model.
These criteria are not meant to cover every feature of the surveyed tools.
They are selected because they correspond to the requirements that most directly determine whether an existing tool can replace Graphle.

== Traditional File Managers

To understand the problem space, we need to assess the current solutions of file managers.
Finder for macOS @findermac, Nautilus @gnomefiles, and Dolphin @dolphin for Linux desktop
environments were chosen as representatives of file managers.
All three provide a graphical directory browser with file manipulation support.
They operate on arbitrary files and folders and inherit the permission model of the operating system account used to run them.
Finder exposes the macOS tagging system, which persists #voc("tag", text: "tags") as extended filesystem attributes.
Nautilus and Dolphin offer no comparable built-in tagging.
All three support remote filesystem access. Finder connects via _Connect to Server_ (supporting #voc("sftp") and #voc("smb")),
while Nautilus and Dolphin accept `sftp://` URIs directly, allowing users to browse files on remote machines
and NAS devices as if they were local.

The closest approximation to relationships available in all three managers is the #voc("symbolic_link"),
which can make a file appear at a different location.
#voc("symbolic_link", text: "Symbolic links") require no special application support.
However, they are untyped, as they carry no label describing why the
two locations are related, and the #voc("filesystem") keeps no index of what points to a given target,
making them non-queryable as a first-class concept.
Finding all files symbolically linked to a given target requires a full recursive #voc("filesystem") scan,
which does not scale.

Dolphin exposes an integrated Konsole panel @dolphinkonsole that can be toggled inside the file manager window,
giving users a terminal session whose working directory follows the GUI's current folder.
The standard shell commands available, such as `cd`, `mv`, and `find`, can be used to navigate the #voc("filesystem").
However, they only operate on paths and cannot navigate based on semantic information. Therefore, Dolphin
receives a partial rating for the DSL part.

== Obsidian

Obsidian @obsidian is a personal knowledge management application built on top of a local folder
of Markdown files.
Its central feature is a graph view that visualizes the `wikilink` connections users embed
in their notes, establishing explicit, browsable #voc("relationship", text: "relationships") between documents.
Because links are stored as plain text, the vault remains readable without the application.
#voc("tag", text: "Tags") are supported via `#hashtag` syntax or YAML front matter.
Its primary storage model is a local Markdown vault plus indexes built by the application and its plugins.

Despite the graph view, Obsidian is scoped to Markdown files.
It cannot express #voc("relationship", text: "relationships") between arbitrary file types such as PDFs, images,
or source code files, and folders cannot be tagged or linked as first-class entities.
Because Obsidian stores its link index only for Markdown vaults, non-Markdown files in the same
folder are invisible to the graph, hence the _Partial_ rating for arbitrary files and folders in the
functional comparison table below.
The Dataview @dataview community plugin adds a query language over file #voc("metadata"),
but its queries are still confined to Markdown files. PDFs, images, or binaries cannot
appear as a node in a Dataview query.

== TagSpaces

TagSpaces @tagspaces is a cross-platform file manager that adds a tagging
layer directly on top of the existing #voc("filesystem").
Rather than maintaining a separate database, it encodes tags either in the file name
itself (e.g.~`report[tax 2024].pdf`) or in a sidecar #voc("json") file placed next to the target,
keeping the #voc("metadata") portable and readable without the application.
It supports any file type and includes a built-in viewer for common formats such as images,
PDFs, and plain text.
This storage model is optimized for tags rather than graph traversal.

TagSpaces has no concept of directed, typed connections between individual files.
Files that share a #voc("tag") can be gathered through a tag-intersection search, but there is no way
to express that _document A cites document B_ or that _image X was exported from project Y_.
Its query model is limited to #voc("tag") and filename matching, and there is no composable #voc("dsl") for
queries spanning multiple edges. The application has no remote-access capability.

== TMSU

TMSU @tmsu is a CLI tagging tool that stores tags in its own database
and exposes them through a virtual #voc("filesystem").
Files remain unchanged in their original locations, while still allowing users to persist and query tags.
TMSU supports simple tags, tag values, and its own #voc("dsl"), making it a relevant example of using
tags over an ordinary #voc("filesystem").

The limitation is that the model remains centered on tags.
The mounted hierarchy provides a convenient alternative view, but it does not express typed
#voc("relationship", text: "relationships") such as _derived from_, _cites_, or _belongs to project_ between two
files or folders.
TMSU also has no dedicated graphical interface, no application-level remote-access workflow, and no shared
multiuser model.
It therefore demonstrates the usefulness of tag-based navigation, but does not cover the graph-oriented
file management model required by Graphle.

== autojump

autojump @autojump is a shell utility that learns which directories a user visits frequently
and allows rapid navigation to them by typing only a partial directory name.
Internally it maintains a weighted database of visited paths. The `j <query>` command
jumps to the highest-ranked directory whose name contains the query string.
This directly addresses quick navigation without requiring the user to remember or type full paths.

autojump is limited to directories and operates only on what the shell has explicitly visited.
It carries no concept of #voc("relationship", text: "relationships") between files or directories, no tagging, and no means of
querying the #voc("filesystem") beyond frequency-ranked prefix matching.
It is a navigation aid rather than a file management tool, and it provides no graphical interface.
It is useful as evidence that users benefit from cross-hierarchy navigation, but its storage model is based on an access frequency
rather than a semantic model of the #voc("filesystem").

== Comparison

#figure(
  table(
    columns: (auto, 1fr, 1fr, 1fr, 1fr, 1fr, 1fr),
    align: (left, center, center, center, center, center, center),
    table.header(
      [*Tool*],
      [*Relationships*],
      [*Tags*],
      [*Arbitrary files/folders*],
      [*Query/DSL*],
      [*Autocomplete*],
      [*Remote Access*],
    ),
    [Finder],            [No],      [Yes],     [Yes],       [No],      [No],    [Yes],
    [Nautilus],          [No],      [No],      [Yes],       [No],      [No],    [Yes],
    [Dolphin],           [No],      [No],      [Yes],       [Partial], [No],    [Yes],
    [Obsidian],          [Partial], [Yes],     [Partial],   [Plugin],  [No],    [No],
    [TagSpaces],         [No],      [Yes],     [Yes],       [No],      [No],    [No],
    [TMSU],              [No],      [Yes],     [Yes],       [Yes],     [No],    [No],
    [autojump],          [No],      [No],      [Dirs only], [No],      [Yes],   [No],
    [*Graphle*],         [*Yes*],   [*Yes*],   [*Yes*],     [*Yes*],   [*Yes*], [*Yes*],
  ),
  caption: [
    Functional comparison of existing tools against selected requirements.
  ]
)

#figure(
  table(
    columns: (auto, 1.2fr, 2.3fr, 2fr),
    align: (left, left, left, left),
    table.header(
      [*Tool*],
      [*Platform fit*],
      [*Storage model*],
      [*Multiuser support*],
    ),
    [Finder],
    [macOS only],
    [Native #voc("filesystem") plus macOS metadata such as tags and #voc("symbolic_link", text: "symbolic links")],
    [Uses macOS accounts and file permissions; no separate application users],

    [Nautilus],
    [Linux / GNOME],
    [Native #voc("filesystem") plus GVfs-backed remote locations],
    [Uses Linux accounts and file permissions; no separate application users],

    [Dolphin],
    [Linux / KDE],
    [Native #voc("filesystem") plus KIO-backed remote locations],
    [Uses Linux accounts and file permissions; no separate application users],

    [Obsidian],
    [macOS and Linux],
    [Markdown vault plus application and plugin indexes],
    [Primarily single-user local vaults; shared editing depends on optional sync or external storage],

    [TagSpaces],
    [macOS and Linux],
    [Filenames or sidecar #voc("json") metadata next to files],
    [Local or shared-folder use; access control depends on the underlying #voc("filesystem")],

    [TMSU],
    [Linux and Unix-like command-line environments],
    [Local tag database plus a mounted virtual #voc("filesystem") of tag views],
    [Primarily per-user local use; sharing depends on the underlying files and database coordination],

    [autojump],
    [macOS and Linux shells],
    [Local weighted database of visited directories],
    [Per-shell-user history database; no shared multiuser model],

    [*Graphle*],
    [*macOS and Linux*],
    [*Existing #voc("filesystem") plus Neo4j graph metadata and Valkey autocomplete index*],
    [*OS accounts and SSH can separate users; no application-level multiuser authorization in the thesis version*],
  ),
  caption: [
    Operational comparison of existing tools against selected requirements and constraints.
  ]
)

None of the surveyed tools satisfies the full requirement set.
Standard file managers provide no #voc("relationship") or tagging model beyond OS-level #voc("tag", text: "tags") on macOS.
They are also tied to a particular desktop environment or operating system, so they do not provide one cross-platform graph layer over both macOS and Linux filesystems.

Obsidian is the closest conceptual match, a graph of linked documents with #voc("tag", text: "tags") and partial query
support. However, it is constrained to Markdown notes and cannot work with arbitrary file types in its graph.
TagSpaces adds tagging to any file type but has no #voc("relationship") model and no query #voc("dsl") beyond
#voc("tag") and filename matching. Its filename and sidecar storage is transparent, but it does not provide a graph storage model.
TMSU adds a stronger tag-query and virtual-filesystem model, but remains tag-centric and lacks typed relationships,
a GUI, and a remote workflow.
autojump demonstrates that traversing links across the whole #voc("filesystem") is useful for
navigation, but it has no file management capabilities, no #voc("relationship") model, and no GUI.

Graphle addresses the gap left by the surveyed tools by extending a standard #voc("filesystem") with #voc("relationship", text: "relationships"),
#voc("tag", text: "tags"), a #voc("dsl") with filename autocompletion, and remote access.
It keeps file contents in place, stores semantic graph #voc("metadata") in a graph database, supports both files and folders as graph nodes, and relies on existing OS accounts and SSH rather than introducing a separate multiuser authorization model in the thesis implementation.
