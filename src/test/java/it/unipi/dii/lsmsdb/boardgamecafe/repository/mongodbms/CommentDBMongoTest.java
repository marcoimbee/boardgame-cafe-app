package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

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
    public void GIVEN_comment_WHEN_inserted_THEN_gets_inserted() {
        CommentModelMongo insertedComment = commentDBMongo.getCommentMongo().findById(sampleComment.getId()).get();
        assertEquals("test_user", insertedComment.getUsername());
    }

    @Test
    public void GIVEN_updated_comment_WHEN_updated_THEN_gets_updated() {
        CommentModelMongo updatedComment = sampleComment;
        updatedComment.setText("Updated comment text");
        assertTrue(commentDBMongo.updateComment(sampleComment.getId(), updatedComment));
    }

    @Test
    public void GIVEN_comment_to_delete_WHEN_deleted_THEN_gets_deleted() {
        assertTrue(commentDBMongo.deleteComment(sampleComment));
    }

    @Test
    public void GIVEN_post_id_WHEN_search_by_post_id_THEN_recent_comments_are_returned() {
        String testPostId = sampleComment.getPost();
        assertEquals(1, commentDBMongo.findRecentCommentsByPostId(testPostId, 1, 0).size());
    }

    @Test
    public void GIVEN_author_username_WHEN_search_by_author_username_THEN_comments_he_wrote_are_returned() {
        assertNotNull(commentDBMongo.findByUsername(sampleComment.getUsername()));
    }

    @Test
    public void GIVEN_comment_idWHEN_search_by_id_THEN_comment_matching_id_is_returned() {
        String sampleCommentId = sampleComment.getId();
        CommentModelMongo retrievedSampleComment = commentDBMongo.findById(sampleCommentId).get();
        assertEquals(sampleComment.getId(), retrievedSampleComment.getId());
    }

    @Test
    public void GIVEN_post_id_WHEN_delete_by_post_id_THEN_comments_under_that_post_deleted() {
        String samplePostId = samplePost.getId();
        sampleComment.setPost(samplePostId);
        commentDBMongo.addComment(sampleComment);
        assertTrue(commentDBMongo.deleteByPost(samplePostId));
    }

    @Test
    public void GIVEN_user_username_WHEN_delete_by_username_THEN_comments_he_wrote_deleted() {
        assertTrue(commentDBMongo.deleteByUsername("test_user"));
    }
}
