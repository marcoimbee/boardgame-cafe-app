package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestPostDBNeo4j
{
    @Autowired
    PostDBNeo4j postDBNeo4j;

    static PostModelNeo4j testPost1;
    static final String testIdPost1 = "testIdPost1";
    static final String testIdComment1 = "testIdComment1";
    static final String testUsername1 = "testUsername1";
    static final String testIdUsernamme1 = "testIdUsernamme1";
    static UserModelNeo4j testAuthor;
    static List<CommentModelNeo4j> testCommentsList;

    @BeforeAll
    public static void setup()
    {
        testPost1 = new PostModelNeo4j(testIdPost1);
        testAuthor = new UserModelNeo4j(testIdUsernamme1, testUsername1);
        testCommentsList = new ArrayList<>();
        testCommentsList.add(new CommentModelNeo4j());
    }

    @Test @Order(10)
    void addPost()
    {
        var shouldReturnTrue = postDBNeo4j.addPost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test @Order(20)
    void updatePost()
    {
        CommentModelNeo4j testComment = new CommentModelNeo4j(testIdComment1);
        testCommentsList.add(testComment);

        testPost1.setComments(testCommentsList);
        postDBNeo4j.updatePost(testPost1);

        // DA CONTROLLARE

        var shouldHaveUpdatedCommentsList = postDBNeo4j.findById(testIdPost1).get();
        assertEquals(testCommentsList, shouldHaveUpdatedCommentsList.getComments());
    }

    @Test
    void findPostsByAuthorName() {
    }

    @Test
    void deleteByReferredBoardgame() {
    }

    @Test
    void deleteByUsername() {
    }

    @Test
    void findById() {
    }

    @Test
    void addLikePost() {
    }

    @Test
    void removeLikePost() {
    }

    @Test
    void hasUserLikedPost() {
    }

    @Test @Order(200)
    void deletePost()
    {
        var shouldReturnTrue = postDBNeo4j.deletePost(testIdPost1);
        assertTrue(shouldReturnTrue);
    }
}