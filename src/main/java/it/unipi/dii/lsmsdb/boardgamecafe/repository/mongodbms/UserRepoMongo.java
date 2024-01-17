package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

//import it.unipi.dii.lsmsdb.phoneworld.model.GenericUser;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepoMongo extends MongoRepository<UserModelMongo, String> {

    @Query("{'username': {$regex : ?0, $options: 'i'}, '_class': ?1}")
    List<UserModelMongo> findByUsernameRegexAnd_class(String username, String classType);

    Optional<UserModelMongo> findByUsername(String username);
}
