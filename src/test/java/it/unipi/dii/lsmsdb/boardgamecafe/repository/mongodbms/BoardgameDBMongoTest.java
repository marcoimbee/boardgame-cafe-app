package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BoardgameDBMongoTest {

    @Autowired
    private BoardgameDBMongo boardgameDBMongo;

    private BoardgameModelMongo testBoardgame; // Memorizza il risultato di init()

    @BeforeEach
    public void start() {
        testBoardgame = init();
    }

    @AfterEach
    public void clean() {
        // Pulizia dei dati creati durante i test
        if (testBoardgame != null) {
            boardgameDBMongo.deleteBoardgame(boardgameDBMongo.findBoardgameByName(testBoardgame.getBoardgameName()).orElse(null));
        }
    }

    private BoardgameModelMongo init() {
        BoardgameModelMongo boardgame = new BoardgameModelMongo();
        boardgame.setBoardgameName("Test Boardgame");
        boardgame.setYearPublished(2023);
        boardgame.setDescription("A test boardgame");
        boardgame.setImage("url_image");
        boardgame.setMinPlayers(2);
        boardgame.setMaxPlayers(6);
        boardgame.setPlayingTime(120);
        boardgame.setMinAge(8);

        List<String> categories = new ArrayList<>();
        List<String> designers = new ArrayList<>();
        List<String> publishers = new ArrayList<>();
        categories.add("Strategic");
        designers.add("designer");
        publishers.add("publisher");

        boardgame.setBoardgameCategory(categories);
        boardgame.setBoardgameDesigner(designers);
        boardgame.setBoardgamePublisher(publishers);

        ReviewModelMongo review = new ReviewModelMongo();
        review.setId(new ObjectId().toString());
        review.setBoardgameName("Test Boardgame");
        review.setUsername("test_user");
        review.setBody("Test review");
        review.setRating(5);
        review.setDateOfReview(new Date());

        boardgame.setReviews(Collections.singletonList(review));
        return boardgame;
    }

    @Test
    @Order(1)
    public void testAddBoardgame() {
        // Salva il boardgame
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        // Assicurati che il boardgame sia stato salvato correttamente
        assertNotNull(savedBoardgame);
        assertNotNull(savedBoardgame.getId());
        assertEquals("Test Boardgame", savedBoardgame.getBoardgameName());
    }

    @Test
    @Order(2)
    public void testFindBoardgameByName() {
        // Salva il boardgame per il test
        boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isPresent());
        assertEquals("Test Boardgame", boardgame.get().getBoardgameName());
    }

    @Test
    @Order(3)
    public void testUpdateBoardgameMongo() {
        // Salva il boardgame per il test
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);
        String boardgameId = savedBoardgame.getId();

        BoardgameModelMongo updatedBoardgame = new BoardgameModelMongo();
        updatedBoardgame.setBoardgameName("Updated Test Boardgame");
        updatedBoardgame.setYearPublished(2025);

        boolean result = boardgameDBMongo.updateBoardgameMongo(boardgameId, updatedBoardgame);
        assertTrue(result);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName("Updated Test Boardgame");
        assertTrue(boardgame.isPresent());
        assertEquals(2025, boardgame.get().getYearPublished());
    }


    @Test
    @Order(4)
    public void testFindRecentBoardgames() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findRecentBoardgames(5, 0);
        assertNotNull(boardgames);
        assertFalse(boardgames.isEmpty());
        assertTrue(boardgames.stream().anyMatch(b -> b.getBoardgameName().equals("Test Boardgame")));
    }

    @Test
    @Order(5)
    public void testFindBoardgamesStartingWith() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findBoardgamesStartingWith("Test", 5, 0);
        assertNotNull(boardgames);
        assertFalse(boardgames.isEmpty());
        assertTrue(boardgames.stream().anyMatch(b -> b.getBoardgameName().equals("Test Boardgame")));
    }

    @Test
    @Order(6)
    public void testFindBoardgameById() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameById(savedBoardgame.getId());
        assertTrue(boardgame.isPresent());
        assertEquals("Test Boardgame", boardgame.get().getBoardgameName());
    }

    @Test
    @Order(7)
    public void testAddReviewInBoardgameArray() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        ReviewModelMongo review = new ReviewModelMongo();
        review.setId(new ObjectId().toString());
        review.setBoardgameName("Test Boardgame");
        review.setUsername("test_user");
        review.setBody("Test review");
        review.setRating(5);
        review.setDateOfReview(new Date());

        boolean result = boardgameDBMongo.addReviewInBoardgameArray(savedBoardgame, review);
        assertTrue(result);

        Optional<BoardgameModelMongo> updatedBoardgame = boardgameDBMongo.findBoardgameById(savedBoardgame.getId());
        assertTrue(updatedBoardgame.isPresent());
        assertFalse(updatedBoardgame.get().getReviews().isEmpty());
        assertEquals("test_user", updatedBoardgame.get().getReviews().get(0).getUsername());
    }

    @Test
    @Order(8)
    public void testGetBoardgameTags() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<String> tags = boardgameDBMongo.getBoardgameTags();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertTrue(tags.contains("Test Boardgame"));
    }

    @Test
    @Order(9)
    public void testGetBoardgamesCategories() {
        // Salva il boardgame per il test
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<String> categories = boardgameDBMongo.getBoardgamesCategories();
        assertNotNull(categories);
        assertTrue(categories.contains("Strategic"));
    }

    @Test
    @Order(10)
    public void testFindBoardgamesByCategory() {
        // Salva il boardgame per il test
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findBoardgamesByCategory("Strategic", 10, 0);
        assertNotNull(boardgames);
        assertFalse(boardgames.isEmpty());
        assertEquals("Test Boardgame", boardgames.get(0).getBoardgameName());
    }

    @Test
    @Order(11)
    public void testDeleteReviewInBoardgameReviewsById() {
        // Salva il boardgame per il test
        boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isPresent());

        String reviewId = boardgame.get().getReviews().get(0).getId();
        boolean result = boardgameDBMongo.deleteReviewInBoardgameReviewsById("Test Boardgame", reviewId);
        assertTrue(result);

        boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isPresent());
        assertEquals(0, boardgame.get().getReviews().size());
    }

    @Test
    @Order(12)
    public void testDeleteBoardgame() {
        // Salva il boardgame per il test
        boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isPresent());
        boardgameDBMongo.deleteBoardgame(boardgame.get());
        boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isEmpty());
    }

}
