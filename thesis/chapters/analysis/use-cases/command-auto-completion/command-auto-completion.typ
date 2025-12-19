#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Command auto-completion
* Preconditions *
- The user has the application opened
- The file browser is connected to a Graphle server
- The DSL command interface is active

* Flow *
1. The user opens the DSL command interface
2. The user starts typing a command
3. The system analyzes the current input prefix
4. The system queries the auto-completion service
5. The system generates suggestions
6. The system displays a list of suggestions to the user
7. The user selects a suggestion from the list or continues typing
8. If the user selects a suggestion, the system completes the command with the selected text
9. The user continues constructing the command or executes it

* Postconditions *
- The user has a valid or partially constructed DSL command
- The user saves time by not typing complete paths or commands manually

#figure(
  placement: none,
  image("command-auto-completion-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Command auto-completion activity diagram]
)