package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestBoardgamesDBNeo4j
{
    @Autowired
    BoardgameDBNeo4j boardgameDBNeo4j;
    static BoardgameModelNeo4j testBoardgame;
    static final String testIdBoardgame = "testIdBoardgame";
    static final String testBoardgameName = "testBoardgameName";

    @BeforeAll
    public static void setup()
    {
        testBoardgame = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName, "testThumbnail", 2024);
    }
    @Test @Order(10)
    void addBoardgame()
    {
        var shouldNotBeNull = boardgameDBNeo4j.addBoardgame(testBoardgame);
        assertNotNull(shouldNotBeNull);
    }

    @Test @Order(20)
    void findById()
    {
        var shouldBeNotEmpty = boardgameDBNeo4j.findById(testIdBoardgame);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test @Order(30)
    void findByBoardgameName()
    {
        var shouldBeNotEmpty = boardgameDBNeo4j.findByBoardgameName(testBoardgameName);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test @Order(40)
    void updateBoardgameNeo4j()
    {
        int newYear = 2000;
        testBoardgame.setYearPublished(newYear);
        boardgameDBNeo4j.updateBoardgameNeo4j(testIdBoardgame, testBoardgame);

        var shouldHaveUpdatedYearPublished = boardgameDBNeo4j.findByBoardgameName(testBoardgameName).get();
        assertEquals(newYear, shouldHaveUpdatedYearPublished.yearPublished);
    }

    @Test @Order(100)
    void deleteBoardgameDetach()
    {
        var shouldBeTrue = boardgameDBNeo4j.deleteBoardgameDetach(testBoardgameName);
        assertTrue(shouldBeTrue);
    }
}