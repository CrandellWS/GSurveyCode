package objects;

/**
 * Created by Martin on 06.03.2016.
 */
public class GoogleProfile {
    private String givenName;
    private String familyName;
    private String userId;
    private String email;
    private boolean emailVerified;
    private String name;
    private String pictureUrl;
    private String locale;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getGivenName() {
        return givenName;
    }
}
