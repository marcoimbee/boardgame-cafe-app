package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostDBNeo4jTest {

    @Autowired
    PostDBNeo4j postDBNeo4j;
    @Autowired
    UserDBNeo4j userDBNeo4j;

    static PostModelNeo4j testPost1;
    static final String testIdPost1 = "testIdPost1";
    static final String testUsername1 = "testUsername1";
    static final String testIdUsernamme1 = "testIdUsernamme1";
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
    public void GIVEN_a_post_WHEN_adding_to_Neo4j_THEN_post_is_added_successfully() {
        boolean shouldReturnTrue = postDBNeo4j.addPost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(20)
    public void GIVEN_a_post_id_WHEN_finding_by_id_THEN_the_found_post_has_the_same_id() {
        Optional<PostModelNeo4j> shouldBeNotEmpty = postDBNeo4j.findById(testIdPost1);

        PostModelNeo4j shouldHaveSameId = shouldBeNotEmpty.get();
        assertEquals(testIdPost1, shouldHaveSameId.getId());
    }

    @Test
    @Order(30)
    void GIVEN_a_post_with_comments_WHEN_updating_the_post_THEN_post_is_updated_successfully() {
        testPost1.setTaggedGame(testBoardgameNeo4j);
        boolean shouldReturnTrue = postDBNeo4j.updatePost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(40)
    void GIVEN_a_post_WHEN_deleting_the_post_and_all_its_references_THEN_deletion_is_successful() {
        boolean shouldReturnTrue = userDBNeo4j.deleteUserDetach(testAuthor.getUsername());
        assertTrue(shouldReturnTrue);
        shouldReturnTrue = postDBNeo4j.deletePost(testIdPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(50)
    public void GIVEN_post_referred_to_boardgame_WHEN_deleting_by_referred_boardgame_THEN_post_is_deleted() {
        assertTrue(postDBNeo4j.deleteByReferredBoardgame(testIdBoardgame));
    }

    @Test
    @Order(60)
    public void GIVEN_post_by_user_WHEN_deleting_by_username_THEN_post_is_deleted() {
        assertTrue(postDBNeo4j.deleteByUsername(testUsername1));
    }
}
