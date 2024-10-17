package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    @Autowired
    CommentDBMongo commentDBMongo;
    @Autowired
    CommentDBNeo4j commentDBNeo4j;

    private final static Logger logger = LoggerFactory.getLogger(PostService.class);

    public boolean insertPost(PostModelMongo postModelMongo, UserModelNeo4j userModelNeo4j, BoardgameModelNeo4j boardgameModelNeo4j) {
        try {
            if (!postDBMongo.addPost(postModelMongo)) {
                logger.error("Error in adding post to collection in MongoDB");
                return false;
            }
            postModelMongo = postDBMongo.findByUsernameAndTimestamp(postModelMongo.getUsername(), postModelMongo.getTimestamp()).get();

            PostModelNeo4j postModelNeo4j = new PostModelNeo4j(postModelMongo.getId());
            if (boardgameModelNeo4j != null) {
                postModelNeo4j.setTaggedGame(boardgameModelNeo4j);
            }
            if (!postDBNeo4j.addPost(postModelNeo4j)) {
                logger.error("Error in adding post to graph in Neo4j");
                if (!postDBMongo.deletePost(postModelMongo)) {
                    logger.error("Error in deleting post from collection in MongoDB");
                }
                return false;
            }

            if (!addPostToUser(postModelNeo4j, userModelNeo4j)) {
                deletePost(postModelMongo);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean addPostToUser(PostModelNeo4j postModelNeo4j, UserModelNeo4j userModelNeo4j) {
        try {
            userModelNeo4j.addWrittenPost(postModelNeo4j);
            if (!userDBNeo4j.updateUser(userModelNeo4j.getId(), userModelNeo4j)) {
                logger.error("Error in connecting post to user in Neo4j");
                return false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deletePost(PostModelMongo postModelMongo) {
        try {
            // delete all comments
            if (!commentDBMongo.deleteByPost(postModelMongo.getId())) {
                logger.error("Error in deleting comments of post in MongoDB");
                return false;
            }
            if (!commentDBNeo4j.deleteByPost(postModelMongo.getId())) {
                logger.error("Error in deleting comments of post in Neo4j");
                return false;
            }

            // delete post
            if (!postDBNeo4j.deletePost(postModelMongo.getId())) {
                logger.error("Error in deleting post in Neo4j");
                return false;
            }
            if (!postDBMongo.deletePost(postModelMongo)) {
                logger.error("Error in deleting post in MongoDB");
                return false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public String getPostId(PostModelMongo postModelMongo) {
        Optional<PostModelMongo> postResult =
                postDBMongo.findByUsernameAndTimestamp(postModelMongo.getUsername(), postModelMongo.getTimestamp());
        if (postResult.isPresent()) {
            return postResult.get().getId();
        }
        else {
            logger.error("Post not found");
        }
        return "";
    }

    public void likeOrDislikePost(String username, String postId) {
        try {
            if (postDBNeo4j.hasUserLikedPost(username, postId)) {
                postDBNeo4j.addLikePost(username, postId);
            } else {
                postDBNeo4j.removeLikePost(username, postId);
            }
        } catch (Exception ex) {
            // Log dell'eccezione
            logger.error("Error liking or disliking post for user " + username + " on post " + postId + ": " + ex.getMessage());
        }
    }

    public boolean hasLikedPost(String username, String postId) {
        try {
            return postDBNeo4j.hasUserLikedPost(username, postId);
        } catch (Exception ex) {
            // Log dell'eccezione
            logger.error("Error checking like status for user " + username + " on post " + postId + ": " + ex.getMessage());
            return false; // Restituisce false in caso di errore
        }
    }

    public Optional<PostModelMongo> showMostLikedPost() {
        try {
            // Ottieni il post con il maggior numero di like da Neo4j tramite la relativa relazione LIKES
            PostModelNeo4j mostLikedPost = postDBNeo4j.findPostWithMostLikes();

            // Se il post con più like esiste, recupera da MongoDB lo stesso post utilizzando il suo ID
            if (mostLikedPost != null) {
                // Usa il metodo findById per ottenere il post dal database Mongo
                return postDBMongo.findById(mostLikedPost.getId());
            } else {
                return Optional.empty(); // Restituisce un Optional vuoto se non ci sono post (meglio del null in questo caso)
            }
        } catch (Exception ex) {
            // Log dell'eccezione
            logger.error("Error fetching most liked post: " + ex.getMessage());
            return Optional.empty(); // Restituisce un Optional vuoto in caso di errore
        }
    }

    /* *
     * Workflow per metodo che ritornerà i post con più like, da mettere
     * in un controller creato ad-hoc per visualizzarlo.
     * */

    /*Repos*/
    //1) Scrivere un metodo 'findMostLikedPost' in postDBneo4j per ottenere il post con più likes
    //2) Sfruttare il metodo 'findByID' in postDBMongo per ottenere il post tramite il relativo id

    /*Services*/
    //Scrivere un service 'showMostLikedPost' che tramite autowired di entrambi i precedenti elementi di repos
    // permette di ottenere da neo4j il post con più likes ed il suo id, così da usarlo per ottenere
    // il relativo post da mongo. (Fondamentale per l'oggetto da visualizzare in grafica
    //                              perchè deve contenere tutte le info del post che stanno in Mongo)

    /*Controller*/
    //Implementare il tutto in una vista ad-hoc per visualizzare il post con più likes, ad esempio
    //  inserendo nella schermata dei posti una sezione "Visualizza il post con più likes" oppure
    // "Visualizza il post più Influente".

    //N.B. Il criterio di scelta del best-post si basa sul numero di likes che un post ha ricevuto.
    // Maggiore sarà il numero di likes, maggiore sarà l'indice di gradimento/influenza di quel post
    // per essere scelto come tale.


}