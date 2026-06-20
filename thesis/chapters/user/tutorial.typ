#import "../../template/shared.typ": *

== Tutorial

=== DSL

This section explains how to use the provided #voc("dsl") interface.
The #voc("dsl") provides the same graph operations available from the graphical user interface, plus more. This includes
inspecting files, adding #voc("tag", text: "tags"), creating #voc("relationship", text: "relationships"), and searching through the graph.
It is useful when the user wants to integrate the Graphle system into their own scripts, or express a search that would be inconvenient to build through the GUI.

==== Running a command

In the desktop application, commands are entered into the command line in the header.
The same commands can also be sent directly to the backend via #voc("http") by posting to `/dsl`.
For example, if `GraphleManager` is running on the default port, the following command asks for all files carrying the `project` tag:

```bash
curl \
  -H 'Content-Type: application/json' \
  -X POST \
  -d '{"command":"tag \"project\""}' \
  http://localhost:5824/dsl
```

The server returns a `DSLResponse`.
Successful upsert commands return the type `SUCCESS`.
Search commands return one of `FILENAMES`, `CONNECTIONS`, `FILE`, or `TAG`, depending on what was requested.
If the command cannot be parsed or executed, the response type is `ERROR`.

==== Basic commands

The simplest commands operate on one file, one tag, or one relationship.
Names, values, and paths can be written without double quotes, but if one needs multi-word entries, one should use double quotes.

