#import "../../template/shared.typ": *
#import "mockups/main-page.typ": main-page-mockup
#import "mockups/filenames-page.typ": filenames-page-mockup, find-relationships-page-mockup
== Mockups

This section presents the visual design of the GUI client.
Each mockup illustrates a key screen of the application and explains the layout.

=== File Detail Page mockup

The main page opens at the user's home directory and displays the file detail view.
The header contains a hamburger menu and a command line input pre-filled with the current #link(label("voc_dsl"))[DSL] command.
Below the header, the body is divided into three sections: URLs (#link(label("voc_tag"))[tags] whose value is a URL), Tags, and Files.
Files are rendered as pills. Parent and descendant #link(label("voc_connection"))[connections] use arrow icons to indicate direction, while named #link(label("voc_relationship"))[relationships] show their label.
This page is displayed for detail on every file, which can be traversed by pressing on the pill or by #link(label("voc_dsl"))[DSL].

#main-page-mockup()

=== Filenames Results mockup

The filenames results page displays the output of a find #link(label("voc_dsl"))[DSL] command ending with filenames scope.
The header shows the executed query, and the body lists the matching files as pills.

#filenames-page-mockup()

=== Find with Relationship Filter mockup

When a find command includes a #link(label("voc_relationship"))[relationship] filter, the results page shows matching files as pills.
Each pill displays the #link(label("voc_relationship"))[relationship] name and value alongside the file path.

#find-relationships-page-mockup()
