#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Add a tag <uc-add-tag>
* Preconditions *
- The user has the application open
- The file browser is connected to a Graphle server
- The user wants to tag a file

* Flow *
1. The user searches for a file
2. The user opens the context menu for the selected file
3. The user selects the operation "Add tag"
4. The system displays a dialog where the user can enter information about the tag
5. The user enters the information and submits it

* Alternative flow *
- 1a) The user issues a DSL command
- 5b) The user cancels the operation

* Postconditions *
- The system remembers a new tag
- The tag has a name set by the user
- The user can find the file by looking at files tagged with such a tag
#figure(
  placement: none,
  image("add-a-tag-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Add a tag activity diagram]
)
