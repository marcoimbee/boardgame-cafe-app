package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;

import org.bson.Document;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

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

    public Document findCountriesWithMostUsers(int number) {

        // Step 1: Filtro i documenti per garantire che si tratti di utenti
        MatchOperation matchOperation = match(new Criteria("_class").is("user"));

        // Step 2: Raggruppa gli utenti per paese e conta quanti utenti ci sono per ciascun paese
        GroupOperation groupOperation = group("nationality")
                .count().as("numUsers");  // Conta il numero di utenti per paese

        // Step 3: Ordina i paesi in base al numero di utenti in ordine decrescente
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "numUsers"));

        // Step 4: Limita i risultati al numero specificato
        LimitOperation limitOperation = limit(number);

        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("nationality")  // Proietta l'id come paese
                .andExpression("numUsers").as("numUsers");  // Proietta il numero di utenti

        Aggregation aggregation = newAggregation(matchOperation, groupOperation, sortOperation, limitOperation, projectionOperation);

        AggregationResults<UserDBMongo> result = mongoOperations
                .aggregate(aggregation, "users", UserDBMongo.class);

        return result.getRawResults();
    }

    public Document findActiveUsersByReviews(Date startDate, Date endDate, int limtResults) {

        // Step 1: Filtrare le recensioni pubblicate nell'intervallo temporale specificato
        MatchOperation matchOperation = match(Criteria.where("reviews.dateOfReview")
                .gte(startDate)
                .lte(endDate));

        // Step 2: Scomporre il campo delle recensioni
        UnwindOperation unwindOperation = unwind("reviews");

        // Step 3: Raggruppare per utente, contare le recensioni e calcolare il tempo tra le recensioni
        GroupOperation groupOperation = group("username")
                .count().as("reviewCount")  // Conta il numero di recensioni per utente
                .avg("reviews.dateOfReview").as("averageTimeBetweenReviews"); // Media del tempo tra una recensione e l'altra

        // Step 4: Proiettare i risultati includendo l'ID dell'utente, il numero di recensioni e il tempo medio
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("username")
                .andExpression("reviewCount").as("reviewCount")
                .andExclude("_id").and("averageTimeBetweenReviews").as("avgReviewTime");

        // Step 5: Ordinare per numero di recensioni e tempo medio tra le recensioni (frequenza)
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "reviewCount").
                and(Sort.by(Sort.Direction.ASC, "avgReviewTime"))); // Più recensioni e minore tempo tra le recensioni

        LimitOperation limitOperation = limit(limtResults);

        // Step 6: Definire l'aggregazione completa
        Aggregation aggregation = newAggregation(matchOperation, unwindOperation, groupOperation, projectionOperation, sortOperation, limitOperation);

        // Step 7: Eseguire l'aggregazione e restituire i risultati
        AggregationResults<UserModelMongo> result = mongoOperations.aggregate(aggregation, "users", UserModelMongo.class);

        return result.getRawResults();
    }



}