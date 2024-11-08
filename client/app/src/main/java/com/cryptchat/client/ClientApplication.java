package com.cryptchat.client;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;

import com.cryptchat.client.dh.KeyExchange;
import com.cryptchat.client.asynctask.ReceiveMessage;
import com.cryptchat.client.asynctask.SendMessage;
import com.cryptchat.client.utils.Base64;
import com.cryptchat.client.utils.CryptChatUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

import static com.cryptchat.client.utils.CryptChatUtils.PROTOCOL_SEPARATOR;
import static com.cryptchat.client.utils.CryptChatUtils.PUBLIC_KEY;

public class ClientApplication extends Application implements KeyExchange.SendKey {

    private boolean isConnected;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private KeyExchange keyExchange;
    private ReceiveMessage receive;

    private ChatResponse chatClient;

    private LoginResponse loginResponse;
    private ReceiveMessage.ReceiveMessageResponse receiveMessageResponse;
    private SendMessage.SendMessageResponse sendMessageResponse;


    @Override
    public void onCreate() {
        receive = new ReceiveMessage(this);
        super.onCreate();
    }

    public void executeKeyExchange(byte[] pbk){
        keyExchange = new KeyExchange(this);
        keyExchange.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pbk);
    }

    public void executeReceive(){

        receive.setResponse(receiveMessageResponse);
        receive.setIsConnected(isConnected());
        receive.setOis(ois);
        receive.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void executeSend(String message) throws Exception{
        if(message.isEmpty()){
            throw new Exception("You must enter a message.");
        }

        SendMessage send;
        send = new SendMessage();
        send.setResponse(sendMessageResponse);
        send.setOos(oos);
        send.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);

    }


    public String decrypt(String message) throws Exception {
        return CryptChatUtils.decrypt(message, keyExchange.getAESKey());
    }

    public String encrypt(String message) throws Exception {
        return CryptChatUtils.encrypt(message, keyExchange.getAESKey());
    }


    public void sendMessage(String message) throws Exception {
        executeSend(CryptChatUtils.ENCRYPTED_MESSAGE + CryptChatUtils.PROTOCOL_SEPARATOR + message);
    }

    public void ready() throws Exception {
        executeSend(CryptChatUtils.READY + CryptChatUtils.PROTOCOL_SEPARATOR + "ready");
    }

    public void connect(String ip, String port){
        Connect connect = new Connect();
        connect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ip, port);
    }

    @Override
    public void openDialogPublicKey() {
        chatClient.openDialogPublicKey();

    }

    @Override
    public void closeDialogPublicKey() {
        chatClient.closeDialogPublicKey();
    }

    @Override
    public void openDialogKeyPair() {
        chatClient.openDialogKeyPair();
    }

    @Override
    public void closeDialogKeyPair() {
        chatClient.closeDialogKeyPair();
    }

    @Override
    public void sendPublicKey(PublicKey publicKey) {
        byte[] encodedPublicKey = publicKey.getEncoded();
        String base64PublicKey = Base64.encode(encodedPublicKey);
        String formatPBK = PUBLIC_KEY + PROTOCOL_SEPARATOR + base64PublicKey;
        try {
            executeSend(formatPBK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendError(String error) { }

    @Override
    public void showPrivateKey(String s) {
        chatClient.showPrivateKey(s);
    }


    private void initStreams(Socket server) {
        try {
            oos = new ObjectOutputStream(server.getOutputStream());
            ois = new ObjectInputStream(server.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            oos.close();
            ois.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class Connect extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginResponse.openDialogConnection();
        }

        @Override
        protected Boolean doInBackground(String... networkData) {
            String ip = networkData[0];
            int port = Integer.parseInt(networkData[1]);

            try {
                System.err.println("connecting");
                socket = new Socket(ip, port);
                isConnected = true;

                CryptChatUtils.delay(5000);
                initStreams(socket);
            } catch (IOException e) {
                e.printStackTrace();
                loginResponse.showErrorMessage(e.getMessage());
                isConnected = false;
                closeConnection();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean connected) {
            super.onPostExecute(connected);
            if (connected) {
                loginResponse.closeDialogConnection();
                loginResponse.openChat();
            }
        }

    }


    public boolean isConnected() {
        return isConnected;
    }


    public void setLoginResponse(LoginResponse loginResponse) {
        this.loginResponse = loginResponse;
    }

    public void setChatClient(ChatResponse chatClient) {
        this.chatClient = chatClient;
    }

    public void setReceiveMessageResponse(ReceiveMessage.ReceiveMessageResponse receiveMessageResponse) {
        this.receiveMessageResponse = receiveMessageResponse;
    }

    public void setSendMessageResponse(SendMessage.SendMessageResponse sendMessageResponse) {
        this.sendMessageResponse = sendMessageResponse;
    }



    public interface ChatResponse {
        void showPrivateKey(String s);
        void showMessageReceived(String s);

        void openDialogKeyPair();
        void closeDialogKeyPair();

        void openDialogPublicKey();
        void closeDialogPublicKey();
    }

        public interface LoginResponse {
        void openChat();

        void openDialogConnection();
        void closeDialogConnection();

        void showErrorMessage(String message);
    }
}