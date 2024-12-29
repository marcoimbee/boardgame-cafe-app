//package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;
//
//import com.mongodb.client.result.DeleteResult;
//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface CommentRepoMongo extends MongoRepository<CommentModelMongo, String> {
//    List<CommentModelMongo> findByUsername(String username);
//    long deleteByPost(String post);
//    void deleteByUsername(String username);
//}
