package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepoMongo extends MongoRepository<GenericUserModelMongo, String> {

    @Query("{'username': {$regex : ?0, $options: 'i'}, '_class': ?1}")
    List<GenericUserModelMongo> findByUsernameRegexAnd_class(String username, String classType);

    Optional<GenericUserModelMongo> findByUsername(String username);

    Optional<GenericUserModelMongo> findByEmail(String email);

    @Aggregation(pipeline = { "{ $project: { username: 1, _id: 0 } }" })
    List<String> findAllUsernames();
}
