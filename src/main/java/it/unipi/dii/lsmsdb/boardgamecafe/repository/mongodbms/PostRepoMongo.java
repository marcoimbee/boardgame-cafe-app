package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepoMongo extends MongoRepository<PostModelMongo, String>{
    List<PostModelMongo> findByUsername(String username);

    @Query("{username: $username, timestamp:  $timestamp}")
    Optional<PostModelMongo> findByUsernameAndTimestamp(@Param("username") String username, @Param("timestamp") Date timestamp);

    void deleteByTag(String bgName);


    void deleteByUsername(String username);

    @Query("{tag: $bgName}")
    List<PostModelMongo> findByTag(@Param("tag") String bgName);
}