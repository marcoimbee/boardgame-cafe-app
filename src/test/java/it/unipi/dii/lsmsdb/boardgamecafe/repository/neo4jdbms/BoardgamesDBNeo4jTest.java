package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BoardgamesDBNeo4jTest {

    @Autowired
    BoardgameDBNeo4j boardgameDBNeo4j;

    static BoardgameModelNeo4j testBoardgame;
    static final String testIdBoardgame = "testIdBoardgame";
    static final String testBoardgameName = "testBoardgameName";
    static final String testBoardgameImage = "urlImageTest";
    static final String testBoardgameDescription = "descriptionTest";
    static final int testBoardgameYearPublished = 2024;

    @BeforeAll
    public static void setup() {
        testBoardgame = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName,
                                                testBoardgameImage, testBoardgameDescription, testBoardgameYearPublished);
    }

    @Test
    @Order(10)
    public void GIVEN_a_boardgame_WHEN_adding_to_Neo4j_THEN_boardgame_is_added_successfully() {
        var shouldNotBeNull = boardgameDBNeo4j.addBoardgame(testBoardgame);
        assertNotNull(shouldNotBeNull);
    }

    @Test
    @Order(20)
    public void GIVEN_a_boardgame_id_WHEN_finding_by_id_THEN_boardgame_is_found() {
        var shouldBeNotEmpty = boardgameDBNeo4j.findById(testIdBoardgame);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test
    @Order(30)
    public void GIVEN_a_boardgame_name_WHEN_finding_by_name_THEN_boardgame_is_found() {
        var shouldBeNotEmpty = boardgameDBNeo4j.findByBoardgameName(testBoardgameName);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test
    @Order(40)
    public void GIVEN_a_boardgame_WHEN_updating_name_THEN_boardgame_is_updated_successfully() {
        String newName = "NewName";
        testBoardgame.setBoardgameName(newName);
        boardgameDBNeo4j.updateBoardgameNeo4j(testIdBoardgame, testBoardgame);

        var shouldHaveUpdatedYearPublished = boardgameDBNeo4j.findByBoardgameName("NewName").get();
        assertEquals(newName, shouldHaveUpdatedYearPublished.boardgameName);
    }

    @Test
    @Order(50)
    public void GIVEN_a_boardgame_name_WHEN_deleting_THEN_boardgame_is_deleted_successfully() {
        var shouldBeTrue = boardgameDBNeo4j.deleteBoardgameDetach(testBoardgameName);
        assertTrue(shouldBeTrue);
    }

    @Test
    @Order(60)
    public void GIVEN_boardgames_WHEN_finding_recent_THEN_recent_boardgames_returned() {
        assertNotNull(boardgameDBNeo4j.findRecentBoardgames(10, 0));
    }
}
