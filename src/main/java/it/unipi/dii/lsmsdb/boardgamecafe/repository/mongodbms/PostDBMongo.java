package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.CommentModel;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.UserContentUpdateReason;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Component;

import java.util.*;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


@Component
public class PostDBMongo {

    public PostDBMongo() {}

    @Autowired
    private PostRepoMongo postMongo;
    @Autowired
    private MongoOperations mongoOperations;

    /* fra: Da eliminare? -> 19/12/2024
    public PostRepoMongo getPostMongo() {return postMongo;}
     */

    /* fra: Da eliminare? -> 19/12/2024
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
    */

    public PostModelMongo addPost(PostModelMongo post)
    {
        try { return postMongo.save(post); }
        catch (Exception e) { System.err.println("[ERROR] addPost@PostDBMongo.java raised an exception: " + e.getMessage()); }
        return null;
    }

    public boolean updatePost(String id, PostModelMongo updated)
    {
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

    public boolean updateLikeCount(String postId, boolean increment)
    {
        try
        {
            Query query = new Query(Criteria.where("_id").is(postId));
            Update update = new Update().inc("like_count", ( (increment) ? 1 : -1 ));
            UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);
            return (result.getMatchedCount() > 0);
        }
        catch (Exception e)
        {
            System.out.println("Exception updateLikeCount(): " + e.getMessage());
            return false;
        }
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

    public List<PostModelMongo> findRecentPostsByUsername(String username, int limit, int skip) {
        List<PostModelMongo> posts = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
            query.skip(skip).limit(limit);
            posts = mongoOperations.find(query, PostModelMongo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
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

    public List<PostModelMongo> findByTag(String bgName, int limit, int skip) {
        List<PostModelMongo> posts = new ArrayList<>();
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("tag").is(bgName));
            query.skip(skip);
            query.limit(limit);
            posts = mongoOperations.find(query, PostModelMongo.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
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

    public List<PostModelMongo> findTopCommentedTaggedPosts(String tag, int limit, int skip) {
        MatchOperation matchOperation = match(Criteria.where("tag").is(tag));

        ProjectionOperation projectionOperation = project()
                .and("_id").as("id")
                .and("title").as("title")
                .and("username").as("username")
                .and("timestamp").as("timestamp")
                .and("tag").as("tag")
                .and("like_count").as("like_count")
                .and("comments").as("comments")
                .and(ArrayOperators.Size.lengthOfArray("comments")).as("numComments");

        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "numComments")
                .and(Sort.by(Sort.Direction.ASC, "_id")));  // Ordinamento per numComments e _id

        SkipOperation skipOperation = skip(skip);

        LimitOperation limitOperation = limit(limit);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectionOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );
        AggregationResults<PostModelMongo> results = mongoOperations.aggregate(aggregation, "posts", PostModelMongo.class);

        if (results == null || results.getMappedResults() == null) {
            return new ArrayList<>();
        }
        return results.getMappedResults();
    }

    public Document findMostPostedAndCommentedTags(int limitResults) {
        MatchOperation matchOperation = match(new Criteria("tag").exists(true));

        GroupOperation groupOperation = group("tag")
                .count().as("postCount")
                .sum(new AggregationExpression() {
                    @Override
                    public @NotNull Document toDocument(@NotNull AggregationOperationContext context) {
                        return new Document("$size", "$comments");
                    }
                }).as("commentCount");

        ProjectionOperation projectionOperation = project()
                .and("_id").as("tag")
                .and("postCount").as("postCount")
                .and("commentCount").as("commentCount")
                .andExclude("_id");

        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "postCount", "commentCount"));

        LimitOperation limitOperation = limit(limitResults);

        Aggregation aggregation = newAggregation(
                matchOperation,
                groupOperation,
                projectionOperation,
                sortOperation,
                limitOperation
        );

        AggregationResults<Document> results = mongoOperations.aggregate(aggregation, "posts", Document.class);

        return results.getRawResults();
    }

    /* fra: Da eliminare? -> 19/12/2024
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
     */

    //Operazioni di Aggiornamento Specifici (granularità fine sui campi del document)
    public boolean deleteCommentFromArrayInPost(PostModelMongo post, CommentModel comment)
    {
        Query query = new Query(Criteria.where("_id").is(post.getId()));
        Query matchCommentById = new Query(Criteria.where("_id").is(comment.getId()));
        Update update = new Update().pull("comments", matchCommentById);
        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

        // Se almeno un documento è stato modificato, l'update è riuscito
        return result.getModifiedCount() > 0;
    }

    //ToDo: aggiornare implementazione del sottostante metodo per eliminare i commenti dai post
    // fatti da un utente che si è eliminato dall'applicazione (DeleteAccountOp)
