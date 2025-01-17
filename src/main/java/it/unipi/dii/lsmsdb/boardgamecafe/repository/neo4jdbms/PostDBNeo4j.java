package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.LikedPostsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PostDBNeo4j {
    @Autowired
    private PostRepoNeo4j postRepoNeo4j;
    @Autowired
    private Neo4jOperations neo4jOperations;
    @Autowired
    private LikedPostsCache likedPostsCache;

    public PostRepoNeo4j getPostRepoNeo4j() {
        return postRepoNeo4j;
    }

    public boolean addPost(PostModelNeo4j post) {
        try {
            postRepoNeo4j.save(post);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updatePost(PostModelNeo4j updated) {
        try {
            Optional<PostModelNeo4j> old = postRepoNeo4j.findById(updated.getId());
            if (old.isPresent()) {
                PostModelNeo4j newPost = old.get();
                newPost.setTaggedGame(updated.getTaggedGame());
                postRepoNeo4j.save(newPost);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deletePost(String id) {
        try {
            postRepoNeo4j.deleteAndDetach(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteByReferredBoardgame(String bgName) {
        try {
            postRepoNeo4j.deleteByReferredBoardgame(bgName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteByUsername(String username) {
        try {
            postRepoNeo4j.deleteByUsername(username);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<PostModelNeo4j> findById(String id) {
        Optional<PostModelNeo4j> post = Optional.empty();
        try {
            post = postRepoNeo4j.findById(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return post;
    }
    /* fra: Da eliminare? -> 20/12/2024
    public Optional<PostModelNeo4j> findFromCommentId(String commentId) {
        Optional<PostModelNeo4j> post = Optional.empty();
        try {
            post = postRepoNeo4j.findFromCommentId(commentId);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return post;
    }
    */

    /* fra: Da eliminare? -> 20/12/2024

    public List<PostModelNeo4j> findFromReferredBoardgame(String boardgameName) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        try {
            posts = postRepoNeo4j.findFromReferredBoardgame(boardgameName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return posts;
    }
    */

    public void addLikePost(String username, String postId, boolean likeAction)
    {
        try
        {
            likedPostsCache.addInfoLike(postId, true, likeAction); // AutoIncrement inside
            postRepoNeo4j.addLike(username, postId); // Create the relationship on Neo
        } catch (Exception ex) {
            // Log dell'eccezione
            System.err.println("Error adding like from user " + username + " to post " + postId + ": " + ex.getMessage());
        }
    }

    public void removeLikePost(String username, String postId)
    {
        try
        {
            likedPostsCache.addInfoLike(postId, false, true);// AutoDecrement inside
            postRepoNeo4j.removeLike(username, postId);
        } catch (Exception ex) {
            // Log dell'eccezione
            System.err.println("Error removing like from user " + username + " for post " + postId + ": " + ex.getMessage());
        }
    }

    public boolean hasUserLikedPost(String username, String postId) {
        try {
            // Prima controlla la cache
            int like_info = likedPostsCache.hasLiked(postId);
            // If the infoLike is in cache, then returns immediately. Else, it's necessary ask to Neo
            if (like_info == LikedPostsCache.LIKED) // info presente in cache. C'è il LIKE
                return true;
            else if (like_info == LikedPostsCache.NOT_LIKED) // info presente in cache. Non cè il LIKE
                return false;
            else // Se non è in cache, controlla il database Neo4j
            {
                boolean hasLiked = postRepoNeo4j.hasLiked(username, postId);
                likedPostsCache.addInfoLike(postId, hasLiked, false);
                return hasLiked;
            }


        } catch (Exception ex) {
            // Log dell'eccezione
            System.err.println("Error checking like status for user " + username + " on post " + postId + ": " + ex.getMessage());
            return false; // In caso di errore, restituisce false
        }
    }

    public void setLikeCount(String postId, Integer likeCount)
    {
        this.likedPostsCache.updateLikeCount(postId, likeCount);
    }

    public int findTotalLikesByPostID(String postId) {
        try {
            // Prima controlla nella cache
            int likeCount = likedPostsCache.getLikeCount(postId);
            if (likeCount > 0) {
                return likeCount;
            }

            // Se non è presente nella cache, controlla il database Neo4j
            int totalLikes = postRepoNeo4j.findPostLikesById(postId);

            // Aggiorna la cache con il conteggio ottenuto dal DB
            likedPostsCache.updateLikeCount(postId, totalLikes);

            return totalLikes;

        } catch (Exception ex) {
            // Log dell'eccezione
            System.err.println("Error retrieving total likes for post " + postId + ": " + ex.getMessage());
            return 0; // In caso di errore, restituisce 0
        }
    }

    public PostModelNeo4j findPostWithMostLikes() {
        PostModelNeo4j mostLikedPost = null;
        try {
            mostLikedPost = postRepoNeo4j.findMostLikedPost();
        } catch (Exception ex) {
            ex.printStackTrace(); // Gestisce eventuali eccezioni
        }
        return mostLikedPost;
    }


    public List<PostModelNeo4j> getPostsLikedByFollowedUsers(String username, int limitResults, int skipCounter) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        try {
            posts = postRepoNeo4j.findPostsLikedByFollowedUsers(username, limitResults, skipCounter);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return posts;
    }
//
//    public List<PostModelNeo4j> getPostsCommentedByFollowedUsers(String username, int limitResults, int skipCounter) {
//        List<PostModelNeo4j> posts = new ArrayList<>();
//        try {
//            posts = postRepoNeo4j.findPostsCommentedByFollowedUsers(username, limitResults, skipCounter);
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return posts;
//    }

    public List<PostModelNeo4j> getPostsByFollowedUsers(String username, int limitResults, int skipCounter) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        try {
            posts = postRepoNeo4j.findPostsCreatedByFollowedUsers(username, limitResults, skipCounter);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return posts;
    }
}