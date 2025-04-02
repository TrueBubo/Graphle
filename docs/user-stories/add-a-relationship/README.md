# Add a [relationship](../../vocabulary.md/#relationship)
## Preconditions
- a) The user has the application opened
- b) The file browser is connected to a Graphle server
- c) The user wants to connect two files with their relationship

## Flow
1. The user opens a file browser
2. The user [searches for a file](../search-for-a-file)
3. The user views the menu on the file
4. The system shows the menu with different operation 
5. The user selects the operation "Create a new relationship"
6. The user [searches for another file](../search-for-a-file)
7. The user views the menu on the file 
8. The system shows the menu with different operation 
9. The user selects the operation "Add to the relationship"
10. The system displays the menu where the user can enter info about the relationship (name, is bidirectional)
11. The user enters the information and submits it;

## Postconditions
- a) The system remembers a new relationship between two entities
- b) The relationship has a name set by the user
- c) The user can traverse the relationship to find the second file straight from the first file 