#table(
  columns: (19em, 1fr),
  align: (left, left),
  stroke: 0.4pt + luma(160),
  table.header([*Command*], [*Effect*]),
  [`detail "<path>"`], [Shows one file together with its #voc("tag", text: "tags") and #voc("relationship", text: "relationships").],
  [`tag "<name>"`], [Lists files that carry a #voc("tag") with the given name, regardless of the tag value.],
  [`addTag "<path>" "<name>"`], [Adds a simple #voc("tag") to a file.],
  [`addTag "<path>" "<name>" "<value>"`], [Adds a key-value #voc("tag") to a file.],
  [`removeTag "<path>" "<name>"`], [Removes a simple #voc("tag") from a file.],
  [`removeTag "<path>" "<name>" "<value>"`], [Removes a key-value #voc("tag") from a file.],
  [`addRel "<from>" "<to>" "<name>"`], [Creates a directed #voc("relationship") from one file to another.],
  [`addRel "<from>" "<to>" "<name>" "<value>"`], [Creates a directed #voc("relationship") with an additional value.],
  [`removeRel "<from>" "<to>" "<name>"`], [Removes a directed #voc("relationship") without a value.],
  [`removeRel "<from>" "<to>" "<name>" "<value>"`], [Removes a directed #voc("relationship") with the given value.],
  [`addFile "<path>"`], [Creates a file at the given path and registers it for completion.],
  [`removeFile "<path>"`], [Removes a file or directory and deletes its graph metadata.],
  [`moveFile "<from>" "<to>"`], [Moves a file and updates the stored graph location.],
)

For example, the following commands tag a film with its type and release year:

```text
addTag "/Users/johndoe/GraphleDslSample/movies/the-matrix.mkv" "type" "movie"
addTag "/Users/johndoe/GraphleDslSample/movies/the-matrix.mkv" "year" "1999"
```

The next command links a research note to the plan it motivates:

```text
addRel "/Users/johndoe/GraphleDslSample/research/notes/research-questions.md" "/Users/johndoe/GraphleDslSample/projects/graphle-demo/experiment-plan.md" "motivates"
```

==== Finding files

The `find` command is the expressive part of the #voc("dsl").
It is built from scopes:

- file scopes use parentheses: `( ... )`
- #voc("relationship") scopes use square brackets: `[ ... ]`

A file scope filters file nodes.
It can use the fields `location`, `tagName`, and `tagValue`.
A #voc("relationship") scope filters edges between the currently selected files and their #voc("neighbor", text: "neighbors").
It can use the fields `name` and `value`.

The supported operators are `=`, `!=`, `<>`, `>`, `>=`, `<`, and `<=`.
Predicates can be combined with `AND` and `OR`, and parentheses can be used inside a scope to make precedence explicit.
Numeric comparisons are most useful with numeric tag values, such as years:

```text
find (tagName = "year" AND tagValue >= 2000)
```

This returns files whose `year` tag has a numeric value greater than or equal to `2000`.

To find all files tagged as movies or shows, use:

```text
find (tagName = type AND (tagValue = show OR tagValue = movie))
```

To find one file by its path, use:

```text
find (location = "/Users/johndoe/GraphleDslSample/research/notes/research-questions.md")
```

If no filter is given for the first file scope, it displays all the files indexed by Graphle, to avoid having to
fetch all the files present on the disk.

==== Traversing relationships

Scopes are evaluated from left to right.
A relationship scope uses the files selected by the previous file scope as its starting points.
If a command ends with a relationship scope, the result is a list of relationships.
If the user wants the target filenames instead, an empty file scope `()` can be appended.

The following command starts at `research-questions.md`, follows outgoing relationships named `motivates`, and returns the target files:

```text
find (location = "/Users/johndoe/GraphleDslSample/research/notes/research-questions.md")[name = "motivates"]()
```

Longer traversals are written by alternating relationship and file scopes.
For example, this command follows a `motivates` relationship and then a `produces` relationship:

```text
find (location = "/Users/johndoe/GraphleDslSample/research/notes/research-questions.md")[name = "motivates"]()[name = "produces"]()
```

The special relationship scopes `[DESC]` and `[PRED]` expose the live #voc("filesystem") hierarchy.
`[DESC]` selects direct descendants of a directory, while `[PRED]` selects the parent directory of the current file.
For example, the following command lists the direct children of the sample movie directory:

```text
find (location = "/Users/johndoe/GraphleDslSample/movies")[DESC]()
```

Because these hierarchy relationships are read from the filesystem, they can be used even when the files are not yet registered in Graphle.

==== Suggested workflow

A typical scripted workflow has two phases.
First, create or collect the files with normal filesystem tools such as `mkdir` and `touch`.
Second, initialize the graph metadata with `addTag` and `addRel`.
After that, day-to-day work can use `find`, `tag`, and `detail` to query the graph.

For a small research project, a user might tag notes, datasets, and generated summaries:

```text
addTag "/Users/johndoe/GraphleDslSample/research/notes/literature-review.md" "type" "note"
addTag "/Users/johndoe/GraphleDslSample/research/datasets/interviews.csv" "type" "dataset"
addTag "/Users/johndoe/GraphleDslSample/projects/graphle-demo/results-summary.md" "type" "summary"
addRel "/Users/johndoe/GraphleDslSample/research/datasets/interviews.csv" "/Users/johndoe/GraphleDslSample/projects/graphle-demo/results-summary.md" "input-for"
```

The graph can then be queried by category:

```text
find (tagName = "type" AND tagValue = "dataset")
```

or by semantic connection:

```text
find (location = "/Users/johndoe/GraphleDslSample/research/datasets/interviews.csv")[name = "input-for"]()
```

==== Demo setup

The repository contains a small runnable example for this workflow in the `examples` directory.
The script `examples/graphle-dsl-home-sample.sh` creates a sample hierarchy under `~/GraphleDslSample`, creates empty files, and then initializes Graphle metadata through the #voc("dsl").
The generated hierarchy contains research notes, a dataset, project files, an archive file, and a movie collection.

The same script tags the files with values such as `type = movie` or `year = 1999`.
It also creates relationships such as `motivates` or `input-for`.

After the sample has been initialized, query files can be executed with `examples/run-graphle-dsl-file.sh` with a DSL file.

```bash
examples/run-graphle-dsl-file.sh examples/queries/04-movies-by-type.dsl
```

The `examples/queries` directory contains separate query files to test out the system:

- `01-project-tags.dsl` lists files with the `project` tag.
- `02-year-tags.dsl` lists files with the `year` tag.
- `03-motivated-plan.dsl` follows the `motivates` relationship from the research questions note.
- `04-movies-by-type.dsl` finds files tagged as movies.
- `05-movies-from-2000.dsl` finds files whose numeric `year` tag is at least `2000`.
- `06-movie-directory-children.dsl` uses `[DESC]` to list the direct filesystem children of the movie directory.

=== GUI

The graphical interface provides the same core operations as the #voc("dsl"), but organizes them around the currently displayed file or directory.
When the application starts, it opens the user's home directory and shows its tags and connected files.
The top row contains the application menu on the left and the command line next to it.
The command line can still be used for #voc("dsl") commands, but most common operations are available from menus and dialogs.

==== Main view

The main body of the application is a detail view for the selected file or directory.
It is divided into three kinds of information:

- URL tags, shown separately when a tag value is a valid URL.
- Other #voc("tag", text: "tags"), containing the tag name and optional value.
- Related files, containing the #voc("relationship") name and the target file path.

Clicking a file opens that file's detail view.
Parent and descendant relationships are displayed together with custom relationships, so the user can navigate both the normal #voc("filesystem") hierarchy
and manually created graph relationships from the same screen. Parent relationships are displayed with an upward arrow and descendant relationships with a downward arrow.

When a command or menu action returns a list of filenames rather than a single file, the application shows a list of filename pills.
Clicking one of these filenames opens the corresponding detail view.
When a tag lookup is opened, the application shows files carrying that tag together with the matched tag value.

==== Application menu

The application menu is opened with the button on the left side of the header.
It contains global actions and, when a file detail view is active, also file actions for the selected path.

The main global actions are:

- `Open Home` returns the user's home directory.
- `Open Trash` opens the trash view.
- `Show Hidden Files` toggles whether hidden filesystem entries are displayed.
- `Dark mode` toggles the visual theme.

When the current view is a file or directory detail view, the same menu also exposes the file actions described below.

==== File context menu

File and relationship pills can be opened directly with a click.
Opening the context menu on a file pill gives access to operations for that file:

- `Open` opens the file with the default app. If the backend is remote, Graphle first downloads a copy into `GraphleDownloads` under the user's home directory.
- `Add file` is available on directories and creates a new child file.
- `Copy path` copies the absolute path to the clipboard.
- `Move` moves the file to another directory.
- `Add tag` opens a dialog for entering a tag name and optional value.
- `Add relationship` opens a dialog for linking the current file to another file.
- `Remove relationship` is shown for custom relationships and removes that graph edge.
- `Move to trash` moves the file to the trash.
- `Delete permanently` removes the file after confirmation.

Parent and descendant relationships come from the live #voc("filesystem"), so they cannot be removed as custom Graphle relationships.
Only manually created relationships have the `Remove relationship` action.

==== Adding tags

To add a tag through the GUI, open the menu for the target file and choose `Add tag`.
The dialog requires a tag name and accepts an optional value.

Existing tags are displayed as pills in the detail view.
Opening a tag pill's menu provides two actions.
`Open` shows all files that carry a tag with the same name.
`Delete` removes that tag from the current file.
If a tag value is a URL, clicking the tag opens the URL directly.

==== Adding relationships

To create a relationship, open the source file's menu and choose `Add relationship`.
The dialog requires:

- `Related to`, the absolute path of the target file.
- `Relationship name`, the semantic name of the edge.

The optional `Relationship value` field can store extra information about the relationship.
The `Is bidirectional` checkbox creates the relationship in both directions, which is useful when the connection should be navigable from either file.

Clicking the relationship pill navigates to the target file.
Opening the pill's context menu allows the user to remove the relationship.

==== Searching from the GUI

The GUI can search in two ways.
First, the user can browse interactively by clicking file, relationship, and tag pills.
Second, the command line in the header accepts the same #voc("dsl") commands described in the previous section.
While typing, the application asks the backend for autocomplete suggestions.
Suggestions can be selected from the list, and the completed command can then be executed from the command line.
Once the command is complete, press Shift + Enter to submit it, and the backend response will be rendered in the browser.
