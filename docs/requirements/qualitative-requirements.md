# Qualitative requirements
This list serves as a requirement list for how well they should handle the users' wishes.
- **Usability**
    - The application provides the users with the option to choose between interacting via DSL or GUI
    - Users can set other themes or use a different GUI client altogether
    - Users can connect remotely
- **Performance**
    - The autocomplete responds with available filenames within 250ms
    - The GUI should update within 200ms after clicking to see [neighbors](../vocabulary.md/#neighbor) for a node
- **Security**
  - Users need to authenticate to access the API
- *Q4 Reliability*
    - The application will retry performing failing operations before giving up
    - The failure of auxiliary components, such as an auto-completer or GUI, does not prevent the core from running
- **Extensibility**
  - The application can be extended to allow new commands
