package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepoMongo extends MongoRepository<ReviewModelMongo, String> {
    List<ReviewModelMongo> findByUsername(String id);
    void deleteReviewByBoardgameName(String boardgameName);
    void deleteReviewByUsername(String username);
}
