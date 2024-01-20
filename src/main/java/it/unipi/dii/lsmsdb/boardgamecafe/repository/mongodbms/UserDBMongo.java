package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;

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

    public boolean addUser(GenericUserModelMongo user) {
        boolean result = true;
        try {
            userRepoMongo.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
    public boolean deleteUser(GenericUserModelMongo user) {
        try {
            userRepoMongo.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<GenericUserModelMongo> findByUsername(String username) {
        Optional<GenericUserModelMongo> user = Optional.empty();
        try {
            user = userRepoMongo.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public Optional<GenericUserModelMongo> findUserById(String id) {
        Optional<GenericUserModelMongo> user = Optional.empty();
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