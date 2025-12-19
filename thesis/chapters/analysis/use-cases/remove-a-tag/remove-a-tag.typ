#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Remove a tag
* Preconditions *
- a) The user has the application opened
- b) The file browser is connected to a Graphle server
- c) The user wants to connect two files with their relationship

* Flow *
1. The user searches for a file
2. The user selects the operation "Delete" on the desired tag

* Alternative flow *
- 1a) The user issues a DSL command
- 4b) The user cancels the operation

* Postconditions *
- a) The system removes the relationship between two entities
#figure(
  placement: none,
  image("remove-a-tag-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Remove a tag activity diagram]
)