//    public boolean deleteCommentFromArrayInPostAfterAccountDeletion(PostModelMongo post, CommentModel comment)
//    {
//        Query query = new Query(Criteria.where("_id").is(post.getId()));
//        Query matchCommentById = new Query(Criteria.where("_id").is(comment.getId()));
//        Update update = new Update().pull("comments", matchCommentById);
//        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);
//
//        // Se almeno un documento è stato modificato, l'update è riuscito
//        return result.getModifiedCount() > 0;
//    }

    public boolean addCommentInPostArray(PostModelMongo post, CommentModel comment)
    {
        Query query = new Query(Criteria.where("_id").is(post.getId()));
        Update update = new Update().push("comments",
                new BasicDBObject("$each", Collections.singletonList(comment))
                        .append("$position", 0));
        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

        // Se almeno un documento è stato modificato, l'update è riuscito
        return result.getModifiedCount() > 0;
    }

    public boolean updatePostComment(PostModelMongo post, CommentModel updatedComment) {
        try {
            ObjectId updatedCommentObjectId = new ObjectId(updatedComment.getId());
            Query query = Query.query(Criteria.where("comments._id").is(updatedCommentObjectId));

            Update update = new Update();
            update.set("comments.$.text", updatedComment.getText());

            mongoOperations.updateFirst(
                    query,
                    update,
                    PostDBMongo.class,
                    "posts"
            );

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] updatePostComment@PostDBMongo raised an exception: " + ex.getMessage());
            return false;
        }
    }


    public boolean updatePostCommentsAfterAdminAction(String username, UserContentUpdateReason updateReason, List<CommentModel> userComments) {
        try {
            if (updateReason == UserContentUpdateReason.BANNED_USER || updateReason == UserContentUpdateReason.DELETED_USER) {
                Query query = Query.query(Criteria.where("comments.username").is(username));

                Update update = new Update();
                if (updateReason == UserContentUpdateReason.DELETED_USER) {
                    update.set("comments.$.username", "[Deleted user]");
                } else {
                    update.set("comments.$.username", "[Banned user]")
                            .set("comments.$.text", "[Banned user]");
                }

                mongoOperations.updateMulti(
                        query,
                        update,
                        PostDBMongo.class,
                        "posts"
                );
            }

            if (updateReason == UserContentUpdateReason.UNBANNED_USER) {
                for (CommentModel comment : userComments) {
                    ObjectId commentObjectId = new ObjectId(comment.getId());
                    Query query = Query.query(Criteria.where("comments._id").is(commentObjectId));

                    Update update = new Update();
                    update.set("comments.$.username", comment.getUsername())
                            .set("comments.$.text", comment.getText());

                    mongoOperations.updateFirst(
                            query,
                            update,
                            PostDBMongo.class,
                            "posts"
                    );
                }
            }

            return true;
        } catch(Exception ex) {
            System.err.println("[ERROR] exception: " + ex.getMessage());
            return false;
        }
    }

    public List<CommentModel> getRecentCommentsByPostId(String postId, int limit, int skip)
    {
        try
        {
            Query query = new Query(Criteria.where("comments.post").is(postId));
            query.fields().include("comments").exclude("_id");
            query.with(Sort.by(Sort.Direction.DESC, "comments.timestamp"));
            query.limit(limit);
            query.skip(skip);
            PostModelMongo post = mongoOperations.findOne(query, PostModelMongo.class);
            if (post != null)
                return post.getComments();
            else
                throw new Exception();
        } catch (Exception e)
        {
            System.out.println("Exception getRecentCommentsByPostId() -> " + e.getMessage());
            return null;
        }
    }

    /* fra: Da eliminare? -> 19/12/2024
    public boolean deleteCommentFromPost(PostModelMongo post, CommentModelMongo comment) {
        Criteria criteria = Criteria.where("_id").is(post.getId());
        Update update = new Update().pull("comments", comment);
        mongoOperations.updateFirst(query(criteria), update, PostModelMongo.class);

        return true;
    }
    */
}
