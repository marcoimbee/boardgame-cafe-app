package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository //Check if it is needed comment it
public interface ReviewRepoMongo extends MongoRepository<ReviewModelMongo, String> {

    Optional<ReviewModelMongo> findByUsernameAndBoardgameName(String username, String boardgameName);

    List<ReviewModelMongo> findByUsername(String id);

    List<ReviewModelMongo> findByBoardgameName(String id);

    void deleteReviewsByUsername(String username);

    void deleteReviewByBoardgameName(String boardgameName);

    void deleteReviewByUsername(String username);

    //DA ELIMINARE
    //List<ReviewModelMongo> findByTitleContainingOrBodyContainingOrderByDateOfReviewDesc(String word, String word1);
}