package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

//import it.unipi.dii.lsmsdb.phoneworld.model.Admin;
//import it.unipi.dii.lsmsdb.phoneworld.model.GenericUser;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserDBMongo {

    public UserDBMongo() {
    }

    //private final static Logger logger = LoggerFactory.getLogger(Boardgame.class);

    @Autowired
    private UserRepoMongo userRepoMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public UserRepoMongo getUserMongo() {
        return userRepoMongo;
    }

    public boolean addUser(UserModelMongo user) {
        boolean result = true;
        try {
            userRepoMongo.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
    public boolean deleteUser(UserModelMongo user) {
        try {
            userRepoMongo.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<UserModelMongo> findByUsername(String username) {
        Optional<UserModelMongo> user = Optional.empty();
        try {
            user = userRepoMongo.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public Optional<UserModelMongo> findUserById(String id) {
        Optional<UserModelMongo> user = Optional.empty();
        try {
            user = userRepoMongo.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean deleteUserById(String id) {
        try {
            userRepoMongo.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}