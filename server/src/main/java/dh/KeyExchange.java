package dh;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static utils.CryptChatUtils.*;


public class KeyExchange {

    private X509EncodedKeySpec x509KeySpec;

    private KeyPairGenerator     serverKeyPair;
    private KeyPair                 serverPair;
    private KeyAgreement    serverKeyAgreement;
    private KeyFactory              keyFactory;

    private PrivateKey privateKey;
    private PublicKey   publicKey;


    private byte[] commonSecret;


    public void init(){
        try {
            generateDHKeyPair();
            initDHAgreement();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    //2
    public DHParameterSpec retrieveDHParamFromPB(PublicKey key){
        return ((DHPublicKey) key).getParams();
    }

    public void generateDHKeyPair() throws NoSuchAlgorithmException {
        serverKeyPair = KeyPairGenerator.getInstance("dh");
        serverKeyPair.initialize(2048);

        serverPair = serverKeyPair.generateKeyPair();
        privateKey = serverPair.getPrivate();
        publicKey  = serverPair.getPublic();

        System.err.println("pu: " + encodeBase64(privateKey.getEncoded()));
        System.err.println( "pr: " + encodeBase64(privateKey.getEncoded()));


        System.err.println("G: " + retrieveDHParamFromPB(publicKey).getG());
        System.err.println("L: " + retrieveDHParamFromPB(publicKey).getL());
        System.err.println("P: " + retrieveDHParamFromPB(publicKey).getP());
    }

    public void initDHAgreement() throws NoSuchAlgorithmException, InvalidKeyException {
        serverKeyAgreement = KeyAgreement.getInstance("dh");
        serverKeyAgreement.init(privateKey);
    }


    public String getPublicKeyEncoded(){
        return encodeBase64(publicKey.getEncoded());
    }

    public String getPrivateKeyEncoded(){
        return encodeBase64(privateKey.getEncoded());
    }


    public void receivePublicKeyFromClient(String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {

        byte[] pubKeyDecoded = decodeBase64(publicKeyBase64);

        keyFactory = KeyFactory.getInstance("dh");
        x509KeySpec = new X509EncodedKeySpec(pubKeyDecoded);

        PublicKey publicKey1 = keyFactory.generatePublic(x509KeySpec);

        serverKeyAgreement.doPhase(publicKey1, true);


        this.commonSecret = serverKeyAgreement.generateSecret();

        System.out.println("Your common secret is: " + encodeBase64(getAESKey().getEncoded()));

        System.out.println("Secret: " + commonSecret);

    }


    public byte[] getCommonSecret() {
        return commonSecret;
    }

    public SecretKeySpec getAESKey() {

        return generateAESKey(commonSecret);
    }
}
