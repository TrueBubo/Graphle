#import "../../template/shared.typ": *

== Functional and Qualitative Requirements

I defined the requirements based on my personal analysis of existing tools and their limitations.
I examined existing filesystem tools and productivity applications to identify potential enhancement that can be made to them.
The existing solutions will be discussed further in the #link(label("landscape"))[Current landscape] section.
No formal user interviews were conducted. The requirements were instead derived from my own experience with the problem domain.
The main goal of this project is to extend a standard filesystem to support faster traversal via interpreting the 
#link(label("voc_relationship"))[relationships] as an #link(label("voc_lpg"))[LPG].

=== Functional Requirements
This list serves as a feature list this application should fulfill in order to be considered complete.

<functional_requirements>
- *F1 Backwards Compatibity* - The application must work with the existing filesystems and can work with hierarchies via #link(label("voc_connection"))[connections] in the graph
- *F2 #link(label("voc_relationship"))[Relationships] between files* - Files can be #link(label("voc_relationship"))[related] to other files
- *F3 File #link(label("voc_tag"))[tags]* - Files can be categorized via #link(label("voc_tag"))[tags]
- *F4 File manipulation* - Files can be created, opened, deleted and moved
- *F5 Folders work as files* - Folders can do everything files can do
- *F6 GUI file browser* - Filesystem can be traversed via a GUI client
- *F7 Custom #link(label("voc_dsl"))[DSL]* - Custom #link(label("voc_dsl"))[DSL] for more advanced operations with the filesystem
- *F8 Filename autocompletion* - The application recommends possible filenames based on recently visited files
- *F9 Lazy updates* - Updates on the filesystem are done dynamically as needed

=== Qualitative Requirements
This list serves as a requirement list for how well they should handle the users' wishes.

- *Q1.1 Usability - Interaction modes* - The application provides the users with the option to choose between interacting via DSL or GUI
- *Q1.2 Usability - Theming* - Users can set other themes or use a different GUI client altogether
- *Q1.3 Usability - Remote access* - Users can connect remotely
- *Q2.1 Performance - Autocomplete latency* - The autocomplete responds with available filenames within 250ms, ensured by an in-memory index of recently visited files
- *Q3.1 Reliability - Operation retries* - The application will retry performing failing operations before giving up
- *Q3.2 Reliability - Component isolation* - The failure of auxiliary components, such as an auto-completer or GUI, does not prevent the core from running
- *Q4.1 Extensibility - New commands* - The application can be extended to allow new commands
