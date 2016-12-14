# Concurrency

Distributed application whereby a single user running a server controls a
number of clients on remote computers using a combination of Java RMI and messaging.

Communication between the server and the clients uses individual Mailboxes. These are used by the
server to deposit commands and to pick up responses from the clients.

The behaviour is that the server should be able to place a command or pick up a response
(which may be empty) at any time without blocking, and the clients should be able to deposit a
reply (or part of a reply) at any time without blocking. Clients are ready for a command when they
have completed the previous command, and will then block waiting on their mailbox.