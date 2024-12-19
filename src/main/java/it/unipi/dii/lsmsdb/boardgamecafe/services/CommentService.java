package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class CommentService {

    @Autowired
    private CommentDBMongo commentMongo;
    @Autowired
    private CommentDBNeo4j commentNeo4j;
    @Autowired
    private PostDBMongo postMongo;
    @Autowired
    private PostDBNeo4j postNeo4j;
    @Autowired
    private UserDBNeo4j userNeo4j;

    private final static Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Transactional
    public boolean insertComment(CommentModelMongo comment, PostModelMongo post, UserModelNeo4j user) {
        try {
            CommentModelMongo insertedCommentResult = commentMongo.addComment(comment);         // Inserting the comment in MongoDB
            if (insertedCommentResult == null) {
                throw new RuntimeException("Error while inserting the new MongoDB comment.");
            }

            if (!commentNeo4j.addComment(new CommentModelNeo4j(insertedCommentResult.getId()))) {       // Inserting the comment in Neo4J
                commentMongo.deleteComment(insertedCommentResult);
                throw new RuntimeException("Error while inserting the new Neo4J comment (MongoDB comment has been removed).");
            }

            if (!addCommentRelationshipToNeo4jUser(new CommentModelNeo4j(insertedCommentResult.getId()), user)) {       // Creating the needed relationships in Neo4J
                deleteComment(insertedCommentResult, post);
                throw new RuntimeException("Error while creating relationships in Neo4J related to a new comment insertion.");
            }

            if (!addCommentToMongoPostAndNeoPost(insertedCommentResult, post)) {        // Adding the comment to the post's comment list
                deleteComment(insertedCommentResult, post);
                throw new RuntimeException("Error while creating relationships in Neo4J related to a new comment insertion.");
            }
        } catch (RuntimeException e) {
            System.err.println("[ERROR] insertComment@CommentService.java raised an exception: " + e.getMessage());
            return false;
        }

        return true;
    }

    private boolean addCommentRelationshipToNeo4jUser(CommentModelNeo4j comment, UserModelNeo4j user) {
        try {
            user.addWrittenComment(comment);
            if(!userNeo4j.addUser(user)) {
                return false;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] addCommentToUser()@CommentService.java generated an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean addCommentToMongoPostAndNeoPost(CommentModelMongo comment, PostModelMongo post) {

        post.addComment(comment);           // Adding the comment to the local MongoDB post object

        if (!postMongo.addCommentInPostArray(post, comment)) {             // Updating the actual document in MongoDB
            return false;               // Aborting whole operation, this will make insertComment() fail and rollback
        }

        // Getting the Neo4j Optional related to the Post node on which the user has commented
        Optional<PostModelNeo4j> commentedNeo4jPostOptional = postNeo4j.findById(post.getId());
        if (commentedNeo4jPostOptional.isEmpty()) {
            return false;
        }

        PostModelNeo4j commentedNeo4jPost = commentedNeo4jPostOptional.get();  // Obtaining the actual Neo4j post that's being commented
        commentedNeo4jPost.addComment(new CommentModelNeo4j(comment.getId()));    // Adding the comment to the post
        if (!postNeo4j.updatePost(commentedNeo4jPost)) {         // Finally updating the post in Neo4J
            return false;           // Something goes wrong, we can return and insertComment() will take care of rolling back
        }

        return true;
    }

    @Transactional
    public boolean deleteComment(CommentModelMongo comment, PostModelMongo post) {
        try
        {
            // delete all comments
            if (!postMongo.deleteCommentFromArrayInPost(post, comment)) {
                throw new RuntimeException("Error in deleting comments from array post in MongoDB");
            }
            if (!commentNeo4j.deleteAndDetachComment(comment.getId())) {
                throw new RuntimeException("Error in deleting comments of post in Neo4j");
            }
            if (!commentMongo.deleteComment(comment)) {
                throw new RuntimeException("Error in deleting comment from Comment Collection MongoDB");
            }
        }
        catch (Exception ex) {
            System.out.println("Exception deletePost(): " + ex.getMessage());
            return false;
        }
        return true;
    }
}
