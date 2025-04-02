# Search for a file
## Preconditions
- a) The user has the application opened  
- b) The user is in their home directory  
- c) The user wants to find a file  

## Flow
1. The user opens the file browser
2. The file browser is opened in the user's defined home location
3. Application shows the graph of possible neighbors from the current location
4. The user clicks on the neighbor they want to visit
5. The file was located or go back to step 3
6. The user clicks on the file 
7. The file opens

### Alternative flow
- 4a The user uses the integrated DSL for search  
- 6a The user hovers over the file  
   - 6a.1 The system shows the tags for the file  
   - 6a.2 The flow ends 
- 6b. The user shows the menu on the file
   - 6b.1 The system shows the menu with different operations available  
   - 6b.2 The user selects an operation  

## Postcondition
- a) The user accessed the wanted file  
- b) The system performed the operation