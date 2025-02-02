package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.CommentModel;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
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

    @Autowired
    private PostRepoMongo postMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public PostDBMongo() {}

    public PostModelMongo addPost(PostModelMongo post) {
        try {
            return postMongo.save(post);
        } catch (Exception e) {
            System.err.println("[ERROR] addPost()@PostDBMongo.java raised an exception: " + e.getMessage());
            return null;
        }
    }

    public boolean updatePost(String id, PostModelMongo updated) {
        try {
            Optional<PostModelMongo> old = postMongo.findById(id);
            if (old.isEmpty()) {
                System.err.println("[ERROR] The post that's being updated was not found in the DB.");
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

            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] updatePost()@PostDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePost(PostModelMongo post) {
        try {
            postMongo.delete(post);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deletePost()@PostDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean updateLikeCount(String postId, boolean increment) {
        try {
            Query query = new Query(Criteria.where("_id").is(postId));
            Update update = new Update().inc("like_count", (increment) ? 1 : -1);
            UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

            return (result.getMatchedCount() > 0);
        } catch (Exception e) {
            System.err.println("[ERROR] updateLikeCount()@PostDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public Optional<PostModelMongo> findById(String id) {
        Optional<PostModelMongo> post = Optional.empty();
        try {
            post = postMongo.findById(id);
        } catch (Exception e) {
            System.err.println("[ERROR] findById()@PostDBMongo.java raised an exception: " + e.getMessage());
        }
        return post;
    }

    public List<PostModelMongo> findByUsername(String username) {
        List<PostModelMongo> posts = new ArrayList<>();
        try {
            posts = postMongo.findByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] findByUsername()@PostDBMongo.java raised an exception: " + e.getMessage());
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
            System.err.println("[ERROR] findRecentPostsByUsername()@PostDBMongo.java raised an exception: " + e.getMessage());
        }
        return posts;
    }

    public boolean deleteByTag(String bgName) {
        try {
            postMongo.deleteByTag(bgName);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteByTag()@PostDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteByUsername(String username) {
        try {
            postMongo.deleteByUsername(username);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteByUsername()@PostDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public List<PostModelMongo> findByTag(String bgName, int limit, int skip) {
        List<PostModelMongo> posts = new ArrayList<>();
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("tag").is(bgName));
            query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
            query.skip(skip);
            query.limit(limit);
            posts = mongoOperations.find(query, PostModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findByTag()@PostDBMongo.java raised an exception: " + e.getMessage());
        }
        return posts;
    }

    public List<PostModelMongo> findByTag(String bgName) {
        List<PostModelMongo> posts = new ArrayList<>();
        try {
            posts = postMongo.findByTag(bgName);
        }
        catch (Exception e) {
            System.err.println("[ERROR] findByTag()@PostDBMongo.java raised an exception: " + e.getMessage());
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
            System.err.println("[ERROR] deleteByUsername()@PostDBMongo.java raised an exception: " + e.getMessage());
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
                .and(Sort.by(Sort.Direction.ASC, "_id")));  // Ordering by numComments and _id

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

    public boolean deleteCommentFromArrayInPost(PostModelMongo post, CommentModel comment) {
        Query query = new Query(Criteria.where("_id").is(post.getId()));
        Query matchCommentById = new Query(Criteria.where("_id").is(comment.getId()));
        Update update = new Update().pull("comments", matchCommentById);
        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

        return result.getModifiedCount() > 0;     // At least a document got modified, update was successful
    }

    public boolean addCommentInPostArray(PostModelMongo post, CommentModel comment) {
        Query query = new Query(Criteria.where("_id").is(post.getId()));
        Update update = new Update().push("comments",
                new BasicDBObject("$each", Collections.singletonList(comment))
                        .append("$position", 0));
        UpdateResult result = mongoOperations.updateFirst(query, update, PostModelMongo.class);

        return result.getModifiedCount() > 0;       // At least a document got updated, the update is successful
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
            System.err.println("[ERROR] updatePostComment()@PostDBMongo raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public boolean deleteCommentsAfterUserDeletion(String username) {
        try {
            Query query = new Query(Criteria.where("comments.username").is(username));
            Update update = new Update().pull("comments", new Query(Criteria.where("username").is(username)));

            mongoOperations.updateMulti(query, update, "posts");

            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteCommentsAfterUserDeletion@PostDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePostsAfterBoardgameUpdate(String oldBoardgameName, String updatedBoardgameName) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("tag").is(oldBoardgameName));

            Update update = new Update();
            update.set("tag", updatedBoardgameName);

            mongoOperations.updateMulti(query, update, "posts");

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] updatePostsAfterBoardgameUpdate()@PostDBMongo.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public List<PostModelMongo> findPostsCreatedByFollowedUsers(List<String> followedUsernames, int limit, int skip) {
        try {
            Query query = new Query();

            query.addCriteria(Criteria.where("username").in(followedUsernames));
            query.with(Sort.by(Sort.Order.desc("timestamp")));
            query.skip(skip).limit(limit);

            return mongoOperations.find(query, PostModelMongo.class);
        }
        catch (Exception e)
        {
            System.err.println("[ERROR] findPostsCreatedByFollowedUsers()@PostDBMongo.java raised an exception: " + e.getMessage());
            return null;
        }
    }

}
