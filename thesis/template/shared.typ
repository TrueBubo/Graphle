// shorthand to vertically position elements
#let b(dy, content, size: none, weight: none) = {
    set text(size: size) if size != none
    set text(weight: weight) if weight != none
    place(dy: dy, text(content))
}

#let todo(body, big_text: 40pt, small_text: 15pt, gap: 2mm) = {
    set text(fill: black, size: small_text, weight: "bold")
    block(
      fill: yellow,
      stroke: 1pt + rgb("#e0e0e0"),
      width: 100%,
      radius: 4pt,
      inset: 10pt,
      [TODO: #body 
    #place()[    
      #set text(size: 0pt)
      #figure(kind: "todo", supplement: "", caption: body, [])
    ]])
}

//Function to insert TODOs outline
#let todo_outline = outline(
    title: [TODOs],
    target: figure.where(kind: "todo")
)

#let vocabulary-items = (
  api: (
    label: "voc_api",
    term: "API",
    title: "API (Application Programming Interface)",
    expansion: "Application Programming Interface",
    definition: "A public interface exposed by a software component so other components can call its functionality",
  ),
  apollo: (
    label: "voc_apollo",
    term: "Apollo",
    expansion: "a GraphQL client library used by GraphleUI to generate and execute typed GraphQL operations",
    definition: "A GraphQL client library used by GraphleUI to generate and execute typed GraphQL operations",
  ),
  cache: (
    label: "voc_cache",
    term: "cache",
    title: "Cache",
    expansion: "a faster temporary storage layer used to avoid repeatedly reading the same data from a slower source",
    definition: "A faster temporary storage layer used to avoid repeatedly reading the same data from a slower source",
  ),
  connection: (
    label: "voc_connection",
    term: "connection",
    title: "Connection",
    expansion: "an edge between entities in a database",
    definition: "An edge between entities in a database",
  ),
  relationship: (
    label: "voc_relationship",
    term: "relationship",
    title: "Relationship",
    expansion: "a logical connection between entities describing how they are conceptually related",
    definition: "Logical connection between entities describing how they are conceptually related",
  ),
  neighbor: (
    label: "voc_neighbor",
    term: "neighbor",
    title: "Neighbor",
    expansion: "an entity connected via a direct connection",
    definition: "Entities connected via a direct connection",
  ),
  tag: (
    label: "voc_tag",
    term: "tag",
    title: "Tag",
    expansion: "a category of an entity",
    definition: "Category of an entity",
  ),
  dsl: (
    label: "voc_dsl",
    term: "DSL",
    title: "DSL (domain-specific language)",
    expansion: "domain-specific language",
    definition: "Domain-specific language",
  ),
  filesystem: (
    label: "voc_filesystem",
    term: "filesystem",
    title: "Filesystem",
    expansion: "the operating system structure that stores files and directories and controls access to them",
    definition: "The operating system structure that stores files and directories and controls access to them",
  ),
  graphql: (
    label: "voc_graphql",
    term: "GraphQL",
    expansion: "a query language for APIs that allows clients to request exactly the data they need",
    definition: "A query language for APIs that allows clients to request exactly the data they need",
  ),
  http: (
    label: "voc_http",
    term: "HTTP",
    title: "HTTP (Hypertext Transfer Protocol)",
    expansion: "Hypertext Transfer Protocol",
    definition: "The request-response protocol used by web APIs and browsers",
  ),
  json: (
    label: "voc_json",
    term: "JSON",
    title: "JSON (JavaScript Object Notation)",
    expansion: "JavaScript Object Notation",
    definition: "A text format for structured data commonly used in API requests and responses",
  ),
  lan: (
    label: "voc_lan",
    term: "LAN",
    title: "LAN (Local Area Network)",
    expansion: "Local Area Network",
    definition: "A private network connecting devices in a limited area, such as a home or office",
  ),
  lazy_loading: (
    label: "voc_lazy_loading",
    term: "lazy loading",
    title: "Lazy loading",
    expansion: "loading data only when it is requested instead of eagerly loading everything in advance",
    definition: "Loading data only when it is requested instead of eagerly loading everything in advance",
  ),
  lpg: (
    label: "voc_lpg",
    term: "LPG",
    title: "LPG (Labeled Property Graph)",
    expansion: "Labeled Property Graph",
    definition: "A graph data model in which nodes and edges carry both labels and arbitrary key-value properties",
  ),
  metadata: (
    label: "voc_metadata",
    term: "metadata",
    title: "Metadata",
    expansion: "data that describes another object, such as tags or relationships describing a file",
    definition: "Data that describes another object, such as tags or relationships describing a file",
  ),
  rest: (
    label: "voc_rest",
    term: "REST",
    title: "REST (Representational State Transfer)",
    expansion: "Representational State Transfer",
    definition: "An API style that exposes operations through HTTP resources and methods",
  ),
  sftp: (
    label: "voc_sftp",
    term: "SFTP",
    title: "SFTP (SSH File Transfer Protocol)",
    expansion: "SSH File Transfer Protocol",
    definition: "A protocol for transferring files over an encrypted SSH connection",
  ),
  smb: (
    label: "voc_smb",
    term: "SMB",
    title: "SMB (Server Message Block)",
    expansion: "Server Message Block",
    definition: "A network file sharing protocol commonly used for accessing shared folders",
  ),
  symbolic_link: (
    label: "voc_symbolic_link",
    term: "symbolic link",
    title: "Symbolic link",
    expansion: "a filesystem entry that points to another file or directory path",
    definition: "A filesystem entry that points to another file or directory path",
  ),
  websocket: (
    label: "voc_websocket",
    term: "WebSocket",
    expansion: "a protocol for a persistent bidirectional connection between a client and a server",
    definition: "A protocol for a persistent bidirectional connection between a client and a server",
  ),
  cypher: (
    label: "voc_cypher",
    term: "Cypher",
    expansion: "the declarative query language used by Neo4j to express graph patterns",
    definition: "The declarative query language used by Neo4j to express graph patterns",
  ),
  trie: (
    label: "voc_trie",
    term: "trie",
    title: "Trie",
    expansion: "a tree data structure where each node represents a character, used for efficient prefix-based lookups",
    definition: "A tree data structure where each node represents a character, used for efficient prefix-based lookups",
  ),
)

#let vocabulary-keys = (
  "api",
  "apollo",
  "cache",
  "connection",
  "relationship",
  "neighbor",
  "tag",
  "dsl",
  "filesystem",
  "graphql",
  "http",
  "json",
  "lan",
  "lazy_loading",
  "lpg",
  "metadata",
  "rest",
  "sftp",
  "smb",
  "symbolic_link",
  "websocket",
  "cypher",
  "trie",
)

#let voc(key, text: none) = {
  let item = vocabulary-items.at(key)
  let display = if text == none { item.term } else { text }
  let used = state("vocabulary-used-" + key, false)

  context {
    let already-used = used.get()
    if already-used {
      link(label(item.label))[#display]
    } else {
      used.update(true)
      link(label(item.label))[#display (#(item.expansion))]
    }
  }
}

#let vocabulary-entry(key) = {
  let item = vocabulary-items.at(key)
  let title = if "title" in item { item.title } else { item.term }
  [*#title* - #item.definition #label(item.label)]
}
