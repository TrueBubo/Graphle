#import "../../template/shared.typ": *

== Functional and Qualitative Requirements

The requirements were defined based on an analysis of existing tools and their limitations.
Existing #voc("filesystem") tools and productivity applications were examined to identify potential enhancements that can be made to them.
The existing solutions will be discussed further in the #link(label("landscape"))[Current landscape] section.
No formal user interviews were conducted. The requirements were instead derived from experience with the problem domain.
The main goal of this project is to extend a standard #voc("filesystem") to support faster traversal by interpreting
#voc("relationship", text: "relationships") as an #voc("lpg").

=== Functional Requirements
This list serves as a feature list that this application should fulfill in order to be considered complete.

<functional_requirements>
- *F1 Backwards Compatibility* - The application must work with the existing #voc("filesystem", text: "filesystems") and can work with hierarchies via #voc("connection", text: "connections") in the graph
- *F2 #voc("relationship", text: "Relationships") between files* - Files can be #voc("relationship", text: "related") to other files
- *F3 File #voc("tag", text: "tags")* - Files can be categorized via #voc("tag", text: "tags")
- *F4 File manipulation* - Files can be created, opened, deleted, and moved
- *F5 Folders work as files* - Folders can do everything files can do
- *F6 GUI file browser* - The filesystem can be traversed through a GUI client
- *F7 Custom #voc("dsl")* - Custom #voc("dsl") for more advanced operations with the #voc("filesystem")
- *F8 Filename autocompletion* - The application recommends possible filenames based on recently visited files
- *F9 Lazy updates* - Updates on the #voc("filesystem") are done dynamically as needed
- *F10 Cross-platform support* - The application must run on macOS and Linux

Windows is explicitly not supported, as NTFS assigns each volume an independent drive letter rather than providing a single root hierarchy. 
Without a common root, files on different drives cannot be connected in the graph.  

=== Qualitative Requirements
This list serves as a requirement list for how well the application should handle users' wishes.

<qualitative_requirements>
- *Q1.1 Usability - Interaction modes* - The application provides users with the option to choose between interacting through the DSL or the GUI
- *Q1.2 Usability - Theming* - Users can set other themes or use a different GUI client altogether
- *Q1.3 Usability - Remote access* - Users can connect remotely
- *Q2.1 Performance - Autocomplete latency* - The autocomplete responds with available filenames within 250ms, ensured by an in-memory index of recently visited files
- *Q3.1 Reliability - Operation retries* - The application will retry performing failing operations before giving up
- *Q3.2 Reliability - Component isolation* - The failure of auxiliary components, such as an auto-completer or GUI, does not prevent the core from running
- *Q4.1 Extensibility - New commands* - The application can be extended with new commands
