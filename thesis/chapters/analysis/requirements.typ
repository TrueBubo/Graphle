#import "../../template/shared.typ": *

== Functional and Qualitative Requirements

=== Functional Requirements
This list serves as a feature list this application should fulfill in order to be considered complete.

<functional_requirements>
- *F1 Backward s Compatibity* - The application must work with the existing filesystems and can work with hierarchies via #link(label("voc_connection"))[connections] in the graph
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

- *Q1 Usability*
    - The application provides the users with the option to choose between interacting via DSL or GUI
    - Users can set other themes or use a different GUI client altogether
    - Users can connect remotely
- *Q2 Performance*
    - The autocomplete responds with available filenames within 250ms
    - The GUI should update within 200ms after clicking to see #link(label("voc_neighbor"))[neighbors] for a node
- *Q4 Reliability*
  - The application will retry performing failing operations before giving up
  - The failure of auxiliary components, such as an auto-completer or GUI, does not prevent the core from running
- *Q4 Extensibility*
  - The application can be extended to allow new commands
