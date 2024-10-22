package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
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


    /*
    //Con Uso di metodi e classi di Aggregazione per stage non supportati da spring-data-mongodb
    public Document findActiveUsersByReviews(Date startDate, Date endDate, int limitResults) {

        // Step 1: Filtrare le recensioni pubblicate nell'intervallo temporale specificato
        MatchOperation matchOperation = match(Criteria.where("reviews.dateOfReview")
                .gte(startDate)
                .lte(endDate));

        // Step 3: Raggruppare per utente, contare le recensioni e raccogliere le date delle recensioni
        GroupOperation groupOperation = group("username")
                .count().as("reviewCount")  // Conta il numero di recensioni per utente
                .push("reviews.dateOfReview").as("reviewDates");  // Colleziona le date delle recensioni

        // Step 4: Ordina le reviewDates
        ProjectionOperation orderReviews = project()
                .andExpression("reviewCount").as("reviewCount")
                .andExpression("reviewDates").applyProjection(
                        ArrayOperators.SortArray.sortBy(Sort.Order.asc("$$this"))
                ).as("orderedReviewDates");

        // Step 5: Calcolare le differenze tra le date consecutive
        ProjectionOperation calculateDateDifferences = project()
                .andExpression("reviewCount").as("reviewCount")
                .andExpression("orderedReviewDates").as("orderedReviewDates")
                .andExpression(
                        ArrayOperators.Map.map()
                                .input(ArrayOperators.Range.range(0,
                                        ArrayOperators.Size.sizeOfArray("$orderedReviewDates")
                                ))
                                .asVar("index")
                                .in(
                                        ArrayOperators.DateDiff.dateDiff(
                                                ArrayOperators.ArrayElemAt.arrayElemAt("$orderedReviewDates", "$$index"),
                                                ArrayOperators.ArrayElemAt.arrayElemAt("$orderedReviewDates",
                                                        Expressions.add("$$index", 1)),
                                                "day"
                                        )
                                )
                ).as("dateDifferences");

        // Step 6: Calcola la media delle differenze tra le date
        ProjectionOperation averageDateDifferences = Aggregation.project()
                .andExpression("reviewCount").as("reviewCount")
                .andExpression("orderedReviewDates").as("orderedReviewDates")
                .andExpression("dateDifferences").as("dateDifferences")
                .andExpression(Aggregation.avg("$dateDifferences")).as("averageDateDifference");


        // Step 7: Calcola la media pesata
        ProjectionOperation weightedAverage = Aggregation.project()
                .andExpression("reviewCount").as("reviewCount")
                .andExpression("orderedReviewDates").as("orderedReviewDates")
                .andExpression("dateDifferences").as("dateDifferences")
                .andExpression("averageDateDifference").as("averageDateDifference")
                .andExpression(
                        Expressions.divide(
                                Expressions.add(
                                        Expressions.multiply("$averageDateDifference", 0.3),  // Peso per il tempo medio
                                        Expressions.multiply("$reviewCount", 0.7)  // Peso per il numero di recensioni
                                ),
                                1
                        )
                ).as("weightedAverage");

        // Step 8: Ordinare per numero di recensioni e tempo medio tra le recensioni (frequenza)
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "reviewCount")
                .and(Sort.by(Sort.Direction.ASC, "averageDateDifference")));  // Più recensioni e minore tempo tra le recensioni

        // Step 9: Limitare i risultati
        LimitOperation limitOperation = limit(limitResults);

        // Step 10: Definire l'aggregazione completa
        Aggregation aggregation = newAggregation(
                matchOperation,
                groupOperation,
                orderReviews,
                calculateDateDifferences,
                averageDateDifferences,
                weightedAverage,
                sortOperation,
                limitOperation
        );

        // Step 11: Eseguire l'aggregazione e restituire i risultati
        AggregationResults<UserModelMongo> result = mongoOperations.aggregate(aggregation, "reviews", UserModelMongo.class);

        // Step 12: Restituisci i risultati grezzi come Document
        return result.getRawResults();
    }
*/
    public Document findActiveUsersByReviews(Date startDate, Date endDate, int limitResults) {

        // Step 1: Match - Filtrare le recensioni nell'intervallo temporale
        Document matchOperation = new Document("$match",
                new Document("reviews.dateOfReview", new Document("$gte", startDate).append("$lte", endDate)).append("reviews.username", "whitepeacock121")
        );

        // Step 2: Unwind - Scomporre il campo delle recensioni
        Document unwindOperation = new Document("$unwind", "$reviews");

        // Step 3: Group - Raggruppare per utente e raccogliere date di recensioni
        Document groupOperation = new Document("$group", new Document("_id", "$username")
                .append("reviewCount", new Document("$sum", 1))
                .append("reviewDates", new Document("$push", "$reviews.dateOfReview"))
        );

        // Step 4: Project - Ordinare le date
        Document sortDates = new Document("$project", new Document("reviewCount", 1)
                .append("orderedReviewDates", new Document("$sortArray", new Document("input", "$reviewDates").append("sortBy", 1)))
        );

        // Step 5: Project - Calcolare differenze di date
        Document calculateDateDifferences = new Document("$project", new Document("reviewCount", 1)
                .append("orderedReviewDates", 1)
                .append("dateDifferences", new Document("$map", new Document("input", new Document("$range", Arrays.asList(0, new Document("$subtract", Arrays.asList(new Document("$size", "$orderedReviewDates"), 1))))
                        ).append("as", "index")
                                .append("in", new Document("$dateDiff", new Document("startDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", "$$index")))
                                        .append("endDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", new Document("$add", Arrays.asList("$$index", 1)))))
                                        .append("unit", "day"))))
                ));

        // Step 6: Calcola la media delle differenze tra le date
        Document averageDateDifferences = new Document("$project", new Document("reviewCount", 1)
                .append("orderedReviewDates", 1)
                .append("dateDifferences", 1)
                .append("averageDateDifference", new Document("$avg", "$dateDifferences"))
        );

        // Step 7: Calcola la media pesata
        Document weightedAverage = new Document("$project", new Document("reviewCount", 1)
                .append("orderedReviewDates", 1)
                .append("dateDifferences", 1)
                .append("averageDateDifference", 1)
                .append("weightedAverage", new Document("$divide", Arrays.asList(
                        new Document("$add", Arrays.asList(
                                new Document("$multiply", Arrays.asList("$averageDateDifference", 0.3)),
                                new Document("$multiply", Arrays.asList("$reviewCount", 0.7))
                        )),
                        1
                )))
        );

        // Step 8: Sort - Ordinare per recensioni e tempo medio tra le recensioni
        Document sortOperation = new Document("$sort", new Document("reviewCount", -1).append("averageDateDifference", 1));

        // Step 9: Limit - Limitare i risultati
        Document limitOperation = new Document("$limit", limitResults);

        // Eseguire l'aggregazione
        List<Document> pipeline = Arrays.asList(
                matchOperation, unwindOperation, groupOperation, sortDates,
                calculateDateDifferences, averageDateDifferences, weightedAverage,
                sortOperation, limitOperation
        );

        AggregateIterable<Document> result = mongoOperations.getCollection("reviews").aggregate(pipeline);

        // Restituisci i risultati grezzi
        return result.first();
    }

    public List<Document> findActiveUsersByReviews2(Date startDate, Date endDate, int limitResults) {

        // Pipeline di aggregazione
        List<Document> pipeline = Arrays.asList(
                new Document("$match",
                        new Document("dateOfReview", new Document("$gte", startDate).append("$lte", endDate))
                ),
                new Document("$group",
                        new Document("_id", "$username")
                                .append("reviewCount", new Document("$sum", 1))
                                .append("reviewDates", new Document("$push", "$dateOfReview"))
                ),
                new Document("$match",
                        new Document("reviewCount", new Document("$gte", 2))
                ),
                new Document("$project",
                        new Document("reviewCount", 1)
                                .append("orderedReviewDates", new Document("$sortArray", new Document("input", "$reviewDates").append("sortBy", 1)))
                ),
                new Document("$project",
                        new Document("reviewCount", 1)
                                .append("orderedReviewDates", 1)
                                .append("dateDifferences", new Document("$map", new Document("input", new Document("$range", Arrays.asList(0, new Document("$subtract", Arrays.asList(new Document("$size", "$orderedReviewDates"), 1))))
                                        ).append("as", "index")
                                                .append("in", new Document("$dateDiff", new Document("startDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", "$$index")))
                                                        .append("endDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", new Document("$add", Arrays.asList("$$index", 1)))))
                                                        .append("unit", "day"))))
                                )),
                new Document("$project",
                        new Document("reviewCount", 1)
                                .append("orderedReviewDates", 1)
                                .append("dateDifferences", 1)
                                .append("averageDateDifference", new Document("$avg", "$dateDifferences"))
                ),
                new Document("$project",
                        new Document("reviewCount", 1)
                                //.append("orderedReviewDates", 1)
                                .append("dateDifferences", 1)
                                .append("averageDateDifference", 1)
                                .append("weightedAverage", new Document("$divide", Arrays.asList(
                                        new Document("$add", Arrays.asList(
                                                new Document("$multiply", Arrays.asList("$averageDateDifference", 0.3)),
                                                new Document("$multiply", Arrays.asList("$reviewCount", 0.7))
                                        )),
                                        1
                                )))
                ),
                new Document("$sort",
                        new Document("reviewCount", -1)
                                .append("averageDateDifference", 1)
                                .append("weightedAverage", -1)
                ),
                new Document("$limit", limitResults)
        );

        AggregateIterable<Document> result = mongoOperations.
                getCollection("reviews").aggregate(pipeline);

        List<Document> resultsDocuments= new ArrayList<>();
        try (MongoCursor<Document> cursor = result.iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                resultsDocuments.add(document);
                //String json = document.toJson(JsonWriterSettings.builder().indent(true).build());
                //resultsAsJson.add(json);
            }
        } catch (IllegalArgumentException e) {
                System.err.println("Errore durante l'estrazione dei dati: " + e.getMessage());
            }

        return resultsDocuments;
    }

    public Document findActiveUsersByReviews3(Date startDate, Date endDate, int limitResults) {

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
        Document calculateAverage = new Document("$project",
                new Document("reviewCount", 1)
                        .append("orderedReviewDates", 1)
                        .append("dateDifferences", 1)
                        .append("averageDateDifference", new Document("$avg", "$dateDifferences")));

        // Step 7: Calcolo della media pesata
        Document calculateWeightedAverage = new Document("$project",
                new Document("reviewCount", 1)
                        //.append("orderedReviewDates", 1)
                        //.append("dateDifferences", 1)
                        .append("averageDateDifference", 1));

        // Step 8: Ordinamento e limitazione dei risultati
        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "reviewCount")
                .and(Sort.by(Sort.Direction.ASC, "averageDateDifference")));
                //.and(Sort.by(Sort.Direction.DESC, "weightedAverage")));

        LimitOperation limitOperation = Aggregation.limit(limitResults);


        // Step 9: Esecuzione dell'aggregazione
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                matchReviewCount,
                new CustomAggregationOperation(sortReviewDates),  // Inserisci Document manuale per ordinamento
                new CustomAggregationOperation(calculateDateDifferences),  // Inserisci Document manuale per differenze date
                new CustomAggregationOperation(calculateAverage),  // Inserisci Document manuale per media differenze
                new CustomAggregationOperation(calculateWeightedAverage),  // Inserisci Document manuale per media pesata
                sortOperation,
                limitOperation
        );

        AggregationResults<ReviewModelMongo> results = mongoOperations.
                aggregate(aggregation, "reviews", ReviewModelMongo.class);

        // Step 10: Convertire i risultati in una lista di Document
        return results.getRawResults();
    }





}