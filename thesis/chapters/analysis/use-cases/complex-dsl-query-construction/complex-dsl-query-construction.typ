#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Complex DSL query construction
* Preconditions *
- The user has the application opened
- The file browser is connected to a Graphle server
- The user wants to perform a complex search using multiple criteria

* Flow *
1. The user opens the DSL command interface
2. The user starts constructing a query with filters they want to query for (more on that in user tutorial)
3. The user submits the command
4. The system validates the query syntax
5. The system executes the DSL query
6. The system returns the result of the command

* Alternative flow *
- 4a) If the query syntax is invalid, the system shows an error message and the user returns to step 2

* Postconditions *
- The user receives a list of objects matching all specified criteria

#figure(
  placement: none,
  image("complex-dsl-query-construction-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Complex DSL query construction activity diagram]
)