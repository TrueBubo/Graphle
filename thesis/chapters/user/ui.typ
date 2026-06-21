#import "../../template/shared.typ": *
#import "../design/mockups/main-page.typ": main-page-mockup
#import "../design/mockups/filenames-page.typ": filenames-page-mockup, find-relationships-page-mockup
#import "../design/mockups/files-with-tag-page.typ": files-with-tag-page-mockup
== User Interface

This section describes the screens and controls of the Graphle desktop application.
The graphical client is a single-window application: the header stays visible at the top, while the body changes according to the current display mode.
The user can therefore move through the system by opening files, executing #voc("dsl") commands, opening tags, or using context menus on displayed pills.

=== Shared Header and Application Menu

Every screen contains the same header.
The left side contains the application menu button, and the rest of the header is a command line for #voc("dsl") commands.

- *Application menu button* - Opens the global menu.
  The menu contains `Open Home`, `Open Trash`, `Show Hidden Files`, and `Dark mode`.
  When a file detail screen is active, the same menu also contains actions for the currently opened file.
- *Command line* - Allows the user to enter and execute #voc("dsl") commands.
  The command line is also updated when the user navigates through the graphical interface, so it shows the command corresponding to the displayed data. The user can submit the DSL query by pressing `Shift + Enter`.
- *Autocomplete suggestions* - While the user types, the client asks the backend autocomplete service for filename suggestions and displays them below the command line.
  The user can move through suggestions with the arrow keys or `Tab`, select a suggestion with `Enter`, and execute the current command with `Shift + Enter`.

The file actions available from the application menu and from file context menus are:

- `Open` - Opens the file in the operating system.
  If the backend is remote, Graphle first downloads the file into the `GraphleDownloads` directory under the user's home directory.
- `Add file` - Creates a new child file. This action is available only for directories.
- `Copy path` - Copies the absolute path to the clipboard.
- `Move` - Opens the move dialog.
- `Add tag` - Opens the add tag dialog.
- `Add relationship` - Opens the add relationship dialog.
- `Remove relationship` - Removes a custom relationship. This action is shown only for relationship pills and not for filesystem parent or descendant links.
- `Move to trash` - Moves the file into the Graphle trash directory.
- `Delete permanently` - Opens a confirmation dialog and deletes the file after confirmation.

=== File Detail Screen

The file detail screen is the main screen of the application.
It is opened after application startup, where it displays the user's home directory, and it is also opened whenever the user selects a file pill or executes a detail command.
The same screen is used for normal files, directories, and the Graphle trash directory.

#main-page-mockup()

The file detail screen consists of the following parts:

- *Header* - Contains the application menu and the #voc("dsl") command line.
  In this screen, the command line corresponds to a `detail` command for the currently displayed path.
- *URLs section* - Shows #voc("tag", text: "tags") whose value is a valid URL.
  Clicking a URL tag opens the URL. Opening the context menu on the tag allows the user to search for files with the same tag name or delete the tag from the current file.
- *Tags section* - Shows normal #voc("tag", text: "tags") attached to the current file.
  Each tag is displayed as a pill containing the tag name and, if present, the tag value.
  The tag context menu contains `Open`, which displays all files carrying the tag name, and `Delete`, which removes the tag from the current file.
- *Files section* - Shows related files.
  Filesystem parent and descendant links are displayed together with custom graph relationships.
  Parent links use an upward arrow, descendant links use a downward arrow, and custom relationships show their relationship name and optional value.
  Clicking a file pill opens that file in the detail screen.
  Opening the file pill context menu gives access to the file actions described above.

=== Filename Results Screen

The filename results screen is displayed when a #voc("dsl") command returns a list of file paths.
This typically happens after a `find` command that ends with a file scope, for example a search by tag name, tag value, location, or relationship traversal followed by an empty file scope. Scopes and the DSL are explained further in the following section.

#filenames-page-mockup()

The screen contains:

- *Header* - Shows the command that produced the results.
- *Files section* - Lists all returned file paths as pills.
  Clicking a pill opens the selected file in the file detail screen.
  Opening the context menu on a pill gives access to the same file actions as in the file detail screen.

=== Relationship Results Screen

The relationship results screen is displayed when a `find` command ends with a relationship scope and therefore returns relationships rather than target files.
It has the same visual structure as the file list, but each pill contains both relationship information and the related file path.

#find-relationships-page-mockup()

The screen contains:

- *Header* - Shows the executed #voc("dsl") command.
- *Files section* - Lists returned relationships.
  Each pill includes the relationship name, optional relationship value, and target file path.
  Clicking a pill opens the target file.
  Opening the context menu gives access to file actions and, for custom relationships, it also gives the `Remove relationship` action.

=== Files With Tag Screen

The files with tag screen is displayed after opening a tag from the tag context menu or after executing the `tag` #voc("dsl") command.
It lists all files that carry the requested tag name.

#files-with-tag-page-mockup()

The screen contains:

- *Header* - Shows the tag lookup command.
- *Tagged file list* - Displays one pill for each matching file.
  The pill contains the tag name, the optional tag value, and the file path.
  Clicking a pill opens the corresponding file in the file detail screen.

=== Dialog Screens

Several operations are performed through dialogs.

==== Add Tag Dialog

The add tag dialog is opened by selecting the `Add tag` file action.
It contains:

- `Tag name*` - Required field containing the tag name.
- `Tag value` - Optional field containing the tag value.
- `OK` - Creates the tag and refreshes the current file detail screen.
- `Cancel` - Closes the dialog.

==== Add Relationship Dialog

The add relationship dialog is opened when the `Add relationship` file action is invoked.
It creates a relationship from the current file to another file.
It contains:

- `Related to*` - Required path of the target file.
- `Relationship name*` - Required relationship name.
- `Relationship value` - Optional relationship value.
- `Is bidirectional` - Checkbox that creates the relationship in both directions when selected.
- `OK` - Creates the relationship and refreshes the current file detail screen.
- `Cancel` - Closes the dialog.

==== Add File Dialog

The add file dialog is shown after selecting the `Add file` action on a directory.
It contains:

- `Filename` - Required name of the new child file.
- `OK` - Creates the file under the current directory and refreshes the screen.
- `Cancel` - Closes the dialog.

==== Move File Dialog

The move file dialog is shown after selecting the `Move` file action.
It contains:

- `Move to` - Required path of the destination directory.
- `OK` - Moves the current file into the selected directory.
- `Cancel` - Closes the dialog.

==== Delete File Dialog

The delete file dialog is opened from the `Delete permanently` file action.
It asks the user to confirm permanent deletion of the selected path.
The dialog contains `OK`, which permanently deletes the file, and `Cancel`, which closes the dialog.
