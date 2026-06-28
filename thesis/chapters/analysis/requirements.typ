#import "../../template/shared.typ": *

== Functional and Qualitative Requirements

The requirements were defined based on an analysis of existing tools and their limitations.
Existing #voc("filesystem") tools and productivity applications were examined to identify potential enhancements that can be made to them.
The existing solutions will be discussed further in the #link(label("landscape"))[Current landscape] section.
The thesis was not commissioned by an external client. Instead, the author acted as the primary stakeholder for the project.
The requirements were derived from the author's experience with the problem domain and adjusted based on supervisor feedback and informal interviews with potential users.
The main goal of this project is to extend a standard #voc("filesystem") to support faster traversal by interpreting
#voc("relationship", text: "relationships") as an #voc("lpg").

=== Functional Requirements
This list serves as a feature list that this application should fulfill in order to be considered complete.
Each requirement has a priority, an acceptance criterion, and a link to the use case or chapter that explains it further.

<functional_requirements>
#table(
  columns: (auto, auto, 1.4fr, 2fr, 1.2fr),
  align: (left, left, left, left, left),
  stroke: 0.4pt + luma(160),
  inset: 4pt,
  table.header([*ID*], [*Priority*], [*Requirement*], [*Acceptance criterion*], [*See*]),
  [F1], [Must], [Backwards compatibility], [Graphle works on top of the existing #voc("filesystem") without copying file contents, and it exposes parent and child directory entries as graph neighbors.], [@uc-search-file],
  [F2], [Must], [#voc("relationship", text: "Relationships") between files], [The user can create a typed relationship between two files or folders, see it from the source file, traverse it, and remove it.], [@uc-add-relationship, @uc-remove-relationship],
  [F3], [Must], [File #voc("tag", text: "tags")], [The user can add a tag to a file or folder, find files by that tag, and remove it.], [@uc-add-tag, @uc-remove-tag],
  [F4], [Must], [File manipulation], [The user can create, open, delete, and move files through Graphle, and the change is reflected in the underlying #voc("filesystem").], [@uc-search-file],
  [F5], [Must], [Folders work as files], [A folder can be browsed, tagged, connected by relationships, moved, and deleted through the same workflows as a file.], [@uc-search-file, @uc-add-tag, @uc-add-relationship],
  [F6], [Must], [GUI file browser], [The user can open the GUI, navigate through neighbors, and select a file or folder.], [@uc-search-file],
  [F7], [Must], [Custom #voc("dsl")], [The user can execute DSL commands for search and graph operations, and the system returns either matching objects or a clear error.], [@uc-complex-dsl-query],
  [F8], [Should], [Filename autocompletion], [While the user types a DSL command, the system displays filename suggestions based on known or recently visited paths.], [@uc-command-autocomplete],
  [F9], [Must], [Lazy updates], [Files are loaded from the #voc("filesystem") when they are visited or queried, and files deleted outside Graphle are removed from the graph by later navigation or background cleanup.], [@uc-search-file],
  [F10], [Must], [Cross-platform support], [The backend and GUI can be built and run on macOS and Linux using the documented installation steps.], [@user],
)

Windows is explicitly not supported, as NTFS assigns each volume an independent drive letter rather than providing a single root hierarchy. 
Without a common root, files on different drives cannot be connected in the graph.  

=== Qualitative Requirements
Qualitative requirements define constraints on how the functionality should behave.

<qualitative_requirements>
#table(
  columns: (auto, auto, 1.4fr, 2fr, 1.2fr),
  align: (left, left, left, left, left),
  stroke: 0.4pt + luma(160),
  inset: 4pt,
  table.header([*ID*], [*Priority*], [*Requirement*], [*Acceptance criterion*], [*See*]),
  [Q1.1], [Must], [Usability - Interaction modes], [Finding files, editing tags, and editing relationships are available through both the GUI and the DSL.], [@uc-search-file, @uc-complex-dsl-query],
  [Q1.2], [Should], [Usability - Theming and replaceable client], [The default GUI multiple visual themes, and another client can use the public API without direct database access.], [@design, @developer],
  [Q1.3], [Should], [Usability - Remote access], [A user can connect to a remote instance of `GraphleManager` via SSH port forwarding and browse the #voc("filesystem") of the machine where the backend runs.], [@uc-remote-access],
  [Q2.1], [Must], [Performance - Autocomplete latency], [For an established GUI connection and warm caches, filename autocomplete returns suggestions within 250 ms.], [@uc-command-autocomplete],
  [Q3.1], [Should], [Reliability - Autocomplete retries], [The GUI can reconnect to the autocomplete #voc("websocket") without restarting the application.], [@uc-command-autocomplete],
  [Q3.2], [Must], [Reliability - Component isolation], [If an auxiliary component such as Valkey or a GUI client fails, core file, tag, and relationship operations continue to work, possibly with degraded autocomplete.], [@uc-search-file, @uc-add-tag, @uc-add-relationship],
  [Q4.1], [Should], [Extensibility - New commands], [Adding a simple DSL command can be done by extending the parser or command dispatch path without changing unrelated storage or transport layers.], [@uc-complex-dsl-query],
)
