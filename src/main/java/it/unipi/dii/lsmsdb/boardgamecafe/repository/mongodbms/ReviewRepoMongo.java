package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepoMongo extends MongoRepository<ReviewModelMongo, String> {

    Optional<ReviewModelMongo> findByUsernameAndPhoneName(String username, String phoneName);

    List<ReviewModelMongo> findByUsername(String id);

    List<ReviewModelMongo> findByPhoneName(String id);

    List<ReviewModelMongo> findByTitleContainingOrBodyContainingOrderByDateOfReviewDesc(String word, String word1);

    void deleteReviewsByUsername(String id);

    void deleteReviewByPhoneName(String id);

    void deleteReviewByUsername(String id);

}