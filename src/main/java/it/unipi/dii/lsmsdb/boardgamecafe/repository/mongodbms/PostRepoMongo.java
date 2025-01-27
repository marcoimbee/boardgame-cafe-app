package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepoMongo extends MongoRepository<PostModelMongo, String>{
    List<PostModelMongo> findByUsername(String username);

    void deleteByTag(String bgName);

    void deleteByUsername(String username);

    List<PostModelMongo> findByTag(String bgName);
}