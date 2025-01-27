package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepoMongo extends MongoRepository<GenericUserModelMongo, String> {
    @Query("{ 'username': ?0, '_class': { $ne: 'admin' } }")
    Optional<GenericUserModelMongo> findByUsername(String username);

    Optional<GenericUserModelMongo> findByUsername(String username, boolean includeAdmins);

    Optional<GenericUserModelMongo> findByEmail(String email);

    @Aggregation(pipeline = {
            "{ $match: { _class: { $ne: 'admin' } } }",
            "{ $project: { username: 1, _id: 0 } }"
    })
    List<String> findAllUsernames();

    @Aggregation(pipeline = {
            "{ $match: { 'banned': true } }",
            "{ $skip: ?0 }",
            "{ $limit: ?1 }"
    })
    List<GenericUserModelMongo> getBannedUsers(int skip, int limit);
}
