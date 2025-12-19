#import "../../../../template/shared.typ": *
#import "../config.typ": *
=== Add a tag
* Preconditions *
- a) The user has the application opened
- b) The file browser is connected to a Graphle server
- c) The user wants to tag a file

* Flow *
1. The user searches for a file
2. The user selects the operation "Add tag"
3. The system displays the menu where the user can enter info about the tag
4. The user enters the information and submits it

* Alternative flow *
- 1a) The user issues a DSL command
- 4b) The user cancels the operation

* Postconditions *
- a) The system remembers a new tag
- b) The tag has a name set by the user
- c) The user can find the file by looking at files tagged with such a tag
#figure(
  placement: none,
  image("add-a-tag-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Add a tag activity diagram]
)
