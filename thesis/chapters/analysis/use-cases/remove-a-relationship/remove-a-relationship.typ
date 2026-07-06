#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Remove a relationship <uc-remove-relationship>
* Preconditions *
- a) The user has the application open
- b) The file browser is connected to a Graphle server
- c) The user wants to remove a relationship between two files

* Flow *
1. The user searches for a file
2. The user opens the context menu on the desired relationship
3. The user selects the operation "Remove relationship"

* Alternative flow *
- 1a) The user issues a DSL command
- 2a) The user closes the context menu without selecting an operation

* Postconditions *
- a) The system removes the relationship between two entities
#figure(
  placement: none,
  image("remove-a-relationship-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Remove a relationship activity diagram]
)
