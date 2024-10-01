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
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
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

    public boolean insertComment(CommentModelMongo comment, PostModelMongo post, UserModelNeo4j user) {
        try {
            if (!commentMongo.addComment(comment)) {
                logger.error("Error in adding comment to collection of comments in MongoDB");
                return false;
            }
            comment = commentMongo.findByUsernameAndPostAndTimestamp(comment.getUsername(), comment.getPost(), comment.getTimestamp()).get();
            if(!commentNeo4j.addComment(new CommentModelNeo4j(comment.getId()))) {
                logger.error("Error in adding comment to collection of comments in Neo4j");
                if (!commentMongo.deleteComment(comment)) {
                    logger.error("Error in deleting comment from collection of comments in MongoDB");
                }
                return false;
            }

            if (!addCommentToUser(new CommentModelNeo4j(comment.getId()), user)) {
                deleteComment(comment, false);
            }
            if (!addCommentToPost(comment, post)) {
                deleteComment(comment, false);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean addCommentToUser(CommentModelNeo4j comment, UserModelNeo4j user) {
        try {
            user.addWrittenComment(comment);
            if(!userNeo4j.updateUser(user.getId(), user)) {
                logger.error("Error in adding comment to user in Neo4j");
                return false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean addCommentToPost(CommentModelMongo comment, PostModelMongo post) {
        try {
            post.addComment(comment);
            if (!postMongo.updatePost(post.getId(), post)) {
                logger.error("Error in adding comment to post in MongoDB");
                return false;
            }
            Optional<PostModelNeo4j> tmp = postNeo4j.findById(post.getId());
            if (tmp.isEmpty()) {
                logger.error("Post not found in Neo4j");
                post.deleteCommentInPost(comment.getId());
                if (!postMongo.updatePost(post.getId(), post)) {
                    logger.error("Error in removing comment from post in MongoDB");
                }
                return false;
            }
            PostModelNeo4j postModelNeo4j = tmp.get();
            postModelNeo4j.addComment(new CommentModelNeo4j(comment.getId()));
            if (!postNeo4j.updatePost(postModelNeo4j)) {
                logger.error("Error in adding comment to post in Neo4j");
                post.deleteCommentInPost(comment.getId());
                if (!postMongo.updatePost(post.getId(), post)) {
                    logger.error("Error in removing comment from post in MongoDB");
                }
                return false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteComment(CommentModelMongo comment, boolean propagate) {
        try {
            if (propagate) {
                Optional<PostModelMongo> tmp = postMongo.findById(comment.getPost());
                if (tmp.isPresent()) {
                    PostModelMongo post = tmp.get();
                    post.deleteCommentInPost(comment.getId());
                    postMongo.updatePost(post.getId(), post);
                }
            }
            commentNeo4j.deleteAndDetachComment(comment.getId());   // Also remove relationships, no need to propagate to UserNeo4j or PostNeo4j
            commentMongo.deleteComment(comment);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public String getCommentId(CommentModelMongo comment) {
        Optional<CommentModelMongo> commentResult =
                commentMongo.findByUsernameAndPostAndTimestamp(comment.getUsername(), comment.getPost(), comment.getTimestamp());
        if (commentResult.isPresent()) {
            return commentResult.get().getId();
        }
        else {
            logger.error("Comment not found");
        }
        return "";
    }
}
