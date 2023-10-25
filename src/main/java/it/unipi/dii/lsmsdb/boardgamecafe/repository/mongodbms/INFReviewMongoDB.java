package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.phoneworld.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface INFReviewMongoDB extends MongoRepository<Review, String> {

    Optional<Review> findByUsernameAndPhoneName(String username, String phoneName);
    List<Review> findByUsername(String id);
    List<Review> findByPhoneName(String id);
    List<Review> findByTitleContainingOrBodyContainingOrderByDateOfReviewDesc(String word, String word1);
    void deleteReviewsByUsername(String id);
    void deleteReviewByPhoneName(String id);
    void deleteReviewByUsername(String id);


}
