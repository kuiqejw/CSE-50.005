# NS Programming Assignment : Secure File Transfer
### 50-005 Computer Structures Engineering 2018


In this project, we have implemented a secure file upload application from a client to an Internet file server using TCP sockets.
To ensure the security of the upload, we: 

1. **authenticated** the identity of the file server, and
2. encrypted the file to keep the data **confidential**
This protects our client from leaking their data to random entities including criminals and eavesdropping by any curious adversaries.


### Programming Assignment
To demonstrate the difference in performance between symmetric and asymmetric encryption, we have written two programs CP-1 and CP-2.
The first set ServerCP1 and ClientCP1 makes use of an asymmetric RSA encryption for both certificate and data encryption.
The second set ServerCP2 and ClientCP2 makes use of a symmetric AES encryption for the data, and RSA encryption for the certificate.
The performance for each program can be seen here:


![alt-text](https://github.com/kuiqejw/CSE-50.005/blob/master/ProgAss2/throughput.png "Throughput analysis")



### How to run our program
1. Prepare your private key in your project folder, or use our privateServer.der
2. Get your signed certificate from a Certificate Authority (CA) or use our server.crt, and place it in your project folder. 
3. Put the CA's certificate in your project folder, or use our CA.crt
4. Change the following variables on your Server file: port, privateKeyFile and signedCertFile by using your preferred port number, location of privateServer.der, and location of your server.crt. 
5. Run Ipconfig or Ifconfig on your server host to find Ip address 
6. Run the server host using your preferred IDE. (Netbeans)
7. Change the following variables on your Server file: port, uploadFilePath and filename by using your preferred port number, location of the path you wish to upload, and location of your CA.crt file
8. Run the client host using your preferred IDE. (Netbeans) 
7. Wait for the file to be transferred! Successful file transfer will save it in the root folder. 



### Authentication Protocol (AP) Fix
In the original AP provided with the project instructions, the server sends a standard encrypted message ("Hello, this is SecStore!") to the client during the initial handshake.
The client then decrypts the data to retrieve the server's message and verifies it. This protocol fails in the case where a malicious server interrupts the exchange and executes
a replay attack. 


![alt text](https://github.com/woshibiantai/NS-Programming-Assignment/blob/master/replayAttack.png "Replay attack example")


The malicious server can then trick the honest client into believing that he/she has succesfully uploaded their data onto the server when the server has in fact not received anything.
Although the malicous server does not own the actual server's private key to decrypt the information, the client is has to go through repeated redundant uploads that might never reach
the actual server.

To resolve this issue, the client sends a freshly generated nonce (SecureRandom BigInteger) to the server. The server encrypts this nonce with its private key and sends it back to 
the client. Since the nonce is different for every session, the malicious server cannot save the transmitted information to trick the client in future sessions.



### Protocol Specifications
![alt text](https://github.com/kuiqejw/CSE-50.005/blob/master/ProgAss2/CP1CP2.png "CP1 versus CP2")


### Bonus Functions
#### File Types
Since the data is transmitted in bytes, virtually any file type can be transferred. 

#### Concurrency
Multiple clients can upload different files to the server at one time. The server is able to handle the clients concurrently.
