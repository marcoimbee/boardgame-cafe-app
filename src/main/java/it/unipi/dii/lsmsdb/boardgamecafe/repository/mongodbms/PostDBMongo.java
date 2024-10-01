package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

}
