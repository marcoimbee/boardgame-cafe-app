package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class CommentDBNeo4jTest {

    @Autowired
    private CommentDBNeo4j commentDBNeo4j;
    @Autowired
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private UserDBNeo4j userDBNeo4j;
    private CommentModelNeo4j sampleComment;
    private PostModelNeo4j sampleCommentedPost;
    private UserModelNeo4j sampleCommentAuthor;

    @BeforeEach
    public void start() {
        init();
    }

    @AfterEach
    public void clean() {
        commentDBNeo4j.deleteAndDetachComment(sampleComment.getId());
        postDBNeo4j.deletePost(sampleCommentedPost.getId());
        userDBNeo4j.deleteUserDetach(sampleCommentAuthor.getUsername());
    }

    private void init() {
        String sampleCommentId = "test_id_for_test_comment";
        String sampleAuthorId = "test_comment_author_id";
        String sampleAuthorUsername = "test_comment_author_username";
        String sampleCommentedPostId = "test_commented_post_id";

        sampleComment = new CommentModelNeo4j(sampleCommentId);
        sampleCommentAuthor = new UserModelNeo4j(sampleAuthorId, sampleAuthorUsername);
        sampleCommentedPost = new PostModelNeo4j(sampleCommentedPostId);

        sampleComment.setAuthor(sampleCommentAuthor);
        sampleComment.setCommentedPost(sampleCommentedPost);

        commentDBNeo4j.addComment(sampleComment);
    }

    @Test
    public void testAddComment() {
        assertNotNull(commentDBNeo4j.findById(sampleComment.getId()).get());
    }

    @Test
    public void testDeleteAndDetachComment() {
        assertTrue(commentDBNeo4j.deleteAndDetachComment(sampleComment.getId()));
    }

    @Test
    public void testFindById() {
        assertNotNull(commentDBNeo4j.findById(sampleComment.getId()).get());
    }

    @Test
    public void testDeleteByPost() {
        assertTrue(commentDBNeo4j.deleteByPost(sampleCommentedPost.getId()));
    }

    @Test
    public void testDeleteByUsername() {
        assertTrue(commentDBNeo4j.deleteByUsername(sampleCommentAuthor.getUsername()));
    }
}