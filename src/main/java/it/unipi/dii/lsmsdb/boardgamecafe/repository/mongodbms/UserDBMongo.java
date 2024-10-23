package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.*;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Component
public class UserDBMongo {


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

    public Optional<Document> showUserAvgAgeByNationality()
    {
        MatchOperation matchOperation = match(new Criteria("_class").is("user"));

        // Calcolo la proiezione dell'età, calcolata come differenza delle date [ birthday - oggi ]
        ProjectionOperation computeAge = Aggregation.project()
                .andExpression("{$dateDiff: {startDate: '$dateOfBirth', endDate: '$$NOW', unit: 'year'}}")
                .as("age")
                .andExpression("nationality").as("nationality");

        GroupOperation groupByCountry = Aggregation.group("nationality") // Raggruppo per Nazionalità e mostro l'età
                .avg("age").as("averageAge");

        ProjectionOperation projectFields = Aggregation.project("averageAge")
                .andExpression("_id").as("nationality")
                .andExpression("averageAge").as("averageAge")
                .and(ArithmeticOperators.Round.roundValueOf("averageAge").place(1)).as("averageAge");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                computeAge,
                groupByCountry,
                projectFields
        );

        AggregationResults<UserModelMongo> results = mongoOperations.aggregate(aggregation, "users", UserModelMongo.class);

        return Optional.ofNullable(results != null ? results.getRawResults() : null);
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

    public Document findActiveUsersByReviews(Date startDate, Date endDate, int limitResults) {

        // Step 1: Filtrare le recensioni pubblicate nell'intervallo temporale specificato
        MatchOperation matchOperation = Aggregation.match(Criteria.where("dateOfReview")
                .gte(startDate)
                .lte(endDate));

        // Step 2: Raggruppare per utente, contare le recensioni e raccogliere le date delle recensioni
        GroupOperation groupOperation = Aggregation.group("username")
                .count().as("reviewCount")
                .push("dateOfReview").as("reviewDates");

        // Step 3: Filtrare solo gli utenti che hanno almeno 2 recensioni
        MatchOperation matchReviewCount = Aggregation.match(Criteria.where("reviewCount").gte(2));

        // Step 4: Ordinare le date
        Document sortReviewDates = new Document("$project",
                new Document("reviewCount", 1)
                        .append("orderedReviewDates", new Document("$sortArray",
                                new Document("input", "$reviewDates").append("sortBy", 1))));

        // Step 5: Calcolare le differenze tra le date (con $map)
        Document calculateDateDifferences = new Document("$project",
                new Document("reviewCount", 1)
                        .append("orderedReviewDates", 1)
                        .append("dateDifferences", new Document("$map", new Document("input", new Document("$range", Arrays.asList(0, new Document("$subtract", Arrays.asList(new Document("$size", "$orderedReviewDates"), 1))))
                                ).append("as", "index")
                                        .append("in", new Document("$dateDiff", new Document("startDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", "$$index")))
                                                .append("endDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", new Document("$add", Arrays.asList("$$index", 1)))))
                                                .append("unit", "day"))))
                        ));

        // Step 6: Proiettare e calcolare la media delle differenze
        Document calculateAverageFrequency = new Document("$project",
                new Document("reviewCount", 1)
                        //.append("orderedReviewDates", 1)
                        //.append("dateDifferences", 1)
                        .append("averageDateDifference", new Document("$avg", "$dateDifferences")));

        // Step 7: Ordinamento risultati
        SortOperation sortOperation = Aggregation
                .sort(Sort.by(Sort.Direction.DESC, "reviewCount")
                .and(Sort.by(Sort.Direction.ASC, "averageDateDifference")));


        // Step 8: Limitazione della quantità di risultati da visualizzare
        LimitOperation limitOperation = Aggregation.limit(limitResults);

        // Step 9: Esecuzione dell'aggregazione
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                matchReviewCount,
                new CustomAggregationOperation(sortReviewDates),  // Inserimento Document manuale per ordinamento
                new CustomAggregationOperation(calculateDateDifferences),  // Inserimento Document manuale per differenze date
                new CustomAggregationOperation(calculateAverageFrequency),  // Inserimento Document manuale per media differenze
                sortOperation,
                limitOperation
        );

        AggregationResults<ReviewModelMongo> results = mongoOperations.
                aggregate(aggregation, "reviews", ReviewModelMongo.class);

        return results.getRawResults();
    }

}