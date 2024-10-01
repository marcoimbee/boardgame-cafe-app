package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PostDBNeo4j {
    @Autowired
    private PostRepoNeo4j postRepoNeo4j;

    @Autowired
    private Neo4jOperations neo4jOperations;

    public PostRepoNeo4j getPostRepoNeo4j() {
        return postRepoNeo4j;
    }

    public boolean addPost(PostModelNeo4j post) {
        try {
            postRepoNeo4j.save(post);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updatePost(PostModelNeo4j updated) {
        try {
            Optional<PostModelNeo4j> old = postRepoNeo4j.findById(updated.getId());
            if (old.isPresent()) {
                PostModelNeo4j oldPost = old.get();
                oldPost.setComments(updated.getComments());
                postRepoNeo4j.save(oldPost);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deletePost(String id) {
        try {
            postRepoNeo4j.deleteAndDetach(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteByReferredBoardgame(String bgName) {
        try {
            postRepoNeo4j.deleteByReferredBoardgame(bgName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteByUsername(String username) {
        try {
            postRepoNeo4j.deleteByUsername(username);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<PostModelNeo4j> findById(String id) {
        Optional<PostModelNeo4j> post = Optional.empty();
        try {
            post = postRepoNeo4j.findById(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return post;
    }
    public Optional<PostModelNeo4j> findFromCommentId(String commentId) {
        Optional<PostModelNeo4j> post = Optional.empty();
        try {
            post = postRepoNeo4j.findFromCommentId(commentId);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return post;
    }

    public List<PostModelNeo4j> findFromReferredBoardgame(String boardgameName) {
        List<PostModelNeo4j> posts = new ArrayList<>();
        try {
            posts = postRepoNeo4j.findFromReferredBoardgame(boardgameName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return posts;
    }
}