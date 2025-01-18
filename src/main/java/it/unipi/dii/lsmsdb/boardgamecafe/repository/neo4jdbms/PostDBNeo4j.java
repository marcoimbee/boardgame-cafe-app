package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.LikedPostsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PostDBNeo4j {

    @Autowired
    private PostRepoNeo4j postRepoNeo4j;
    @Autowired
    private LikedPostsCache likedPostsCache;

    public PostRepoNeo4j getPostRepoNeo4j() {
        return postRepoNeo4j;
    }

    public boolean addPost(PostModelNeo4j post) {
        try {
            postRepoNeo4j.save(post);
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] addPost()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public boolean updatePost(PostModelNeo4j updated) {
        try {
            Optional<PostModelNeo4j> old = postRepoNeo4j.findById(updated.getId());
            if (old.isPresent()) {
                PostModelNeo4j newPost = old.get();
                newPost.setTaggedGame(updated.getTaggedGame());
                postRepoNeo4j.save(newPost);
            }
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] updatePost()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public boolean deletePost(String id) {
        try {
            postRepoNeo4j.deleteAndDetach(id);
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deletePost()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public boolean deleteByReferredBoardgame(String bgName) {
        try {
            postRepoNeo4j.deleteByReferredBoardgame(bgName);
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteByReferredBoardgame()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public boolean deleteByUsername(String username) {
        try {
            postRepoNeo4j.deleteByUsername(username);
            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] deleteByUsername()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public Optional<PostModelNeo4j> findById(String id) {
        Optional<PostModelNeo4j> post = Optional.empty();
        try {
            post = postRepoNeo4j.findById(id);
        } catch (Exception ex) {
            System.err.println("[ERROR] findById()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
        }
        return post;
    }

    public void addLikePost(String username, String postId, boolean likeAction) {
        try {
            likedPostsCache.addInfoLike(postId, true, likeAction);      // AutoIncrement done here
            postRepoNeo4j.addLike(username, postId);            // Creating Neo4j relationship here
        } catch (Exception ex) {
            System.err.println("[ERROR] addLikePost()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
        }
    }

    public void removeLikePost(String username, String postId) {
        try {
            likedPostsCache.addInfoLike(postId, false, true);       // AutoDecrement done here
            postRepoNeo4j.removeLike(username, postId);
        } catch (Exception ex) {
            System.err.println("[ERROR] removeLikePost()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
        }
    }

    public boolean hasUserLikedPost(String username, String postId) {
        try {
            int likeInfo = likedPostsCache.hasLiked(postId);       // First, check the cache

            // If the infoLike is in the cache, then return immediately. Otherwise, it's necessary to ask Neo4j
            if (likeInfo == LikedPostsCache.LIKED) { // Info present in cache. LIKE IS PRESENT.
                return true;
            } else if (likeInfo == LikedPostsCache.NOT_LIKED) { // Info present in cache. LIKE IS NOT PRESENT.
                return false;
            } else {        // Not in cache, check Neo4j DB
                boolean hasLiked = postRepoNeo4j.hasLiked(username, postId);
                likedPostsCache.addInfoLike(postId, hasLiked, false);
                return hasLiked;
            }
        } catch (Exception ex) {
            System.err.println("[ERROR] hasUserLikedPost()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public void setLikeCount(String postId, Integer likeCount) {
        this.likedPostsCache.updateLikeCount(postId, likeCount);
    }

    public int findTotalLikesByPostId(String postId) {
        try {
            int likeCount = likedPostsCache.getLikeCount(postId);       // First, check in the cache
            if (likeCount > 0) {
                return likeCount;
            }

            int totalLikes = postRepoNeo4j.findPostLikesById(postId);       // Not present in cache, check Neo4j DB

            likedPostsCache.updateLikeCount(postId, totalLikes);        // Update the cache with the count obtained from DB

            return totalLikes;
        } catch (Exception ex) {
            System.err.println("[ERROR] findTotalLikesByPostId()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
            return 0;       // Return 0 if anything wrong happens
        }
    }

    public List<PostModelNeo4j> getPostsLikedByFollowedUsers(String username, int limitResults, int skipCounter) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        try {
            posts = postRepoNeo4j.findPostsLikedByFollowedUsers(username, limitResults, skipCounter);
        } catch (Exception ex) {
            System.err.println("[ERROR] getPostsLikedByFollowedUsers()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
        }
        return posts;
    }

    public List<PostModelNeo4j> getPostsByFollowedUsers(String username, int limitResults, int skipCounter) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        try {
            posts = postRepoNeo4j.findPostsCreatedByFollowedUsers(username, limitResults, skipCounter);
        } catch (Exception ex) {
            System.err.println("[ERROR] getPostsByFollowedUsers()@PostDBNeo4j.java raised an exception: " + ex.getMessage());
        }
        return posts;
    }
}
