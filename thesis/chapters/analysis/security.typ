#import "../../template/shared.typ": *
== Security
=== Accounts
The system will not have the ability to create custom users, as operating systems themselves already provide this functionality. The user account used is the account of the user who launched the application. Hence, to create a new user, you create a new user account in your operating system. This comes with the benefit that the system itself does not need to concern itself with holding user credentials, which could be a significant security liability if the system was exposed to the internet. Furthermore, regular people often set passwords in a way that allows malicious actors to crack them easily. @password-stolen Instead, I recommend that users set up SSH port forwarding @ssh-port-forwarding. This is the method that many other applications, such as DBeaver @dbeaver-ssh, use for safer access.

=== File permissions
The application inherits all the file permissions from the user who launched the system. To restrict a user's access, create a new user account for them and assign the necessary permissions.
