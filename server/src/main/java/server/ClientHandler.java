package server;

import dh.KeyExchange;
import utils.CryptChatUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static utils.CryptChatUtils.*;

public class ClientHandler extends Thread{

    private Socket client;

    private Server.ServerResponse response;
    private KeyExchange keyExchange;

    private ObjectOutputStream oos;
    private ObjectInputStream  ois;

    private boolean isClientConnected;

    ClientHandler(Socket client, Server.ServerResponse response){
        this.client = client;
        this.response = response;
        this.isClientConnected = true;
    }




    private void initStreams(Socket client) {
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            ois = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run();
        initStreams(client);

        try {
            receiveMessages();
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
            System.err.println("Closing server...");
        }
    }

    private void receiveMessages() throws IOException {
        while (isClientConnected){
            String dataEncoded = ois.readUTF();
            performOperation(retrieveCommand(dataEncoded), retrieveMessage(dataEncoded));
        }
    }


    private void performOperation(int c, String message) {
        if(c == ENCRYPTED_MESSAGE){
            response.showMessageReceived(message);
            response.notifyMessageReceived();
            System.err.println("message received");
        }else if(c == PUBLIC_KEY){
            System.err.println("Receiving public key from client..");

               try {

                   System.out.println("pbk via socket: " + message);
                   try {
                       keyExchange.receivePublicKeyFromClient(message);
                   } catch (InvalidKeySpecException e) {
                       e.printStackTrace();
                   } catch (InvalidKeyException e) {
                       e.printStackTrace();
                   }
                   response.showPrivateKey(encodeBase64(keyExchange.getAESKey().getEncoded()));
               } catch (NoSuchAlgorithmException e) {
                   e.printStackTrace();
               }

        }else if(c == READY){
            System.err.println("Client is ready to received the key...");
            sendPublicKey(keyExchange.getPublicKeyEncoded());
        }

    }


    public void requestNewKey(){
        System.err.println(CryptChatUtils.REQUEST_NEW_KEY + CryptChatUtils.PROTOCOL_SEPARATOR + keyExchange.getPublicKeyEncoded());
        sendMessage(REQUEST_NEW_KEY, CryptChatUtils.PROTOCOL_SEPARATOR, keyExchange.getPublicKeyEncoded());

    }

    public void sendMessage(int command, String separator, String message){
        try {
            String s = command + separator + message;
            oos.writeUTF(s);
            System.out.println("Sending: " + s);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeConnection(){
        try {
            ois.close();
            oos.close();
            client.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    public void setKeyExchange(KeyExchange keyExchange) {
        this.keyExchange = keyExchange;
    }

    public void sendPublicKey(String key){
        System.err.println(CryptChatUtils.REQUEST_PUBLIC_KEY + CryptChatUtils.PROTOCOL_SEPARATOR + key);

        sendMessage(CryptChatUtils.REQUEST_PUBLIC_KEY, CryptChatUtils.PROTOCOL_SEPARATOR, key);
    }

    public KeyExchange getKeyExchange() {
        return keyExchange;
    }
}
