package objects;

import java.security.Principal;

/**
 * Created by Martin on 25.10.2015.
 */
public class User implements Principal {
    private long id;
    private String name;
    private String email;
    private Role role;
    public User()
    {

    }
    public User(long id, String name) {

        this.id=id;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
