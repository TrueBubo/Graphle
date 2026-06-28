#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Remove a tag <uc-remove-tag>
* Preconditions *
- a) The user has the application open
- b) The file browser is connected to a Graphle server
- c) The user wants to remove a tag from a file

* Flow *
1. The user searches for a file
2. The user selects the operation "Delete" on the desired tag

* Alternative flow *
- 1a) The user issues a DSL command
- 4b) The user cancels the operation

* Postconditions *
- a) The system removes the tag
#figure(
  placement: none,
  image("remove-a-tag-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Remove a tag activity diagram]
)
