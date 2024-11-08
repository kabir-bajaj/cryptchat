CryptChat
CryptChat is a secure chat between an Android client and Java server based on TCP/IP socket connection.

This chat uses the Diffie-Hellman algorithm for the exchange of public keys and the AES algorithm for the encryption/decryption of messages. The public key carries different parameters which can be seen by anyone, it is needed to generate a common secret key. Both client and server generate a key pair, the public key mentioned before and private key used to decrypt every message. The common secret key (generated by the Diffie-Hellman algorithm) encrypts the messages. The message sent over the network socket are encoded in BASE64.

How to run the project
Simply use Intellij with Maven to run the server and Android studio with gradle for the client. The IDE will automatically detect the project.

Contributing
Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are greatly appreciated.

Fork the Project
Create your Feature Branch (git checkout -b feature/AmazingFeature)
Commit your Changes (git commit -m 'Add some AmazingFeature')
Push to the Branch (git push origin feature/AmazingFeature)
Open a Pull Request