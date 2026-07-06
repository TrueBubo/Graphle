#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Open a file <uc-open-file>
* Preconditions *
- The user has the application open
- The file browser is connected to a Graphle server
- The user wants to open a file or folder

* Flow *
1. The user searches for the file or folder
2. The user opens the context menu for the selected item
3. The user selects the operation "Open"
4. The system opens the file or folder with the default application

* Alternative flow *
- 4a) If the backend runs on a remote machine, the GUI downloads the file first and then opens the local copy
- 4b) If the selected item is a remote folder, the operation is not offered and the user can browse the folder in Graphle instead
- 4c) If the file cannot be opened, the system shows an error and leaves the #voc("filesystem") unchanged

* Postconditions *
- The selected file or folder is opened outside Graphle, or the user receives an error explaining why it could not be opened

#figure(
  placement: none,
  image("open-a-file-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Open a file activity diagram]
)
