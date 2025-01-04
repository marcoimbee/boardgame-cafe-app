package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.CommentModel;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostDBMongoTest
{
    @Autowired
    PostDBMongo postDBMongo;

//    @Autowired
//    CommentDBMongo commentDBMongo;
    static PostModelMongo post1;
    static PostModelMongo post2;
    static PostModelMongo post3;
    static CommentModel comment1;
    static final String testBoardgameName1 = "TestBoardgame1";
    static final String testBoardgameName2 = "TestBoardgame2";
    static final String testUsername = "testUsername";


    @BeforeAll
    public static void setup()
    {
        post1 = new PostModelMongo("gino", "TestPost1",
                "TextTestPost1", testBoardgameName1, new Date());

        post2 = new PostModelMongo("gino", "TestPost2",
                "TextTestPost2", testBoardgameName2, new Date());

        post3 = new PostModelMongo(testUsername, "TestPost3",
                "TextTestPost3", testBoardgameName2, new Date());
    }

    @AfterAll
    public static void clean(){ }

    @Test @Order(1)
    void testAddPost()
    {
        post1 = postDBMongo.addPost(post1);
        post2 = postDBMongo.addPost(post2);
        post3 = postDBMongo.addPost(post3);
        assertInstanceOf(PostModelMongo.class, post1);
        assertInstanceOf(PostModelMongo.class, post2);
        assertInstanceOf(PostModelMongo.class, post3);
        //String id = new ObjectId().toString();  //67710d9bb4363b367af7c16c
        //System.out.println("id " + id);
    }

    @Test @Order(10)
    void testUpdatePost()
    {
        String updatedText = "Updated test";
        post1.setText(updatedText);
        postDBMongo.updatePost(post1.getId(), post1);

        PostModelMongo shouldReturnUpdatedTextPost = postDBMongo.findById(post1.getId()).get();
        assertEquals(updatedText, shouldReturnUpdatedTextPost.getText());
    }

    @Test @Order(20)
    void testUpdateLikeCount()
    {
        postDBMongo.updateLikeCount(post1.getId(), true);

        PostModelMongo shouldReturnSamePostButWith1Like = postDBMongo.findById(post1.getId()).get();
        assertEquals(1, shouldReturnSamePostButWith1Like.getLikeCount());
    }

    @Test @Order(30)
    void testDeleteByTag()
    {
        postDBMongo.deleteByTag(testBoardgameName2);

        var shouldReturnEmptyOptional = postDBMongo.findById(post2.getId());
        assertTrue(shouldReturnEmptyOptional.isEmpty());
    }

    @Test @Order(40)
    void testDeleteByUsername()
    {
        postDBMongo.deleteByUsername(testUsername);

        var shouldReturnEmptyOptional = postDBMongo.findById(post3.getId());
        assertTrue(shouldReturnEmptyOptional.isEmpty());
    }

    @Test @Order(50)
    void testAddCommentInPostArray()
    {
        // The id must be 24 numbers
        String testCommentID = "000000000000000000000000";
        comment1 = new CommentModel(testCommentID, post1.getId(), testUsername, "test comment text", new Date());
        postDBMongo.addCommentInPostArray(post1, comment1);

        PostModelMongo postShouldHaveOneComment = postDBMongo.findById(post1.getId()).get();
        assertEquals(testCommentID, postShouldHaveOneComment.getComments().get(0).getId());
    }

    @Test @Order(60)
    void testUpdatePostComment()
    {
        String updatedCommentText = "updatedCommentText";
        comment1.setText(updatedCommentText);

        postDBMongo.updatePostComment(post1, comment1);
        CommentModel shouldHaveUpdatedCommentText = postDBMongo.findById(post1.getId()).get().getComments().get(0);
        assertEquals(updatedCommentText, shouldHaveUpdatedCommentText.getText());
    }

    @Test @Order(70)
    void testDeleteCommentFromArrayInPost()
    {
        postDBMongo.deleteCommentFromArrayInPost(post1, comment1);

        PostModelMongo postShouldHaveNoComments = postDBMongo.findById(post1.getId()).get();
        assertEquals(0, postShouldHaveNoComments.getComments().size());
    }


    @Test @Order(200)
    void testDeletePost()
    {
        assertTrue(postDBMongo.deletePost(post1));
    }
}