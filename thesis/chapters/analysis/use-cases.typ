#import "../../template/shared.typ": *
== Use Cases

This section outlines the use cases describing how the user interacts with the application. Use cases range from
a variety of functionalities, including creating and deleting semantic information, browsing through the filesystem,
and using the DSL.

#figure(
  placement: none,
  image("use-cases/use-case/use-case-diagram.svg"),
  caption: [Use case diagram]
) <use-case-diagram>

The @use-case-diagram shows the overall picture of use cases and how they relate to each other.

#include "use-cases/search-for-a-file/search-for-a-file.typ"

#include "use-cases/add-a-relationship/add-a-relationship.typ"

#include "use-cases/remove-a-relationship/remove-a-relationship.typ"

#include "use-cases/add-a-tag/add-a-tag.typ"

#include "use-cases/remove-a-tag/remove-a-tag.typ"

#include "use-cases/complex-dsl-query-construction/complex-dsl-query-construction.typ"

#include "use-cases/command-auto-completion/command-auto-completion.typ"

