#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Remove a relationship
* Preconditions *
- a) The user has the application opened
- b) The file browser is connected to a Graphle server
- c) The user wants to remove a relationship between two files

* Flow *
1. The user searches for a file
2. The user selects the operation "Remove relationship" on the desired relationship

* Alternative flow *
- 1a) The user issues a DSL command
- 4b) The user cancels the operation

* Postconditions *
- a) The system removes the relationship between two entities
#figure(
  placement: none,
  image("remove-a-relationship-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Remove a relationship activity diagram]
)
