# NetConfAgent
A network management tool that helps to configure and maintain a remote network element that implements the NetConf protocol.

A Network Configuration Protocol (NETCONF) is a network management protocol developed and standardized by the IETF. The NetConf protocol specification is an Internet Standards Track document. NetConf provides mechanisms to install, manipulate and delete the configuration of network devices. Its operations are realized on top of a simple Remote Procedure Call (RPC) layer. The NetConf protocol uses an Extensible Markup Language (XML) based data encoding for the configuration data as well as the protocol messages. The protocol messages are exchanged on top of a secure transport protocol.
The project was to implement a network management tool that helps to configure and maintain a remote network element that implements the NetConf protocol.

## Abstract

Netconf Agent resides on Network Element and responds to Manager’s request to perform operations on the Configuration Data Store. The Network Configuration Protocol (NETCONF) provides mechanisms to install, manipulate and delete the configuration of network devices. It uses an Extensible Markup Language (XML)–based data encoding for the configuration data as well as the protocol messages. The NETCONF protocol operations are realized as remote procedure calls (RPCs). YANG is a data modelling language used to model configuration and state data manipulated by Network Configuration Protocol (NETCONF), NETCONF remote procedure calls, and NETCONF notifications.
<br />Problem Statement: <br />
->	Develop NetConf Agent<br />
->	Implement Support for below Operations:<br />
•	GET<br />
•	GET-Config<br />
•	EDIT-Config<br />
•	DELETE-Config<br />
•	COPY-Config<br />
•	LOCK<br />
•	UNLOCK<br />
•	CLOSE-SESSION<br />
•	KILL-SESSION
