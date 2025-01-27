package it.unipi.dii.lsmsdb.boardgamecafe.utils;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LikedPostsCache {

    public final static int LIKED = 1;
    public final static int NOT_LIKED = 0;
    public final static int UNKNOWN = -1;

    /*
        Map that keeps track of the likes of the current user for each post
        <postId, True/False>
     */
    private final HashMap<String, Boolean> likedPosts = new HashMap<>(); // Concurrent ?

    /*
        Map that keeps the like count for each post
        <postId, likeCount>
     */
    private final ConcurrentHashMap<String, Integer> likeCounts = new ConcurrentHashMap<>();

    /*
        Adds a like information (present or absent) to the cache
     */
    public void addInfoLike(String postId, boolean infoLike, boolean likeAction) {
        if ((likeAction) && (this.likedPosts.containsKey(postId)))
            if (infoLike)
                this.incLikeCount(postId);
            else
                this.decLikeCount(postId);
        this.likedPosts.put(postId, infoLike);
    }

    /*
        Checks if a user has liked a post.
        Returns:
            -> -1: information is absent, so we must read into Neo4J
            -> 1:  user has liked postId post
            -> 0:  user has not liked postId post
     */
    public int hasLiked(String postId) {
        return (!this.likedPosts.containsKey(postId) ? UNKNOWN : (this.likedPosts.get(postId)) ? LIKED : NOT_LIKED);
    }

    /*
        Obtains the total like count for a given post
     */
    public int getLikeCount(String postId) {
        return likeCounts.getOrDefault(postId, 0);
    }

    /*
        Manually sets the like count for a given post
     */
    public void updateLikeCount(String postId, int likeCount) {
        likeCounts.put(postId, likeCount);
    }

    public void incLikeCount(String postId) { this.likeCounts.merge(postId, 1, Integer::sum); }

    public void decLikeCount(String postId) { this.likeCounts.merge(postId, -1, Integer::sum); }
}
