package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public abstract class GenericUserModelMongo {

    @Id
    protected String id;
    protected String username;
    protected String email;
    protected String salt;
    protected String passwordHash;
    protected String _class;

    protected GenericUserModelMongo() {}

    protected GenericUserModelMongo(String id, String username, String email,
                                    String salt, String passwordHash,
                                    String _class)
    {
        this.id = id;
        this.username = username;
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this._class = _class;
    }

    protected GenericUserModelMongo(String username, String email,
                                    String salt, String passwordHash,
                                    String _class)
    {
        this.username = username;
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this._class = _class;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHashed() {
        return passwordHash;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPasswordHashed(String passwordHash) {
        this.passwordHash = passwordHash;
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
