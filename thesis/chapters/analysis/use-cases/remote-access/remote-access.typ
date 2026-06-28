#import "../../../../template/shared.typ": *
#import "../config.typ": *

=== Browse a remote filesystem <uc-remote-access>
* Preconditions *
- The user has the application installed locally
- `GraphleManager` is running on a remote machine
- The user can access the remote machine over SSH
- SSH port forwarding is configured from the local machine to the remote `GraphleManager` port

* Flow *
1. The user starts the SSH port forwarding connection
2. The user configures the GUI to connect to the locally forwarded server port
3. The user opens the Graphle GUI on the local machine
4. The system connects to the remote `GraphleManager` instance through the forwarded port
5. `GraphleManager` opens the configured home location on the remote machine
6. The system displays files, folders, tags, and #voc("relationship", text: "relationships") from the remote #voc("filesystem")
7. The user browses the remote #voc("filesystem") in the same way as in @uc-search-file
8. When the user opens a file, the GUI downloads a local copy

* Alternative flow *
- 4a) If the SSH tunnel is not available, the system shows a connection error
- 4b) The user restarts or fixes the SSH port forwarding connection and returns to step 3
- 8a) If the backend is configured as local, the GUI opens the file directly instead of downloading a copy

* Postconditions *
- The user can browse files from the machine where `GraphleManager` runs
- File paths are interpreted on the backend machine, not on the local GUI machine

#figure(
  placement: none,
  image("remote-access-activity-diagram.svg", width: use-case-figure-scale),
  caption: [Browse a remote filesystem activity diagram]
)
