/*
package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

//import it.unipi.dii.lsmsdb.phoneworld.model.Admin;
//import it.unipi.dii.lsmsdb.phoneworld.model.GenericUser;
//import it.unipi.dii.lsmsdb.phoneworld.model.User;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class UserMongoDB {

    public UserMongo() {
    }

    private final static Logger logger = LoggerFactory.getLogger(PhoneMongo.class);

    @Autowired
    private IUserMongo userMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public IUserMongo getUserMongo() {
        return userMongo;
    }

    public boolean addUser(GenericUser user) {
        boolean result = true;
        try {
            userMongo.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<GenericUser> findUsers(String username, String classType) {
        List<GenericUser> users = new ArrayList<>();
        try {
            users.addAll(userMongo.findByUsernameRegexAnd_class(username, classType));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public Optional<GenericUser> findByUsername(String username) {
        Optional<GenericUser> user = Optional.empty();
        try {
            user = userMongo.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public Optional<GenericUser> findUserById(String id) {
        Optional<GenericUser> user = Optional.empty();
        try {
            user = userMongo.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public boolean updateUser(String id, GenericUser newGenericUser, String userType) {
        boolean result = true;
        try {
            Optional<GenericUser> genericUser = userMongo.findById(id);
            if (genericUser.isPresent()) {
                if (userType.equals("admin")) {
                    Admin administrator = (Admin) genericUser.get();
                    administrator.setUsername(newGenericUser.getUsername());
                    administrator.setHashedPassword(newGenericUser.getHashedPassword());
                    administrator.setSalt(newGenericUser.getSalt());
                    this.addUser(administrator);
                } else {
                    User user = (User) genericUser.get();
                    User newUser = (User)newGenericUser;
                    user.setUsername(newUser.getUsername());
                    user.setHashedPassword(newUser.getHashedPassword());
                    user.setSalt(newUser.getSalt());
                    user.setAge(newUser.getAge());
                    user.setCity(newUser.getCity());
                    user.setCountry(newUser.getCountry());
                    user.setEmail(newUser.getEmail());
                    user.setGender(newUser.getGender());
                    user.setFirstName(newUser.getFirstName());
                    user.setLastName(newUser.getLastName());
                    user.setDateOfBirth(newUser.getDateOfBirth());
                    user.setStreetName(newUser.getStreetName());
                    user.setStreetNumber(newUser.getStreetNumber());
                    user.setReviews(newUser.getReviews());
                    userMongo.save(user);
                } 
            } 
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteUserById(String id) {
        try {
            userMongo.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteUser(GenericUser user) {
        try {
            userMongo.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Document findYoungerCountriesByUsers(int number) {
        MatchOperation matchOperation = match(new Criteria("_class").is("user"));
        GroupOperation groupOperation = group("$country").avg("$age").as("avgAge");
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.ASC, "avgAge"));
        LimitOperation limitOperation = limit(number);
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("country")
                .and(ArithmeticOperators.Round.roundValueOf("avgAge").place(1)).as("age");
        Aggregation aggregation = newAggregation(matchOperation, groupOperation, sortOperation,
                limitOperation, projectionOperation);
        AggregationResults<User> result = mongoOperations
                .aggregate(aggregation, "users", User.class);
        return result.getRawResults();
    }

    public Document findTopCountriesByUsers(int number) {
        MatchOperation matchOperation = match(new Criteria("_class").is("user"));
        GroupOperation groupOperation = group("$country").count().as("numUsers");
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "numUsers"));
        LimitOperation limitOperation = limit(number);
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("country")
                .andExpression("numUsers").as("users").andExclude("_id");
        Aggregation aggregation = newAggregation(matchOperation, groupOperation, sortOperation,
                limitOperation, projectionOperation);
        AggregationResults<User> result = mongoOperations
                .aggregate(aggregation, "users", User.class);
        return result.getRawResults();
    }

}
*/