package it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@TypeAlias("admin")
public class AdminModelMongo extends GenericUserModelMongo {

    public AdminModelMongo() {
    }
    public AdminModelMongo(String id, String username, String email, String salt,
                           String passwordHash, String _class) {

        super(id, username, email, salt, passwordHash, _class);
    }

    public AdminModelMongo(String username, String email, String salt,
                           String passwordHash, String _class) {

        super(username, email, salt, passwordHash, _class);
    }


    @Override
    public String toString() {
        return "Admin{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", salt='" + salt + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                '}';
    }
}