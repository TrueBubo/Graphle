#import "../../template/shared.typ": *
== Security

Since the application operates directly on the user's #voc("filesystem") and can be accessed remotely through SSH port forwarding, security must be considered.
The current implementation does not provide application-level authentication.
Instead, it relies on the operating system account under which `GraphleManager` runs, filesystem permissions, and the access control already provided by SSH.
This is a deliberate limitation of the bachelor's thesis implementation rather than a complete security model.
Multi-user support and authorization for remote clients are left as a future work.

=== Accounts
The system will not have the ability to create custom users, as operating systems already provide this functionality.
The user account used is the account of the user who launched the application. Therefore, to create a new user, the user creates
a new user account in the operating system. This has the benefit that the system itself does not need to concern
itself with storing user credentials, which could be a significant security liability if the system were exposed to the internet.
Furthermore, regular users often set passwords in a way that allows malicious actors to crack them easily. @password-stolen
Instead, users are advised to set up SSH port forwarding @ssh-port-forwarding. This is the method that many other applications, 
such as DBeaver @dbeaver-ssh, use for safer access.

=== File permissions
The application inherits all the file permissions from the user who launched the system. To restrict a user's access, 
create a new user account for them and assign the necessary permissions.
