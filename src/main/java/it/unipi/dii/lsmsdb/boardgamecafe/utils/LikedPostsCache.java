package it.unipi.dii.lsmsdb.boardgamecafe.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class LikedPostsCache {

    // Mappa che tiene traccia dei like per ogni post messi dall'utente corrente
    //<postId, true/false>
    private final ConcurrentHashMap<String, Boolean> likedPosts = new ConcurrentHashMap<>();
    // Mappa che tiene il conteggio dei like per ogni post
    //<postId, likesNumber>
    private final ConcurrentHashMap<String, Integer> likeCounts = new ConcurrentHashMap<>();

    public void addLike(String postId) { // Aggiungi un like a un post
        likedPosts.put(postId, true);
        likeCounts.merge(postId, 1, Integer::sum);
    }

    public void removeLike(String postId) { // Rimuovi un like da un post
        if (likedPosts.remove(postId) != null) {
            likeCounts.merge(postId, -1, Integer::sum);
        }
    }

    public boolean hasLiked(String postId) { // Controlla se l'utente ha messo like a un post
        return likedPosts.containsKey(postId);
    }

    public int getLikeCount(String postId) { // Ottieni il conteggio totale dei like per un post
        return likeCounts.getOrDefault(postId, 0);
    }

    public void updateLikeCount(String postId, int likeCount) { // Imposta manualmente il conteggio dei like per un post
        likeCounts.put(postId, likeCount);
    }

    public void clearCache() { // Pulisce la cache (se necessario)
        likedPosts.clear();
        likeCounts.clear();
    }
}
