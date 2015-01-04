NAT-Server
===================================
Simple Simulation of a NAT Server
-------------

#### Features: ####
* Add public addresses (admin)
* Set Dynamic NAT or Static NAT (admin)
* Check Log (admin)
* Ask for public addresses (client)
* Connect to a destination address (client)

####Building:####
	mkdir bin
    javac src/*.java -d bin/	

####Usage:####
**Running the server**:

    cd bin/
    java NATServer <port>

**Running a client**:

    cd bin/
    nc <hostname> <port>
