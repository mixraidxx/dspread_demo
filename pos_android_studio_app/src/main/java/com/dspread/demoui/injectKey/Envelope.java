package com.dspread.demoui.injectKey;

import com.dspread.demoui.QPOSUtil;

import java.io.InputStream;
import static com.dspread.demoui.QPOSUtil.byteArray2Hex;
import static com.dspread.demoui.injectKey.RSA.hexStringToBytes;

public class Envelope {
    public static String digitalEnvelopStr;


    public Envelope() {

    }

    public static String getEncryptedDataByPublicKey(String tmStr, String publicKey){
        String digEnvelopStr = null;
        Poskeys posKeys = null;
        try {
            posKeys = new TMKKey();
            TMKKey tmkKey = (TMKKey) posKeys;
            tmkKey.setTMKKEY(tmStr);
            posKeys.setRSA_public_key(publicKey); //Model of device public key
            digEnvelopStr = getDigitalEnvelopStrByKey(null,
                    posKeys, Poskeys.RSA_KEY_LEN.RSA_KEY_1024, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return digEnvelopStr;
    }
    /*
    * 01020000 包头
    * 16000000 4字节长度
    * 04 更新TMK Tag
    * 11000000 4字节长度
    * 00 1字节密钥组
    * 0123456789ABCDEFFEDCBA9876543210 TMK
    *
    * */

    public static String getDigitalEnvelopStrByKey(InputStream in, Poskeys posKeys, Poskeys.RSA_KEY_LEN rsa_key_len , int keyIndex) {
        String ipekKeyStr = null;
        try {
            if (posKeys instanceof DukptKeys) {
                DukptKeys dukptKeys = (DukptKeys) posKeys;
                String trackipekString = dukptKeys.getTrackipek();
                String emvipekString = dukptKeys.getEmvipek();
                String pinipekString = dukptKeys.getPinipek();
                byte[] trackipek = hexStringToBytes(trackipekString);
                byte[] emvipek = hexStringToBytes(emvipekString);
                byte[] pinipek = hexStringToBytes(pinipekString);
                String trackksn = dukptKeys.getTrackksn();
                String emvksn = dukptKeys.getEmvksn();
                String pinksn = dukptKeys.getPinksn();
                String tmkString = dukptKeys.getTmk();
                byte[] tmk = hexStringToBytes(tmkString);
//                String trackipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, trackipek));
//                String emvipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, emvipek));
//                String pinipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, pinipek));
                ipekKeyStr = trackksn + trackipekString + emvksn + emvipekString + pinksn + pinipekString;
            } else if (posKeys instanceof TMKKey) {
                TMKKey tmkKey = (TMKKey) posKeys;
                ipekKeyStr = tmkKey.getTMKKEY();
            }

            byte[] setIpekKeyStrData = new byte[5 + ipekKeyStr.length() / 2];
            if (posKeys instanceof DukptKeys)
                setIpekKeyStrData[0] = 0;
            else if (posKeys instanceof TMKKey)
                setIpekKeyStrData[0] = 4;
            byte[] lenBytes = Utils.int2Byte(ipekKeyStr.length() / 2+1);
            System.arraycopy(lenBytes, 0, setIpekKeyStrData, 1, lenBytes.length);
            byte[] ipekBytes = hexStringToBytes(ipekKeyStr);
            byte bytKeyIndex = (byte) keyIndex;

            if (posKeys instanceof DukptKeys){
                System.arraycopy(ipekBytes, 0, setIpekKeyStrData, 1 + lenBytes.length, ipekBytes.length);
                setIpekKeyStrData[setIpekKeyStrData.length -1] = bytKeyIndex;
            } else if (posKeys instanceof TMKKey){
                setIpekKeyStrData[5] = bytKeyIndex;
                System.arraycopy(ipekBytes, 0, setIpekKeyStrData, 1 + lenBytes.length, ipekBytes.length);
            }
            System.out.println("message5:" + byteArray2Hex(setIpekKeyStrData));

            byte[] command = new byte[]{1, 3, 0, 0};
            byte[] message2 = new byte[8 + setIpekKeyStrData.length];
            System.out.println("message-6:" + byteArray2Hex(message2));
            System.arraycopy(command, 0, message2, 0, command.length);
            System.out.println("message--6:" + byteArray2Hex(message2));
            lenBytes = Utils.int2Byte(setIpekKeyStrData.length);
            System.out.println("message----6:" + byteArray2Hex(lenBytes));
            System.arraycopy(lenBytes, 0, message2, 0 + command.length, lenBytes.length);
            System.out.println("message---6:" + byteArray2Hex(message2));
            System.arraycopy(setIpekKeyStrData, 0, message2, 0 + command.length + lenBytes.length, setIpekKeyStrData.length);
            System.out.println("message----6:" + byteArray2Hex(message2));

            RSA senderRsa = new RSA();
            RSA receiverRsa = new RSA();
            senderRsa.loadPrivateKey(in); //私钥
            String n = posKeys.getRSA_public_key();
            String e = "010001";
            System.out.println("message-----6:" + n);
            receiverRsa.loadPublicKey(n, e);//公钥

            return packageEnvelopFun(message2,senderRsa,receiverRsa,rsa_key_len);
        } catch (Exception e) {
            return digitalEnvelopStr;
        }
    }

    public static String updateTokenTest(InputStream in, Poskeys posKeys, Poskeys.RSA_KEY_LEN rsa_key_len , int keyIndex) throws Exception {
        Envelope envelope = new Envelope();


        String Token = "0F9112700110000000000112A4537790020123";

        //System.out.println(token);

        byte[] setTokenStrData = new byte[1 + 4 + Token.length() / 2];
        setTokenStrData[0] = 0x00;
        byte[] lenBytes = QPOSUtil.int2Byte(Token.length() / 2);
        System.arraycopy(lenBytes, 0, setTokenStrData, 1, lenBytes.length);

        byte[] tokenBytes = hexStringToBytes(Token);
        System.arraycopy(tokenBytes, 0, setTokenStrData, 1 + lenBytes.length, tokenBytes.length);

        byte[] command = new byte[]{0x01,0x03,0x00,0x00};
        byte[] message2 = new byte[4 + 4 + setTokenStrData.length];
        System.arraycopy(command, 0, message2, 0, command.length);
        lenBytes = QPOSUtil.int2Byte(setTokenStrData.length);
        System.arraycopy(lenBytes, 0, message2, 0 + command.length, lenBytes.length);
        System.arraycopy(setTokenStrData, 0, message2, 0 + command.length + lenBytes.length, setTokenStrData.length);


		/* fist convert your rsa private key into PKCS#8 format
        openssl pkcs8 -topk8 -inform PEM -outform PEM -in private_key.pem  -out private_rsa_pkcs8.pem -nocrypt
        openssl pkcs8 -topk8 -inform PEM -outform DER -in private_rsa.pem  -out private_key.der -nocrypt
        n is the export data from QPOS
		 */

        RSA senderRsa = new RSA();
        RSA receiverRsa = new RSA();
        senderRsa.loadPrivateKey(in);
        String n = posKeys.getRSA_public_key();
        String e = "010001";
        System.out.println("message-----6:" + n);
        receiverRsa.loadPublicKey(n, e);//公钥

        byte[] de = new byte[0];
        try {
            de = envelope(message2, senderRsa, receiverRsa);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        int blockSize = de.length / 256;
        if((de.length % 256) != 0){
            blockSize += 1;
        }

        byte[] pde = new byte[blockSize * 256];
        for (int i = 0; i < pde.length; i++) {
            pde[i] = (byte)0xFF;
        }
        for(int i=0;i<10000;i++)
        {
            i+=1;
        }
        System.arraycopy(de, 0, pde, 0, de.length);

        System.out.println("de:"+de.length+"\n"+"pde:"+pde.length);
		/*String data=envelope.bytes2hex(pde);
		StringBuffer sb=new StringBuffer();
		sb.append(data);
		if (data.length()<1024) {
			for (int i = 0; i <1024-data.length(); i++) {
				sb.append("F");
			}
		}
		String resultData=sb.toString();
		System.out.println(resultData); */

        System.out.println(envelope.bytes2hex(pde));
        System.out.println("length:"+envelope.bytes2hex(pde).length());
		/*
		FileOutputStream dst = new FileOutputStream(new File("stm8_firmware.hex.message" + ".package"));
		dst.write(pde);
		dst.close();

		FileWriter fw = new FileWriter(new File("stm8_firmware.hex.message" + ".package.asc"));
		fw.write(envelope.bytes2hex(pde));
		fw.close();*/
        return QPOSUtil.byteArray2Hex(pde);
    }

    public static byte[] envelope(byte[] message, RSA senderRsa, RSA receiverRsa) throws Exception{
        System.out.println("key before Encryption : " +bytes2hex(getTdesKey()));
        System.out.println("message : " +bytes2hex(message));

        byte[] encrypedTdesKey = receiverRsa.encrypt(getTdesKey());
        System.out.println("key after Encryption : " +bytes2hex(encrypedTdesKey));
        byte[] encrypedMessage = encrypt(message,senderRsa);
        byte[] toShaMessage = new byte[encrypedTdesKey.length + encrypedMessage.length];

        System.arraycopy(encrypedTdesKey, 0, toShaMessage, 0, encrypedTdesKey.length);
        System.arraycopy(encrypedMessage, 0, toShaMessage, encrypedTdesKey.length, encrypedMessage.length);
        byte[] signedMessage = senderRsa.sign(toShaMessage);
        byte[] results = new byte[4 + encrypedTdesKey.length + encrypedMessage.length + signedMessage.length];
        int len = encrypedTdesKey.length + encrypedMessage.length + signedMessage.length;
        byte[] lenBytes = QPOSUtil.int2Byte(len);
        System.arraycopy(lenBytes, 0, results, 0, lenBytes.length);
        System.arraycopy(encrypedTdesKey, 0, results, lenBytes.length, encrypedTdesKey.length);
        System.arraycopy(encrypedMessage, 0, results, lenBytes.length + encrypedTdesKey.length, encrypedMessage.length);
        System.arraycopy(signedMessage, 0, results, lenBytes.length + encrypedTdesKey.length + encrypedMessage.length, signedMessage.length);
        return results ;
    }
    /*
    * 01030000 4字节包头
    * 14000000 4字节长度
    * 00 代表更新Token Tag
    * 10000000 4字节长度
    * 0A11111111111111111111 Token
    * 01FF Token有效期 255秒
    *
    * */
    public static String updateTokenByDigitalEnvelope(InputStream in, Poskeys posKeys, Poskeys.RSA_KEY_LEN rsa_key_len , int keyIndex) {
        String tokenStr = null;
        try {
            tokenStr = "0A1111111111111111111101FF"; //Token明文包: 1字节TOKEN长度+N字节Token(最长10字节)+1字节TOKEN有效时间长度+N字节TOKEN有效时间时间（秒） 过了这个有效时间, Token将失效, 无法继续交易.
            byte[] setIpekKeyStrData = new byte[5 + tokenStr.length() / 2];
            setIpekKeyStrData[0] = 0;   // Token 的 tag
            byte[] lenBytes = Utils.int2Byte(tokenStr.length() / 2+1);
            System.arraycopy(lenBytes, 0, setIpekKeyStrData, 1, lenBytes.length);
            byte[] ipekBytes = hexStringToBytes(tokenStr);
            byte bytKeyIndex = (byte) keyIndex;

            System.arraycopy(ipekBytes, 0, setIpekKeyStrData, 1 + lenBytes.length, ipekBytes.length);
            System.out.println("message5:" + byteArray2Hex(setIpekKeyStrData));

            byte[] command = new byte[]{1, 3, 0, 0};
            byte[] message2 = new byte[8 + setIpekKeyStrData.length];
            System.out.println("message-6:" + byteArray2Hex(message2));
            System.arraycopy(command, 0, message2, 0, command.length);
            System.out.println("message--6:" + byteArray2Hex(message2));
            lenBytes = Utils.int2Byte(setIpekKeyStrData.length);
            System.out.println("message----6:" + byteArray2Hex(lenBytes));
            System.arraycopy(lenBytes, 0, message2, 0 + command.length, lenBytes.length);
            System.out.println("message---6:" + byteArray2Hex(message2));
            System.arraycopy(setIpekKeyStrData, 0, message2, 0 + command.length + lenBytes.length, setIpekKeyStrData.length);
            System.out.println("message----6:" + byteArray2Hex(message2));

            RSA senderRsa = new RSA();
            RSA receiverRsa = new RSA();
            senderRsa.loadPrivateKey(in); //私钥
            String n = posKeys.getRSA_public_key();
            String e = "010001";
            System.out.println("message-----6:" + n);
            receiverRsa.loadPublicKey(n, e);//公钥

            return packageEnvelopFun(message2,senderRsa,receiverRsa,rsa_key_len);
        } catch (Exception e) {
            return digitalEnvelopStr;
        }
    }


    private static String packageEnvelopFun(byte[] message2,RSA senderRsa,RSA receiverRsa,Poskeys.RSA_KEY_LEN rsa_key_len) {
        try{

            byte[] de = null;
            if (rsa_key_len == Poskeys.RSA_KEY_LEN.RSA_KEY_1024)
                de = byteEvelope(message2, senderRsa, receiverRsa);
            else if (rsa_key_len == Poskeys.RSA_KEY_LEN.RSA_KEY_2048)
                de = byteEvelope(message2, senderRsa, receiverRsa, 2048);
            else {
                throw new Exception("Bad key length");
            }
            int blockSize = de.length / 256;
            if (de.length % 256 != 0) {
                ++blockSize;
            }

            byte[] pde = new byte[blockSize * 256];

            int i;
            for (i = 0; i < pde.length; ++i) {
                pde[i] = -1;
            }

            for (i = 0; i < 10000; ++i) {
                ++i;
            }

            System.arraycopy(de, 0, pde, 0, de.length);
            System.out.println("de:" + de.length + "\n" + "pde:" + pde.length);
            System.out.println(bytes2hex(pde));
            digitalEnvelopStr = bytes2hex(pde);
            System.out.println("length:" + bytes2hex(pde).length());
            System.out.println("digitalEnvelopStr:" + digitalEnvelopStr);
            return digitalEnvelopStr;
        } catch (Exception var30) {
            var30.printStackTrace();
            return digitalEnvelopStr;
        }
    }

    public static byte[] byteEvelope(byte[] message, RSA senderRsa, RSA receiverRsa) throws Exception {

        return byteEvelope(message, senderRsa, receiverRsa, 1024);
    }

    public static byte[] byteEvelope(byte[] message, RSA senderRsa, RSA receiverRsa, int RSA_len) throws Exception {
        byte[] encrypedTdesKey = receiverRsa.encrypt(getTdesKey());
        System.out.println("encrypedTdesKey:" + byteArray2Hex(encrypedTdesKey));
        byte[] encrypedMessage = encrypt(message, senderRsa);
        System.out.println("encrypedMessage:" + byteArray2Hex(encrypedMessage));
        byte[] toSha1Message = new byte[encrypedTdesKey.length + encrypedMessage.length];
        System.arraycopy(encrypedTdesKey, 0, toSha1Message, 0, encrypedTdesKey.length);
        System.arraycopy(encrypedMessage, 0, toSha1Message, encrypedTdesKey.length, encrypedMessage.length);
        System.out.println("toSha1Message:" + byteArray2Hex(toSha1Message));
        byte[] signedMessage = senderRsa.sign(toSha1Message);
        System.out.println("signedMessage:" + byteArray2Hex(signedMessage));
        byte[] results = new byte[4 + encrypedTdesKey.length + encrypedMessage.length + signedMessage.length];
        int len = encrypedTdesKey.length + encrypedMessage.length + signedMessage.length;
        byte[] lenBytes = Utils.int2Byte(len);
        if (RSA_len == 2048) {
            lenBytes[3] = (byte) 0x80;
        }
        System.out.println("encrypedTdesKey:" + encrypedTdesKey.length + "\n" + "encrypedMessage:" + encrypedMessage.length + "\n" + "signedMessage:" + signedMessage.length);
        System.arraycopy(lenBytes, 0, results, 0, lenBytes.length);
        System.arraycopy(encrypedTdesKey, 0, results, lenBytes.length, encrypedTdesKey.length);
        System.arraycopy(encrypedMessage, 0, results, lenBytes.length + encrypedTdesKey.length, encrypedMessage.length);
        System.arraycopy(signedMessage, 0, results, lenBytes.length + encrypedTdesKey.length + encrypedMessage.length, signedMessage.length);
        return results;
    }

    public static byte[] encrypt(byte[] message, RSA senderRsa) throws Exception {
        byte[] packagedMessage = packageMessage(message);
        System.out.println("packagedMessage:" + byteArray2Hex(packagedMessage));
        int blockSize = packagedMessage.length / 8;
        if (packagedMessage.length % 8 != 0) {
            ++blockSize;
        }

        byte[] padedPackagedMessage = new byte[blockSize * 8];

        int i;
        for (i = 0; i < padedPackagedMessage.length; ++i) {
            padedPackagedMessage[i] = -1;
        }
        System.out.println("packagedMessage:" + byteArray2Hex(padedPackagedMessage));
        System.arraycopy(packagedMessage, 0, padedPackagedMessage, 0, packagedMessage.length);
        byte[] encryptedMess = new byte[blockSize * 8];
        System.out.println("packagedMessage:" + byteArray2Hex(padedPackagedMessage));

        for (i = 0; i < blockSize; ++i) {
            byte[] temp = new byte[8];
            byte[] temp2 = new byte[8];
            System.arraycopy(padedPackagedMessage, i * 8, temp, 0, 8);
            System.out.println("temp:" + byteArray2Hex(temp));
            temp2 = TDES.tdesCBCEncypt(getTdesKey(), temp);
            System.arraycopy(temp2, 0, encryptedMess, i * 8, 8);
            System.out.println("temp2:" + byteArray2Hex(temp2));
        }

        return encryptedMess;
    }

    public static byte[] packageMessage(byte[] message) {
        byte[] results = new byte[message.length + 8];

        for (int i = 0; i < results.length; ++i) {
            results[i] = 0;
        }

        byte[] lenBytes = Utils.int2Byte(message.length);
        System.arraycopy(lenBytes, 0, results, 0, lenBytes.length);
        System.arraycopy(message, 0, results, 8, message.length);
        return results;
    }

    public static byte[] getTdesKey() {
        return new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    }

    public static String bytes2hex(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString().toUpperCase();
        } else {
            return null;
        }
    }
}
