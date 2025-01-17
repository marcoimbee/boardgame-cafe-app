package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
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
class PostDBNeo4jTest {

    @Autowired
    PostDBNeo4j postDBNeo4j;
    @Autowired
    UserDBNeo4j userDBNeo4j;

    //Boardgame infos
    static PostModelNeo4j testPost1;
    static final String testIdPost1 = "testIdPost1";
    static final String testUsername1 = "testUsername1";
    static final String testIdUsernamme1 = "testIdUsernamme1";
    //Boardgame infos
    static final String testIdBoardgame = "testIdBoardgame";
    static final String testBoardgameName = "testBoardgameName";
    static final String testImageBoardgame = "testImageLink";
    static final String testDescriptionBoardgame = "testDescription";
    static final int testYearPublishedBoardgame = 2024;
    static BoardgameModelNeo4j testBoardgameNeo4j;
    static UserModelNeo4j testAuthor;

    @BeforeAll
    public static void setup() {
        testPost1 = new PostModelNeo4j(testIdPost1);
        testAuthor = new UserModelNeo4j(testIdUsernamme1, testUsername1);
        testBoardgameNeo4j = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName,
                                                    testImageBoardgame, testDescriptionBoardgame,
                                                    testYearPublishedBoardgame);
        testPost1.setAuthor(testAuthor);
    }

    @Test
    @Order(10)
    void GIVEN_a_post_WHEN_adding_to_Neo4j_THEN_post_is_added_successfully() {
        var shouldReturnTrue = postDBNeo4j.addPost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(20)
    void GIVEN_a_post_id_WHEN_finding_by_id_THEN_the_found_post_has_the_same_id() {
        var shouldBeNotEmpty = postDBNeo4j.findById(testIdPost1);

        var sholdHaveSameId = shouldBeNotEmpty.get();
        assertEquals(testIdPost1, sholdHaveSameId.getId());
    }

    @Test
    @Order(30)
    void GIVEN_a_post_with_comments_WHEN_updating_the_post_THEN_post_is_updated_successfully() {
        testPost1.setTaggedGame(testBoardgameNeo4j);
        boolean shouldReturnTrue = postDBNeo4j.updatePost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(200)
    void GIVEN_a_post_WHEN_deleting_the_post_and_all_its_references_THEN_deletion_is_successful() {
        var shouldReturnTrue = userDBNeo4j.deleteUserDetach(testAuthor.getUsername());
        assertTrue(shouldReturnTrue);
        shouldReturnTrue = postDBNeo4j.deletePost(testIdPost1);
        assertTrue(shouldReturnTrue);
    }
}