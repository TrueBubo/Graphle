#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Search for a file <uc-search-file>
* Preconditions *
- The user has the application open
- The file browser is connected to a Graphle server
- The user wants to find a file

* Flow *
1. The user opens the file browser
2. The file browser opens the user-defined home location
3. The application shows possible #voc("neighbor") entities from the current location
4. The user clicks on the neighbor they want to visit
5. If the file was not located, then go back to step 3
6. The system displays the selected file or folder

* Alternative flow *
- 4a) The user uses the integrated DSL for search

* Postconditions *
- The user accessed the desired file

#figure(
  placement: none,
  image("search-for-a-file-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Search for a file activity diagram]
)
