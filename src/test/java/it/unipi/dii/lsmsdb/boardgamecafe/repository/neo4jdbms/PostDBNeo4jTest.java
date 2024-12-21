package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostDBNeo4jTest
{
    @Autowired
    PostDBNeo4j postDBNeo4j;
    @Autowired
    CommentDBNeo4j commentDBNeo4j;
    @Autowired
    UserDBNeo4j userDBNeo4j;

    static PostModelNeo4j testPost1;
    static final String testIdPost1 = "testIdPost1";
    static final String testIdComment1 = "testIdComment1";
    static final String testUsername1 = "testUsername1";
    static final String testIdUsernamme1 = "testIdUsernamme1";
    static UserModelNeo4j testAuthor;
    static CommentModelNeo4j testComment;
    static List<CommentModelNeo4j> testCommentsList;

    @BeforeAll
    public static void setup()
    {
        testPost1 = new PostModelNeo4j(testIdPost1);
        testAuthor = new UserModelNeo4j(testIdUsernamme1, testUsername1);
        testComment = new CommentModelNeo4j(testIdComment1);
        testComment.setAuthor(testAuthor);
        testCommentsList = new ArrayList<>();
        testPost1.setAuthor(testAuthor);
        testPost1.setComments(testCommentsList);
    }

    @Test @Order(10)
    void shouldAddPostAndReturnTrue()
    {
        var shouldReturnTrue = postDBNeo4j.addPost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test @Order(20)
    void shouldReturnTheSameIdOfInitializedPost()
    {
        var shouldBeNotEmpty = postDBNeo4j.findById(testIdPost1);

        var sholdHaveSameId = shouldBeNotEmpty.get();
        assertEquals(testIdPost1, sholdHaveSameId.getId());
    }

    @Test @Order(30)
    void shouldUpdatePostAndReturnTrue()
    {
        testCommentsList.add(testComment);
        testPost1.setComments(testCommentsList);
        boolean shouldReturnTrue = postDBNeo4j.updatePost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test @Order(200)
    void shouldDeletePostAndAllItsReferencesAndThenReturnTrue()
    {
        var shouldReturnTrue = userDBNeo4j.deleteUserDetach(testAuthor.getUsername());
        assertTrue(shouldReturnTrue);
        shouldReturnTrue = commentDBNeo4j.deleteByPost(testIdPost1);
        assertTrue(shouldReturnTrue);
        shouldReturnTrue = postDBNeo4j.deletePost(testIdPost1);
        assertTrue(shouldReturnTrue);
    }
}