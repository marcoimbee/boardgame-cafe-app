package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommentDBNeo4j {

    @Autowired
    CommentRepoNeo4j commentRepoNeo4j;
    @Autowired
    Neo4jOperations neo4jOperations;

    public CommentRepoNeo4j getCommentRepoNeo4j() {
        return commentRepoNeo4j;
    }

    public boolean addComment(CommentModelNeo4j comment) {
        try {
            commentRepoNeo4j.save(comment);
        } catch (Exception e) {
            System.err.println("[ERROR] addComment()@CommentDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean deleteAndDetachComment(String id) {
        try {
            commentRepoNeo4j.deleteAndDetach(id);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteAndDetachComment()@CommentDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    public Optional<CommentModelNeo4j> findById(String id) {
        Optional<CommentModelNeo4j> comment = Optional.empty();
        try {
            comment = commentRepoNeo4j.findById(id);
        } catch (Exception e) {
            System.err.println("[ERROR] findById()@CommentDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return comment;
    }

    public boolean deleteByPost(String postId) {
        try {
            commentRepoNeo4j.deleteByPost(postId);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteByPost()@CommentDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean deleteByUsername(String username) {
        try {
            commentRepoNeo4j.deleteByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteByUsername()@CommentDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }
}
