package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class PostDBMongo {
    public PostDBMongo() {
    }

    @Autowired
    private PostRepoMongo postMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public PostRepoMongo getPostMongo() {return postMongo;}

    public boolean addPost(PostModelMongo post) {
        try {
            postMongo.save(post);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updatePost(String id, PostModelMongo updated) {
        try {
            Optional<PostModelMongo> old = postMongo.findById(id);
            if (old.isPresent()) {
                PostModelMongo post = old.get();
                post.setUsername(updated.getUsername());
                post.setTitle(updated.getTitle());
                post.setTag(updated.getTag());
                post.setText(updated.getText());
                post.setTimestamp(updated.getTimestamp());
                post.setComments(updated.getComments());
                postMongo.save(post);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deletePost(PostModelMongo post) {
        try {
            postMongo.delete(post);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<PostModelMongo> findById(String id) {
        Optional<PostModelMongo> post = Optional.empty();
        try {
            post = postMongo.findById(id);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    public List<PostModelMongo> findByUsername(String username) {
        List<PostModelMongo> posts = new ArrayList<>();
        try {
            posts = postMongo.findByUsername(username);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }


    public Optional<PostModelMongo> findByUsernameAndTimestamp(String username, Date timestamp) {
        Optional<PostModelMongo> post = Optional.empty();
        try {
            post = postMongo.findByUsernameAndTimestamp(username, timestamp);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    public boolean deleteByTag(String bgName) {
        try {
            postMongo.deleteByTag(bgName);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteByUsername(String username) {
        try {
            postMongo.deleteByUsername(username);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<PostModelMongo> findByTag(String bgName) {
        List<PostModelMongo> posts = new ArrayList<>();
        try {
            posts = postMongo.findByTag(bgName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<PostModelMongo> findRecentPosts(int limit, int skip) {
        List<PostModelMongo> posts = null;
        try {
            Query query = new Query();
            query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
            query.skip(skip).limit(limit);
            posts = mongoOperations.find(query, PostModelMongo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    public Document findTopPostsByBoardgameName(String boardgameName, int limit) {
        // Step 1: Filtra i post che contengono il tag specificato
        MatchOperation matchTagOperation = match(new Criteria("tag").is(boardgameName));

        // Step 2: Scomponi il campo dei commenti per calcolare il numero di commenti
        UnwindOperation unwindComments = unwind("comments");

        // Step 3: Raggruppa per titolo del post per calcolare il numero di commenti per ciascun post
        GroupOperation groupOperation = group("title") // Raggruppa per titolo del post
                .count().as("numComments")   // Conta il numero di commenti per ogni post
                .first("tag").as("tag")  // Prendi il tag (gioco) associato al post
                .first("text").as("content");  // Prendi il contenuto del post

        // Step 4: Proietta i risultati per ottenere il conteggio dei commenti e il contenuto del post
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("title")  // Titolo del post
                .andExpression("content").as("content")  // Contenuto del post
                .andExpression("numComments").as("comments")  // Numero di commenti
                .andExclude("_id").andExpression("tag").as("tag");  // Tag (gioco) associato al post

        // Step 5: Ordina i risultati per numero di commenti in ordine decrescente
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "comments"));

        // Step 6: Limita i risultati al numero specificato (es. top N post)
        LimitOperation limitOperation = limit(limit);

        // Step 7: Definisci l'aggregazione completa
        Aggregation aggregation = newAggregation(matchTagOperation, unwindComments, groupOperation, projectionOperation, sortOperation, limitOperation);

        // Step 8: Esegui l'aggregazione sulla collezione di post
        AggregationResults<PostModelMongo> result = mongoOperations.aggregate(aggregation, "posts", PostModelMongo.class);

        // Step 9: Restituisci i risultati grezzi
        return result.getRawResults();
    }



}
