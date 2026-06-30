#import "../../template/shared.typ": *
#import "mockups/main-page.typ": main-page-mockup
#import "mockups/filenames-page.typ": filenames-page-mockup, find-relationships-page-mockup
#import "mockups/files-with-tag-page.typ": files-with-tag-page-mockup
== Mockups

This section presents the visual design of the GUI client.
Each mockup illustrates a key screen of the application and explains the layout.

=== File Detail Page mockup

The main page opens at the user's home directory and displays the file detail view.
The header contains a hamburger menu and a command line input pre-filled with the current #voc("dsl") command.
Below the header, the body is divided into three sections: URLs (#voc("tag", text: "tags") whose value is a URL), Tags, and Files.
Files are rendered as pills. Parent and descendant #voc("connection", text: "connections") use arrow icons to indicate direction, while named #voc("relationship", text: "relationships") show their label.
This page is displayed as the detail view for every file, which can be traversed by pressing the pill or by using the #voc("dsl").

#main-page-mockup()

=== Filenames Results mockup

The filenames results page displays the output of a find #voc("dsl") command ending with filenames scope.
The header shows the executed query, and the body lists the matching files as pills.

#filenames-page-mockup()

=== Find with Relationship Filter mockup

When a find command includes a #voc("relationship") filter, the results page shows matching files as pills.
Each pill displays the #voc("relationship") name and value alongside the file path.

#find-relationships-page-mockup()

=== Files With Tag mockup

The files with tag page displays the output of a tag lookup command.
Each result pill shows the matched #voc("tag") name and value followed by the file path.

#files-with-tag-page-mockup()
