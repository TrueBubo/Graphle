#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Search for a file
* Preconditions *
- The user has the application opened
- The file browser is connected to a Graphle server
- The user wants to find a file

* Flow *
1. The user opens the file browser
2. The file browser opens the user-defined home location
3. Application shows the graph of possible #link(label("voc_neighbor"))[neighbors] from the current location
4. The user clicks on the neighbor they want to visit
5. If the file was not located then go back to step 3
6. The user clicks on the file
7. The user shows the menu on the file
8. The system shows the menu with different operations available
9. The user selects an operation
10. The system performs the operation

* Alternative flow *
- 4a) The user uses the integrated DSL for search

* Postconditions *
- The user accessed the wanted file
- The system performed the desired operation

#figure(
  placement: none,
  image("search-for-a-file-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Search for a file activity diagram]
)
