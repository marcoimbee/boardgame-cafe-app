package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.CommentModel;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PostService {

    @Autowired
    PostDBMongo postDBMongo;
    @Autowired
    PostDBNeo4j postDBNeo4j;
    @Autowired
    UserDBNeo4j userDBNeo4j;
    @Autowired
    BoardgameDBNeo4j boardgameDBNeo4j;

    @Transactional
//    @Retryable(
//            retryFor = {DataAccessException.class, TransactionSystemException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 2000)
//    )
    public PostModelMongo insertPost(PostModelMongo postModelMongo)
    {
        try
        {
            String usernameAuthorPost = postModelMongo.getUsername();
            Optional<UserModelNeo4j> authorPostOptional = userDBNeo4j.findByUsername(usernameAuthorPost);
            if (authorPostOptional.isEmpty()) // Check if the user is OK
                throw new RuntimeException("Post not added. No account match was found.");

            PostModelMongo insertedPost = postDBMongo.addPost(postModelMongo);
            if (insertedPost == null)
                throw new RuntimeException("Unable to add post to MongoDB collection.");

            PostModelNeo4j postModelNeo4j = new PostModelNeo4j(insertedPost.getId()); // Creation of post node in neo
            UserModelNeo4j authorPost = authorPostOptional.get();
            if (!postModelMongo.getTag().isEmpty()) // if the game is referred to a boardGame, then it's necessary the creation of the "REFERRED TO" relationship
            {
                Optional<BoardgameModelNeo4j> referredBoardgameOptional = boardgameDBNeo4j.findByBoardgameName(insertedPost.getTag());
                referredBoardgameOptional.ifPresent(referredBoardgames -> postModelNeo4j.setTaggedGame(referredBoardgames));
            }

            if (!postDBNeo4j.addPost(postModelNeo4j)) // The REFERES TO relationship is already added (if exists)
            {
                deletePost(insertedPost);
                throw new RuntimeException("Unable to add post to Neo4j.");
            }
            if (!addPostToUser(postModelNeo4j, authorPost))
            {
                deletePost(insertedPost);
                throw new RuntimeException("Unable to create relationships.");
            }
            return insertedPost;
        }
        catch (Exception ex) {
            System.err.println("[ERROR] insertPost@PostService.java raised an exception: " + ex.getMessage());
            return null;
        }
    }

    private boolean addPostToUser(PostModelNeo4j postModelNeo4j, UserModelNeo4j userModelNeo4j) {
        try {
            userModelNeo4j.addWrittenPost(postModelNeo4j);
            if (!userDBNeo4j.updateUser(userModelNeo4j.getId(), userModelNeo4j)) {
                System.err.println("[ERROR] addPostToUser()@PostService.java: unable to update user with a new post (MongoDB)");
                return false;
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] addPostToUser()@PostService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean deletePost(PostModelMongo postModelMongo) {
        try {
            if (!postDBNeo4j.deletePost(postModelMongo.getId())) {
                throw new RuntimeException("Error in deleting post in Neo4j");
            }
            if (!postDBMongo.deletePost(postModelMongo)) {
                throw new RuntimeException("Error in deleting post in MongoDB");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deletePost()@PostService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public void likeOrDislikePost(String username, String postId) {
        try {
            if (postDBNeo4j.hasUserLikedPost(username, postId)) {
                if (postDBMongo.updateLikeCount(postId, false)) {
                    postDBNeo4j.removeLikePost(username, postId);
                } else {
                    throw new RuntimeException("Unable to update like count in MongoDB");
                }
            } else {
                if (postDBMongo.updateLikeCount(postId, true)) {
                    postDBNeo4j.addLikePost(username, postId, true);
                } else {
                    throw new RuntimeException("Unable to update like count in MongoDB");
                }
            }
        } catch (Exception ex) {
            System.err.println("[ERROR] likeOrDislikePost()@PostService.java raised an exception: " + ex.getMessage());
        }
    }

    public boolean hasLikedPost(String username, String postId) {
        try {
            return postDBNeo4j.hasUserLikedPost(username, postId);
        } catch (Exception ex) {
            System.err.println("[ERROR] hasLikedPost()@PostService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public List<PostModelMongo> suggestPostLikedByFollowedUsers(String currentUser, int limitResults, int skipCounter) {
        List<PostModelNeo4j> postsLikedByFollowedUsers = postDBNeo4j.               // skipCounter needed for incremental post displaying
                getPostsLikedByFollowedUsers(currentUser, limitResults, skipCounter);
        List<PostModelMongo> suggestedPostsMongo = new ArrayList<>();

        for (PostModelNeo4j postsLikedId : postsLikedByFollowedUsers) {
            Optional<PostModelMongo> postMongo = postDBMongo.findById(postsLikedId.getId());
            postMongo.ifPresent(suggestedPostsMongo::add);  // If the suggested Post is found, then it's added to the suggestedMongoUsers list
        }

        return suggestedPostsMongo;
    }

    public List<PostModelMongo> findPostsByFollowedUsers(String currentUser, int limitResults, int skipCounter) {
        List<String> followedUsernames = userDBNeo4j.getFollowedUsernamesWhoCreatedAtLeastOnePost(currentUser, limitResults, skipCounter);
        List<PostModelMongo> postCreateByFollowedUsers = postDBMongo.findPostsCreatedByFollowedUsers(followedUsernames, limitResults, skipCounter);

        return (postCreateByFollowedUsers == null) ? new ArrayList<>() : postCreateByFollowedUsers;
    }

    public List<PostModelMongo> findPostsByTag(String tag, int limitResults, int skipCounter) {
        List<PostModelMongo> postsReferringToTag = postDBMongo.findByTag(tag, limitResults, skipCounter);   // skipCounter needed for incremental post displaying
        List<PostModelMongo> retrievedPostsMongo = new ArrayList<>();
        for (PostModelMongo postReferringToTag : postsReferringToTag) {
            Optional<PostModelMongo> postMongo = postDBMongo.findById(postReferringToTag.getId());
            postMongo.ifPresent(retrievedPostsMongo::add);
        }

        return retrievedPostsMongo;
    }

    @Transactional
    public boolean insertComment(CommentModel comment, PostModelMongo post, UserModelNeo4j user) {
        try {
            if (!addCommentToMongoPost(comment, post)) {    // Adding the comment to the post's comment list
                deleteComment(comment, post);
                throw new RuntimeException("Error while creating relationships in Neo4J related to a new comment insertion.");
            }

            return true;
        } catch (RuntimeException e) {
            System.err.println("[ERROR] insertComment()@CommentService.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean deleteComment(CommentModel comment, PostModelMongo post) {
        try {
            if (!postDBMongo.deleteCommentFromArrayInPost(post, comment)) {
                throw new RuntimeException("Error in deleting comments from array post in MongoDB");
            }

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteComment()@PostService.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    private boolean addCommentToMongoPost(CommentModel comment, PostModelMongo post) {
        post.addComment(comment);           // Adding the comment to the local MongoDB post object

        if (!postDBMongo.addCommentInPostArray(post, comment)) {             // Updating the actual document in MongoDB
            return false;               // Aborting whole operation, this will make insertComment() fail and rollback
        }

        return true;
    }
}