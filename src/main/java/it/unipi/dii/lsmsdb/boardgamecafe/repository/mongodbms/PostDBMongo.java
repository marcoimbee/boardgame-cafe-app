package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.bulk.UpdateRequest;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.bulk.UpdateRequest;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
public class PostDBMongo {

    public PostDBMongo() {
    }

    @Autowired
    private PostRepoMongo postMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public PostRepoMongo getPostMongo() {return postMongo;}

    public boolean addPostOld(PostModelMongo post) {
        try {
            postMongo.save(post);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public PostModelMongo addPost(PostModelMongo post)
    {
        try { return postMongo.save(post); }
        catch (Exception e) { System.err.println("[ERROR] addPost@PostDBMongo.java raised an exception: " + e.getMessage()); }
        return null;
    }

    public boolean deleteCommentFromPost(PostModelMongo post, CommentModelMongo comment) {
        Criteria criteria = Criteria.where("_id").is(post.getId());
        Update update = new Update().pull("comments", comment);
        mongoOperations.updateFirst(query(criteria), update, PostModelMongo.class);

        return true;
    }

    public boolean updatePost(String id, PostModelMongo updated)
    {
        /* Questo metodo viene invocato quando l'utente vuole modificare il post, ossia text oppure il Title.
        Mentre per aggiornare il post in merito ai like, devo utilizzare "l'aggiornamento singolo" ossia quello
        che evita l'utilizzo della .save()
        * */
        try {
            Optional<PostModelMongo> old = postMongo.findById(id);
            if (!old.isPresent()) {
                System.out.println("The updated post is not in DB!");
                return false;
            }
            PostModelMongo post = old.get();
            post.setUsername(updated.getUsername());
            post.setTitle(updated.getTitle());
            post.setTag(updated.getTag());
            post.setText(updated.getText());
            post.setTimestamp(updated.getTimestamp());
            post.setComments(updated.getComments());
            post.setLikeCount(updated.getLikeCount());
            postMongo.save(post);
        }
        catch (Exception e)
        {
            System.err.println("Exception update post -> " + e.getMessage());
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

    // Show all Posts with a particular Tag (Boardgame Name) and sort them by the highest number of comments
    public List<PostModelMongo> findMostCommentedTaggedPosts(String tag) {

        MatchOperation matchOperation = match(Criteria.where("tag").is(tag));

        UnwindOperation unwindOperation = unwind("comments");

        GroupOperation groupOperation = group("_id")
                .count().as("numComments")
                .first("title").as("title")
                .first("text").as("text")
                .first("username").as("username")
                .first("tag").as("tag")
                .first("timestamp").as("timestamp");

        ProjectionOperation projectionOperation = project()
                .and("_id").as("id")
                .and("title").as("title")
                .and("text").as("text")
                .and("username").as("username")
                .and("tag").as("tag")
                .and("timestamp").as("timestamp")
                .and("numComments").as("comments");

        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "comments"))
                .and(Sort.by(Sort.Direction.ASC, "username"));

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, unwindOperation, groupOperation, projectionOperation, sortOperation);

        AggregationResults<PostModelMongo> results = mongoOperations.aggregate(aggregation, "posts", PostModelMongo.class);

        return results.getMappedResults();
    }

    // Show the tag of Post that is the most commented. (admin)
    public Optional<String> getMostCommentedTag() {
        ProjectionOperation projectionOperation = Aggregation.project("tag")
                .and(ArrayOperators.Size.lengthOfArray("comments")).as("commentCount");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "commentCount"));

        LimitOperation limitOperation = Aggregation.limit(1);

        ProjectionOperation finalProjection = Aggregation.project("tag");

        Aggregation aggregation = Aggregation.newAggregation(
                projectionOperation,
                sortOperation,
                limitOperation,
                finalProjection
        );

        AggregationResults<Document> result = mongoOperations.aggregate(aggregation, "posts", Document.class);

        Document doc = result.getUniqueMappedResult();
        if (doc != null) {
            return Optional.ofNullable(doc.getString("tag"));
        }

        return Optional.empty();
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

    //Operazioni di Aggiornamento Specifici (granularità fine sui campi del document)
    public boolean deleteCommentFromArrayInPost(PostModelMongo post, CommentModelMongo comment)
    {
        Query query = new Query(Criteria.where("_id").is(post.getId()));
        Query matchCommentById = new Query(Criteria.where("_id").is(comment.getId()));
        Update update = new Update().pull("comments", matchCommentById);
        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

        // Se almeno un documento è stato modificato, l'update è riuscito
        return result.getModifiedCount() > 0;
    }

    public boolean addCommentInPostArray(PostModelMongo post, CommentModelMongo comment)
    {
        Query query = new Query(Criteria.where("_id").is(post.getId()));
        Update update = new Update().push("comments", comment);
        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

        // Se almeno un documento è stato modificato, l'update è riuscito
        return result.getModifiedCount() > 0;
    }
}
