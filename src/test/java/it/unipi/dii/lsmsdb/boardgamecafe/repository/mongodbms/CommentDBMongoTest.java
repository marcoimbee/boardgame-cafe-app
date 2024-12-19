package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest
class CommentDBMongoTest {

    @Autowired
    private CommentDBMongo commentDBMongo;
    @Autowired
    private PostDBMongo postDBMongo;
    private CommentModelMongo sampleComment;
    private PostModelMongo samplePost;

    @BeforeEach
    public void start() {
        init();
    }

    @AfterEach
    public void clean() {
        commentDBMongo.deleteComment(sampleComment);
        postDBMongo.deletePost(samplePost);
    }

    private void init() {
        String samplePostId = "65a930a56448dd90156b31ff";
        String sampleUsername = "test_user";
        String sampleCommentText = "sample comment text";
        String samplePostTitle = "Test post title";
        String samplePostText = "sample post text";
        String samplePostTag = "sample post tag";
        Date sampleDate = new Date();

        CommentModelMongo comment1 = new CommentModelMongo(
                samplePostId,
                sampleUsername,
                sampleCommentText,
                sampleDate
        );

        PostModelMongo post1 = new PostModelMongo(
                sampleUsername,
                samplePostTitle,
                samplePostText,
                samplePostTag,
                sampleDate
        );

        sampleComment = commentDBMongo.addComment(comment1);
        samplePost = postDBMongo.addPost(post1);
    }

    @Test
    public void testAddComment() {
        CommentModelMongo insertedComment = commentDBMongo.getCommentMongo().findById(sampleComment.getId()).get();
        assertEquals("test_user", insertedComment.getUsername());
    }

    @Test
    public void testUpdateComment() {
        CommentModelMongo updatedComment = sampleComment;
        updatedComment.setText("Updated comment text");
        assertTrue(commentDBMongo.updateComment(sampleComment.getId(), updatedComment));
    }

    @Test
    public void testDeleteComment() {
        assertTrue(commentDBMongo.deleteComment(sampleComment));
    }

    @Test
    public void testFindRecentCommentsByPostId() {
        String testPostId = sampleComment.getPost();
        assertEquals(1, commentDBMongo.findRecentCommentsByPostId(testPostId, 1, 0).size());
    }

    @Test
    public void testFindByUsername() {
        assertNotNull(commentDBMongo.findByUsername(sampleComment.getUsername()));
    }

    @Test
    public void testFindById() {
        String sampleCommentId = sampleComment.getId();
        CommentModelMongo retrievedSampleComment = commentDBMongo.findById(sampleCommentId).get();
        assertEquals(sampleComment.getId(), retrievedSampleComment.getId());
    }

    @Test
    public void testDeleteByPost() {
        String samplePostId = samplePost.getId();
        sampleComment.setPost(samplePostId);
        commentDBMongo.addComment(sampleComment);
        assertTrue(commentDBMongo.deleteByPost(samplePostId));
    }

    @Test
    public void testDeleteByUsername() {
        assertTrue(commentDBMongo.deleteByUsername("test_user"));
    }
}