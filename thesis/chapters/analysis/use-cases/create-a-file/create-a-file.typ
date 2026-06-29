#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Create a file <uc-create-file>
* Preconditions *
- The user has the application open
- The file browser is connected to a Graphle server
- The user wants to create a file in a known parent folder

* Flow *
1. The user searches for the parent folder
2. The user opens the context menu on the parent folder
3. The user selects the operation "Add file"
4. The system displays a dialog where the user can enter the new name
5. The user enters the name and submits the dialog
6. The system creates the file in the selected parent folder
7. The system shows the new file among the parent's #voc("neighbor", text: "neighbors")

* Alternative flow *
- 1a) The user issues a DSL command to create the file
- 5a) The user cancels the operation
- 6a) If the file cannot be created, the system shows an error and leaves the #voc("filesystem") unchanged

* Postconditions *
- The new file exists in the underlying #voc("filesystem")
- The user can open, tag, move, delete, or relate the new file through Graphle

#figure(
  placement: none,
  image("create-a-file-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Create a file activity diagram]
)
