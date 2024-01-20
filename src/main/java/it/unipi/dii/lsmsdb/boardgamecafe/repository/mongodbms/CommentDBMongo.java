package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CommentDBMongo {

    public CommentDBMongo() {
    }

    @Autowired
    private CommentRepoMongo commentMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public CommentRepoMongo getCommentMongo() {
        return commentMongo;
    }

    public boolean addComment(CommentModelMongo comment) {
        try {
            commentMongo.save(comment);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteComment(CommentModelMongo comment) {
        try {
            commentMongo.delete(comment);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<CommentModelMongo> findByPost(String post) {
        List<CommentModelMongo> commentList = new ArrayList<>();
        try {
            commentList = commentMongo.findByPost(post);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return commentList;
    }

    public Optional<CommentModelMongo> findById(String id) {
        Optional<CommentModelMongo> comment = Optional.empty();
        try {
            comment = commentMongo.findById(id);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return comment;
    }
}