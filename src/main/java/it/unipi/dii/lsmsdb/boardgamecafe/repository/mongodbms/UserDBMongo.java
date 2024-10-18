package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.mongodb.client.model.Aggregates.group;

@Component
public class UserDBMongo {

    public UserDBMongo() {}

    //private final static Logger logger = (Logger) LoggerFactory.getLogger(BoardgameModelMongo.class);

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

    public boolean updateUser(String id,
                              GenericUserModelMongo newGenericUser,
                              String userType) {
        boolean result = true;
        try {
            Optional<GenericUserModelMongo> genericUser = userRepoMongo.findById(id);

            if (genericUser.isPresent())
            {
                if (userType.equals("admin")) {
                    AdminModelMongo administrator = (AdminModelMongo) genericUser.get();
                    administrator.setUsername(newGenericUser.getUsername());
                    administrator.setEmail(newGenericUser.getEmail());
                    administrator.setPasswordHashed(newGenericUser.getPasswordHashed());
                    administrator.setSalt(newGenericUser.getSalt());

                    this.addUser(administrator);
                } else {
                    UserModelMongo user = (UserModelMongo) genericUser.get();
                    UserModelMongo newUser = (UserModelMongo) newGenericUser;

                    user.setUsername(newUser.getUsername());
                    user.setPasswordHashed(newUser.getPasswordHashed());
                    user.setSalt(newUser.getSalt());
                    user.setEmail(newUser.getEmail());
                    user.setName(newUser.getName());
                    user.setSurname(newUser.getSurname());
                    user.setGender(newUser.getGender());
                    user.setDateOfBirth(newUser.getDateOfBirth());
                    user.setNationality(newUser.getNationality());
                    user.setBanned(newUser.isBanned());
                    user.setReviews(newUser.getReviews());

                    userRepoMongo.save(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    // Show average age of users per country

    public List<List> showUserAvgAgeByNationality() // Come deve essere il tipo di ritorno? Mi aspetto che sia una lista di "Nationality, AvgAge"
    {
        // Calcolo la proiezione dell'età, calcolata come differenza delle date [ birthday - oggi ]
        ProjectionOperation computeAge = Aggregation.project()
                .andExpression("{$dateDiff: {birthDate: '$dateOfBirth', today: '$$NOW', unit: 'year'}}")
                .as("age");

        GroupOperation groupByCountry = Aggregation.group("nationality") // Raggruppo per Nazionalità e mostro l'età
                .avg("age").as("averageAge");

        ProjectionOperation projectFields = Aggregation.project("averageAge")
                .and("$_id").as("nationality");

        Aggregation aggregation = Aggregation.newAggregation(
                computeAge,
                groupByCountry,
                projectFields
        );

        AggregationResults<List> results = mongoOperations.aggregate(
                aggregation, "users", List.class);

        return results.getMappedResults();

    }

    // Show the countries from which the highest number of users comes from
    public List<String> getCountriesWithHighestUsersCount(int howMany) {
        GroupOperation groupByNationality = Aggregation.group("nationality").count().as("userCount");
        SortOperation sortByUserCountDesc = sort(Sort.Direction.DESC, "userCount");
        LimitOperation limitResults = limit(howMany);
        ProjectionOperation projectNationalityOnly = project("nationality");

        Aggregation aggregation = newAggregation(groupByNationality, sortByUserCountDesc, limitResults, projectNationalityOnly);

        AggregationResults<String> results = mongoOperations.aggregate(aggregation, "users", String.class);

        return results.getMappedResults();
    }
}