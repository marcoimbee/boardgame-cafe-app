package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import javafx.geometry.Pos;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepoMongo extends MongoRepository<PostModelMongo, String>{
    @Query("{username: $username}")
    List<PostModelMongo> findByUsername(String username);

    Optional<PostModelMongo> findByUsernameAndTimestamp(String username, Date timestamp);

    void deleteByTag(String bgName);

    void deleteByUsername(String username);

    List<PostModelMongo> findByTag(String bgName);
}