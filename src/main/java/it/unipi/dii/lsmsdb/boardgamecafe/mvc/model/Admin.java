package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@TypeAlias("admin")
public class Admin extends GenericUser{

    public Admin() {
    }

    public Admin(String username, String salt, String hashedPassword, String _class) {
        super(username, salt, hashedPassword, _class);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", salt='" + salt + '\'' +
                ", hashedPassword='" + hashedPassword + '\'' +
                '}';
    }
}
