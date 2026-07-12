#import "../../template/shared.typ": *
== User Stories

We derived the functional requirements and use cases from a set of user stories captured when communicating with stakeholders.
The most important points raised in these conversations were written up as the user stories below.
The stories are grouped by the two user roles introduced above, since the two roles interact with Graphle in different ways and get different value from the same underlying functionality.
The #emph[See] column points to the requirement that later formalizes the story and where one exists, to the use case that elaborates it.

=== Regular User Stories

<user_stories_regular>
#table(
  columns: (auto, 2.6fr, 1.2fr),
  align: (left, left, left),
  stroke: 0.4pt + luma(160),
  inset: 4pt,
  table.header([*ID*], [*User story*], [*See*]),
  [US1], [As a regular user, I want the app to work with the files I already have, so that I can start using it without copying anything or risking losing data.], [F1],
  [US2], [As a regular user, I want to browse my files and folders in a simple visual browser, so that I can find what I'm looking for without remembering exact file paths or commands.], [F6, Q1.1 @uc-search-file],
  [US3], [As a regular user, I want to label a file or folder, so that I can group things that belong together even when they're kept in different folders.], [F3, @uc-add-tag],
  [US4], [As a regular user, I want to remove a label I added earlier, so that I can fix it once it no longer makes sense.], [F3, @uc-remove-tag],
  [US5], [As a regular user, I want to link two files together, so that I can jump straight from one to the other, even if they're stored in completely different places.], [F2, @uc-add-relationship],
  [US6], [As a regular user, I want to remove a link between two files, so that my connections stay accurate as things change.], [F2, @uc-remove-relationship],
  [US7], [As a regular user, I want to create, open, move, and delete files and folders in one place, so that I don't have to keep switching between different tools.], [F4, @uc-create-file, @uc-open-file, @uc-move-file, @uc-delete-file],
  [US8], [As a regular user, I want folders to support the same labels and links as files, so that I can organize whole projects, not just single files.], [F5],
  [US9], [As a regular user, I want to keep using my regular file manager and other tools alongside this app, so that if I rename, move, or delete something outside of it, the app still shows things correctly afterwards.], [F9],
  [US10], [As a regular user, I want to browse the files on another computer the same way I browse my own, so that I can organize files on a server without learning a new tool.], [Q1.3, @uc-remote-access],
)

=== Power User Stories

<user_stories_power>
#table(
  columns: (auto, 2.6fr, 1.2fr),
  align: (left, left, left),
  stroke: 0.4pt + luma(160),
  inset: 4pt,
  table.header([*ID*], [*User story*], [*See*]),
  [US11], [As a power user, I want to type simple text commands to search for files, add labels, and create links, so that I can automate tasks I would otherwise repeat by hand.], [F7, Q1.1, @uc-complex-dsl-query],
  [US12], [As a power user, I want the app to suggest filenames as I type a command, so that I can write commands quickly without remembering exact paths.], [F8, Q2.1, @uc-command-autocomplete],
)