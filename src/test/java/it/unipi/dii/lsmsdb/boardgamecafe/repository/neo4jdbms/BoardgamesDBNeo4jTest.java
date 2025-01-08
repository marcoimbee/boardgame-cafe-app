package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BoardgamesDBNeo4jTest
{
    @Autowired
    BoardgameDBNeo4j boardgameDBNeo4j;
    static BoardgameModelNeo4j testBoardgame;
    static final String testIdBoardgame = "testIdBoardgame";
    static final String testBoardgameName = "testBoardgameName";

    @BeforeAll
    public static void setup()
    {
        testBoardgame = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName);
    }
    @Test @Order(10)
    void addBoardgameInNeo4JAndReturnNotNull()
    {
        var shouldNotBeNull = boardgameDBNeo4j.addBoardgame(testBoardgame);
        assertNotNull(shouldNotBeNull);
    }

    @Test @Order(20)
    void findBoardgameByIdAndReturnTrue()
    {
        var shouldBeNotEmpty = boardgameDBNeo4j.findById(testIdBoardgame);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test @Order(30)
    void findBoardgameByNameAndReturnTrue()
    {
        var shouldBeNotEmpty = boardgameDBNeo4j.findByBoardgameName(testBoardgameName);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test @Order(40)
    void shouldUpdateTheBoardgameAndReturnItBack()
    {
        String newName = "NewName";
        testBoardgame.setBoardgameName(newName);
        boardgameDBNeo4j.updateBoardgameNeo4j(testIdBoardgame, testBoardgame);

        var shouldHaveUpdatedYearPublished = boardgameDBNeo4j.findByBoardgameName("NewName").get();
        assertEquals(newName, shouldHaveUpdatedYearPublished.boardgameName);
    }

    @Test @Order(100)
    void deleteBoardgameAndReturnTrue()
    {
        var shouldBeTrue = boardgameDBNeo4j.deleteBoardgameDetach(testBoardgameName);
        assertTrue(shouldBeTrue);
    }
}