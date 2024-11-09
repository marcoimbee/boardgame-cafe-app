package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Component
public class CommentDBMongo {

    @Autowired
    private CommentRepoMongo commentMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public CommentDBMongo() {
    }

    public CommentRepoMongo getCommentMongo() {
        return commentMongo;
    }

    public CommentModelMongo addComment(CommentModelMongo comment) {
        try {
            return commentMongo.save(comment);
        } catch (Exception e) {
            System.out.println("[ERROR] addComment()@CommentDBMongo.java raised an exception: " + e.getMessage());
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

    public List<CommentModelMongo> findRecentCommentsByPostId(String postId, int limit, int skip) {
        List<CommentModelMongo> comments = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("post").is(postId));
            query.with(Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("_id")));
            query.skip(skip).limit(limit);
            comments = mongoOperations.find(query, CommentModelMongo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<CommentModelMongo> findByUsername(String username) {
        List<CommentModelMongo> commentList = new ArrayList<>();
        try {
            commentList = commentMongo.findByUsername(username);
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

    public Optional<CommentModelMongo> findByUsernameAndPostAndTimestamp(String username, String post, Date timestamp) {
        Optional<CommentModelMongo> comment = Optional.empty();
        try {
            comment = commentMongo.findByUsernameAndPostAndTimestamp(username, post, timestamp);
            System.out.println();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return comment;
    }

    public boolean deleteByPost(String postId) {
        try {
            long result = commentMongo.deleteByPost(postId);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteByUsername(String username) {
        try {
            commentMongo.deleteByUsername(username);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}