#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Add a relationship
* Preconditions *
- a) The user has the application opened
- b) The file browser is connected to a Graphle server
- c) The user wants to connect two files with their relationship

* Flow *
1. The user opens a file browser
2. The user searches for a file
3. The user selects the operation "Add relationship"
4. The system displays the menu where the user can enter info about the relationship
5. The user enters the information and submits it

* Alternative flow *

5a) The user cancels the operation

* Postconditions *
- a) The system remembers the new relationship between two entities
- b) The relationship has the name and the value set by the user
- c) The user can traverse the relationship to find the second file straight from the first file 
#figure(
  placement: none,
  image("add-a-relationship-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Add a relationship activity diagram]
)
