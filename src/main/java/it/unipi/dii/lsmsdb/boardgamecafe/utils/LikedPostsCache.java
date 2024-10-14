package it.unipi.dii.lsmsdb.boardgamecafe.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class LikedPostsCache {

    private final Map<String, ConcurrentHashMap<String, Boolean>> likedPosts = new ConcurrentHashMap<>(); // Mappa che tiene traccia dei like in base all'utente e al post
    private final ConcurrentHashMap<String, Integer> likeCounts = new ConcurrentHashMap<>(); // Mappa che tiene il conteggio dei like per ogni post

    public void addLike(String username, String postId) { // Aggiungi un like per un utente a un post
        likedPosts
                .computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                .put(postId, true);

        // Incrementa il conteggio dei like per il post
        likeCounts.merge(postId, 1, Integer::sum);
    }

    public void removeLike(String username, String postId) { // Rimuovi un like per un utente da un post
        ConcurrentHashMap<String, Boolean> userLikes = likedPosts.get(username);
        if (userLikes != null) {
            // Rimuovi il like e decrementa il conteggio dei like
            if (userLikes.remove(postId) != null) {
                likeCounts.merge(postId, -1, Integer::sum);
            }
            // Rimuovi l'utente se non ha più like
            if (userLikes.isEmpty()) {
                likedPosts.remove(username);
            }
        }
    }

    public boolean hasLiked(String username, String postId) { // Controlla se un utente ha messo like a un post
        return likedPosts.getOrDefault(username, new ConcurrentHashMap<>()).containsKey(postId);
    }

    public int getLikeCount(String postId) { // Ottieni il conteggio totale dei like per un post
        return likeCounts.getOrDefault(postId, 0);
    }

    public void clearCache() { // Pulisce la cache (se necessario)
        likedPosts.clear();
        likeCounts.clear(); // Pulisce anche i conteggi dei like
    }
}
