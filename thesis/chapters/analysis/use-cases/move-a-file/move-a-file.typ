#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Move a file <uc-move-file>
* Preconditions *
- The user has the application open
- The file browser is connected to a Graphle server
- The user wants to move a file or folder to another location

* Flow *
1. The user searches for the file or folder
2. The user opens the context menu for the selected item
3. The user selects the operation "Move"
4. The system displays a dialog where the user can enter the destination
5. The user enters the destination and submits the dialog
6. The system moves the file or folder in the underlying #voc("filesystem")
7. The system updates the stored graph location for the moved item

* Alternative flow *
- 1a) The user issues a DSL command to move the file
- 5a) The user cancels the operation
- 6a) If the destination is invalid or the move fails, the system shows an error and keeps the original location

* Postconditions *
- The file or folder exists at the destination path
- Tags and #voc("relationship", text: "relationships") for the moved item remain associated with its new location

#figure(
  placement: none,
  image("move-a-file-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Move a file activity diagram]
)
