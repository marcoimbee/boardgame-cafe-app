package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class UserDBMongo {

    @Autowired
    private UserRepoMongo userRepoMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public UserRepoMongo getUserMongo() {
        return userRepoMongo;
    }

    public boolean addUser(GenericUserModelMongo user) {
        try {
            userRepoMongo.save(user);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] addUser()@UserDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(GenericUserModelMongo user) {
        try {
            userRepoMongo.delete(user);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteUser()@UserDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public Optional<GenericUserModelMongo> findByUsername(String username, boolean includeAdmins) {
        try {
            if (includeAdmins) {
                return userRepoMongo.findByUsername(username, true);
            } else {
                return userRepoMongo.findByUsername(username);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] findByUsername()@UserDBMongo.java raised an exception: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<GenericUserModelMongo> findByEmail(String username) {
        try {
            return userRepoMongo.findByEmail(username);
        } catch (Exception e) {
            System.err.println("[ERROR] findByEmail()@UserDBMongo.java raised an exception: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<UserModelMongo> findAllUsersWithLimit(int limit, int skip, boolean showAlsoBanned) {
        List<UserModelMongo> users = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("_class").ne("admin")); // Exclude documents with _class = admin
            if (! showAlsoBanned)
                query.addCriteria(Criteria.where("banned").is(false));
            query.skip(skip).limit(limit);
            users = mongoOperations.find(query, UserModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findAllUsersWithLimit()@UserDBMongo.java raised an exception: " + e.getMessage());
        }
        return users;
    }

    public boolean updateUser(String id,
                              GenericUserModelMongo newGenericUser,
                              String userType) {
        try {
            Optional<GenericUserModelMongo> genericUser = userRepoMongo.findById(id);

            if (genericUser.isPresent()) {
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
                    user.setEmail(newUser.getEmail());
                    user.setName(newUser.getName());
                    user.setSurname(newUser.getSurname());
                    user.setGender(newUser.getGender());
                    user.setDateOfBirth(newUser.getDateOfBirth());
                    user.setNationality(newUser.getNationality());
                    user.setBanned(newUser.isBanned());
                    user.setSalt(newUser.getSalt());
                    user.setPasswordHashed(newUser.getPasswordHashed());

                    userRepoMongo.save(user);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] updateUser()@UserDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public Optional<Document> showUserAvgAgeByNationality(int limit) {
        MatchOperation matchOperation = match(new Criteria("_class").is("user"));

        ProjectionOperation computeAge = Aggregation.project()      // Projecting age, computed as the difference (birthday - today)
                .andExpression("{$dateDiff: {startDate: '$dateOfBirth', endDate: '$$NOW', unit: 'year'}}")
                .as("age")
                .andExpression("nationality").as("nationality");

        GroupOperation groupByCountry = Aggregation.group("nationality") // Grouping by nationality and maintaining the age
                .avg("age").as("averageAge");

        ProjectionOperation projectFields = Aggregation.project("averageAge")
                .andExpression("_id").as("nationality")
                .andExpression("averageAge").as("averageAge")
                .and(ArithmeticOperators.Round.roundValueOf("averageAge").place(1)).as("averageAge");

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(matchOperation);
        operations.add(computeAge);
        operations.add(groupByCountry);
        if (limit > 0)         // Limit can be -1 (i.e. there is no limit)
            operations.add(Aggregation.limit(limit));
        operations.add(projectFields);

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<UserModelMongo> results = mongoOperations.aggregate(aggregation, "users", UserModelMongo.class);

        return Optional.ofNullable(results != null ? results.getRawResults() : null);
    }

    public Document findCountriesWithMostUsers(int minUserNumber, int limit) {
        // 1) Filtering documents to make sure we're treating users
        MatchOperation matchOperation = match(new Criteria("_class").is("user"));

        // 2) Grouping users country by country and counting how many users we have in each one of them
        GroupOperation groupOperation = group("nationality")
                .count().as("numUsers");  // Counting users

        // 3) Ordering countries based on the number of users (DESC)
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "numUsers"));

        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("nationality")  // Projecting id as nationality
                .andExpression("numUsers").as("numUsers");  // Projecting the number of users

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(matchOperation);
        operations.add(groupOperation);
        operations.add(sortOperation);
        if (limit > 0)
            operations.add(Aggregation.limit(limit));
        operations.add(projectionOperation);

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<UserDBMongo> result = mongoOperations
                .aggregate(aggregation, "users", UserDBMongo.class);

        return result.getRawResults();
    }

    public Document findActiveUsersByReviews(Date startDate, Date endDate, int limitResults) {
        try {
            // 1) Filtering reviews to get those in the specified time interval
            MatchOperation matchOperation = Aggregation.match(Criteria.where("dateOfReview")
                    .gte(startDate)
                    .lte(endDate));

            // 2) Grouping by user, counting the reviews and gathering the dates of the reviews
            GroupOperation groupOperation = Aggregation.group("username")
                    .count().as("reviewCount")
                    .push("dateOfReview").as("reviewDates");

            // 3) Filtering users that published at least 2 reviews
            MatchOperation matchReviewCount = Aggregation.match(Criteria.where("reviewCount").gte(2));

            // 4) Ordering the review dates
            Document sortReviewDates = new Document("$project",
                    new Document("reviewCount", 1)
                            .append("orderedReviewDates", new Document("$sortArray",
                                    new Document("input", "$reviewDates").append("sortBy", 1))));

            // 5) Computing the difference between adjacent dates (using $map)
            Document calculateDateDifferences = new Document("$project",
                    new Document("reviewCount", 1)
                            .append("orderedReviewDates", 1)
                            .append("dateDifferences", new Document("$map", new Document("input", new Document("$range", Arrays.asList(0, new Document("$subtract", Arrays.asList(new Document("$size", "$orderedReviewDates"), 1))))
                                    ).append("as", "index")
                                            .append("in", new Document("$dateDiff", new Document("startDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", "$$index")))
                                                    .append("endDate", new Document("$arrayElemAt", Arrays.asList("$orderedReviewDates", new Document("$add", Arrays.asList("$$index", 1)))))
                                                    .append("unit", "day"))))
                            ));

            // 6) Compute and project the average of the differences
            Document calculateAverageFrequency = new Document("$project",
                    new Document("reviewCount", 1)
                            .append("averageDateDifference", new Document("$avg", "$dateDifferences")));

            // 7) Ordering the results
            SortOperation sortOperation = Aggregation
                    .sort(Sort.by(Sort.Direction.DESC, "reviewCount")
                            .and(Sort.by(Sort.Direction.ASC, "averageDateDifference")));

            // 8) Limiting results
            LimitOperation limitOperation = Aggregation.limit(limitResults);

            Aggregation aggregation = Aggregation.newAggregation(
                    matchOperation,
                    groupOperation,
                    matchReviewCount,
                    new CustomAggregationOperation(sortReviewDates),             // Manual Document insertion for ordering
                    new CustomAggregationOperation(calculateDateDifferences),    // Manual Document insertion for date differences
                    new CustomAggregationOperation(calculateAverageFrequency),   // Manual Document insertion for differences average
                    sortOperation,
                    limitOperation
            );

            AggregationResults<ReviewModelMongo> results = mongoOperations.
                    aggregate(aggregation, "reviews", ReviewModelMongo.class);

            return results.getRawResults();
        } catch (Exception ex) {
            System.err.println("[ERROR] findActiveUsersByReviews()@UserDBMongo raised an exception: " + ex.getMessage());
            return null;
        }
    }

    public List<String> findMostFollowedUsersWithMinAverageLikesCountUsernames(
            List<String> mostFollowedUsersUsernames,
            long minAvgLikeCount,
            int limit
    ) {
        LocalDate pastDate = LocalDate.now().minusDays(2000);  // Consider only posts which have been posted in [today - 60 days, today]
        Date pastDateFullDate = Date.from(pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        MatchOperation matchOperationUsernames = match(Criteria.where("username").in(mostFollowedUsersUsernames));

        MatchOperation matchOperationDate = match(Criteria.where("timestamp").gte(pastDateFullDate));

        GroupOperation groupByUsername = group("username")
                .count().as("postCount")
                .avg("like_count").as("avgLikes");

        MatchOperation matchPostCount = match(Criteria.where("postCount").gte(2));
        MatchOperation matchByMinAvgLikes = match(Criteria.where("avgLikes").gte(minAvgLikeCount));

        SortOperation sortByAvgLikesDesc = sort(Sort.by(Sort.Direction.DESC, "avgLikes"));

        LimitOperation limitOperation = limit(limit);

        ProjectionOperation projectionOperation = project()
                .and("_id").as("username")
                .andExclude("_id");

        Aggregation aggregation = newAggregation(
                matchOperationUsernames,
                matchOperationDate,
                groupByUsername,
                matchPostCount,
                matchByMinAvgLikes,
                sortByAvgLikesDesc,
                limitOperation,
                projectionOperation
        );

        AggregationResults<Document> results = mongoOperations.aggregate(aggregation, "posts", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> doc.getString("username"))
                .collect(Collectors.toList());
    }

    public List<String> getUserUsernames() {
        try {
            return userRepoMongo.findAllUsernames();
        } catch (Exception ex) {
            System.err.println("[ERROR] getUserUsernames()@UserDBMongo.java raised an exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<GenericUserModelMongo> getBannedUsers(int skip, int limit) {
        List<GenericUserModelMongo> bannedUsers = null;
        try {
            bannedUsers = userRepoMongo.getBannedUsers(skip, limit);
        } catch (Exception e) {
            System.err.println("[ERROR] getBannedUsers()@UserDBMongo.java raised an exception: " + e.getMessage());
        }
        return bannedUsers;
    }
}
