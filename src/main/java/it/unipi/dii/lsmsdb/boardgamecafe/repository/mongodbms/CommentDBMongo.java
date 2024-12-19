package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class CommentDBMongo {

    @Autowired
    private CommentRepoMongo commentMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public CommentDBMongo() {}

    public CommentRepoMongo getCommentMongo() {
        return commentMongo;
    }

    public CommentModelMongo addComment(CommentModelMongo comment) {
        try {
            return commentMongo.save(comment);
        } catch (Exception e) {
            System.err.println("[ERROR] addComment()@CommentDBMongo.java raised an exception: " + e.getMessage());
            return null;
        }
    }

    public boolean updateComment(String id, CommentModelMongo updated) {
        try {
            Optional<CommentModelMongo> old = commentMongo.findById(id);
            if (old.isPresent()) {
                CommentModelMongo comment = old.get();
                comment.setPost(updated.getPost());
                comment.setText(updated.getText());
                comment.setTimestamp(updated.getTimestamp());
                comment.setUsername(updated.getUsername());
                commentMongo.save(comment);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] updateComment()@CommentDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean deleteComment(CommentModelMongo comment) {
        try {
            commentMongo.delete(comment);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteComment()@CommentDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    public List<CommentModelMongo> findRecentCommentsByPostId(String postId, int limit, int skip) {
        List<CommentModelMongo> comments = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("post").is(postId));
            query.with(Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("_id")));
            query.skip(skip).limit(limit);
            comments = mongoOperations.find(query, CommentModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findRecentCommentsByPostId()@CommentDBMongo.java raised an exception: " + e.getMessage());
        }
        return comments;
    }

    public List<CommentModelMongo> findByUsername(String username) {
        List<CommentModelMongo> commentList = new ArrayList<>();
        try {
            commentList = commentMongo.findByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] findByUsername()@CommentDBMongo.java raised an exception: " + e.getMessage());;
        }
        return commentList;
    }

    public Optional<CommentModelMongo> findById(String id) {
        Optional<CommentModelMongo> comment = Optional.empty();
        try {
            comment = commentMongo.findById(id);
        } catch (Exception e) {
            System.err.println("[ERROR] findById()@CommentDBMongo.java raised an exception: " + e.getMessage());;
        }
        return comment;
    }

    public boolean deleteByPost(String postId) {
        try {
            commentMongo.deleteByPost(postId);
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteByPost()@CommentDBMongo.java raised an exception: " + ex.getMessage());;
            return false;
        }
    }

    public boolean deleteByUsername(String username) {
        try {
            commentMongo.deleteByUsername(username);
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteByUsername()@CommentDBMongo.java raised an exception: " + ex.getMessage());;
            return false;
        }
        return true;
    }
}
