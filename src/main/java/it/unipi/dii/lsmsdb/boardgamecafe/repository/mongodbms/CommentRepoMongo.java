package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepoMongo extends MongoRepository<CommentModelMongo, String> {
    @Query("{'post': ?0}") //To_Check
    List<CommentModelMongo> findByPost(String postId);
    List<CommentModelMongo> findByUsername(String username);
    Optional<CommentModelMongo> findByUsernameAndPostAndTimestamp(String username, String post, Date timestamp);

    void deleteByPost(String post);
    void deleteByUsername(String username);
}
