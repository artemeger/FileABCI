package crypto;

import com.google.protobuf.ByteString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public abstract class RipeMD160 {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static ByteString getHashFromBytes(ByteString msg) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("RIPEMD160");
        return ByteString.copyFrom(md.digest(msg.toByteArray()));
    }
}
