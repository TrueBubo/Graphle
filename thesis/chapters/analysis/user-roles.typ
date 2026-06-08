#import "../../template/shared.typ": *
== User roles

The application targets both regular users and power user. The user is able to choose what functionality they want
to utilize, and does not need to adjust to a more powerful way of traversing files all at once.

=== Regular User

A regular user is anyone who wants to organize their files beyond a plain folder hierarchy.
They interact with the application through the GUI, browsing the #voc("filesystem"), assigning #voc("tag", text: "tags") to files, 
and navigating #voc("relationship", text: "relationships") between them.
No special technical knowledge is required beyond basic computer literacy.
This user benefits from a gradual transition to a more expressive way of organizing and discovering files, 
as the application layers on top of the existing #voc("filesystem") without requiring any upfront changes.

=== Power User

A power user is a technically proficient user, most often a developer or system administrator, 
who wants to automate or script #voc("filesystem") operations. They interact with the application primarily through the custom 
#voc("dsl"), which allows them to easily perform batch operations and more advanced queries that would 
require a lot of effort using GUI. This user is familiar with command line tools and basic scripting.
This role is the ability to integrate the application into their existing workflows and tooling.
