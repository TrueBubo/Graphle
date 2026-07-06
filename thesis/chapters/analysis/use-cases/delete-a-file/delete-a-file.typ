#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Delete a file <uc-delete-file>
* Preconditions *
- The user has the application open
- The file browser is connected to a Graphle server
- The user wants to delete a file or folder

* Flow *
1. The user searches for the file or folder
2. The user opens the context menu for the selected item
3. The user selects the operation "Delete permanently"
4. The system displays a confirmation dialog
5. The user confirms the deletion
6. The system deletes the file or folder from the underlying #voc("filesystem")
7. The system removes the deleted item and its metadata from the graph

* Alternative flow *
- 1a) The user issues a DSL command to delete the file
- 5a) The user cancels the operation
- 6a) If the file cannot be deleted, the system shows an error and keeps the graph metadata unchanged

* Postconditions *
- The file or folder no longer exists in the underlying #voc("filesystem")
- The deleted item is no longer shown as a graph entity in Graphle

#figure(
  placement: none,
  image("delete-a-file-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Delete a file activity diagram]
)
