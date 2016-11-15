package objects;

/**
 * Created by Martin on 29.02.2016.
 */
public class PasswordData {
    private byte[] encryptedPassword;
    private byte[] salt;

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(byte[] encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}

