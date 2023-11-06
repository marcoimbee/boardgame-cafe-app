/*
package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public abstract class GenericUser {

    @Id
    protected String id;
    protected String username;
    protected String salt;
    protected String hashedPassword;
    protected String _class;

    protected GenericUser() {
    }

    protected GenericUser(String username, String salt, String hashedPassword, String _class) {
        this.username = username;
        this.salt = salt;
        this.hashedPassword = hashedPassword;
        this._class = _class;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}
*/