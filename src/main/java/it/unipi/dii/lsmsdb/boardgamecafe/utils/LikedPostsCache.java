package it.unipi.dii.lsmsdb.boardgamecafe.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class LikedPostsCache {

    public final static int LIKED = 1;
    public final static int NOT_LIKED = 0;
    public final static int UNKNOWN = -1;

    // Mappa che tiene traccia dei like per ogni post messi dall'utente corrente
    //<postId, true/false>
    private final HashMap<String, Boolean> likedPosts = new HashMap<>(); // Concurrent ?
    // Mappa che tiene il conteggio dei like per ogni post
    //<postId, likesNumber>
    private final ConcurrentHashMap<String, Integer> likeCounts = new ConcurrentHashMap<>();

    public void addInfoLike(String postId, boolean infoLike, boolean likeAction)
    {
        // Aggiungi un'informazione di un like (Presente o Assente), nella cache
        if ((likeAction) && (this.likedPosts.containsKey(postId)))
            if (infoLike)
                this.incLikeCount(postId);
            else
                this.decLikeCount(postId);
        this.likedPosts.put(postId, infoLike);
    }

    public int hasLiked(String postId)
    { // Controlla se l'utente ha messo like a un post.
        // Ritorna:
            // -1: se l'informazione è assente, e quindi bisogna andare verso Neo
            //  1: se l'utente ha il like al post 'postId'
            //  0: se l'utente non ha il like al post 'postId'
        return (!this.likedPosts.containsKey(postId) ? UNKNOWN : (this.likedPosts.get(postId)) ? LIKED : NOT_LIKED);
    }

    public int getLikeCount(String postId) { // Ottieni il conteggio totale dei like per un post
        return likeCounts.getOrDefault(postId, 0);
    }

    public void updateLikeCount(String postId, int likeCount) { // Imposta manualmente il conteggio dei like per un post
        likeCounts.put(postId, likeCount);
    }

    public void incLikeCount(String postId) { this.likeCounts.merge(postId, 1, Integer::sum); }

    public void decLikeCount(String postId) { this.likeCounts.merge(postId, -1, Integer::sum); }

    public void clearCache() { // Pulisce la cache (se necessario)
        likedPosts.clear();
        likeCounts.clear();
    }
}
