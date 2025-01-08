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
    public void GIVEN_valid_boardgame_WHEN_add_boardgame_THEN_boardgame_is_added_successfully() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        assertNotNull(savedBoardgame);
        assertNotNull(savedBoardgame.getId());
        assertEquals("Test Boardgame", savedBoardgame.getBoardgameName());
    }

    @Test
    @Order(2)
    public void GIVEN_existing_boardgame_WHEN_find_by_name_THEN_correct_boardgame_is_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isPresent());
        assertEquals("Test Boardgame", boardgame.get().getBoardgameName());
    }

    @Test
    @Order(3)
    public void GIVEN_existing_boardgame_WHEN_update_boardgame_THEN_boardgame_is_updated_successfully() {
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
    public void GIVEN_multiple_boardgames_WHEN_find_recent_boardgames_THEN_correct_boardgames_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findRecentBoardgames(5, 0);
        assertNotNull(boardgames);
        assertFalse(boardgames.isEmpty());
        assertTrue(boardgames.stream().anyMatch(b -> b.getBoardgameName().equals("Test Boardgame")));
    }

    @Test
    @Order(5)
    public void GIVEN_existing_boardgame_WHEN_search_by_prefix_THEN_matching_boardgames_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findBoardgamesStartingWith("Test", 5, 0);
        assertNotNull(boardgames);
        assertFalse(boardgames.isEmpty());
        assertTrue(boardgames.stream().anyMatch(b -> b.getBoardgameName().equals("Test Boardgame")));
    }

    @Test
    @Order(6)
    public void GIVEN_valid_boardgame_id_WHEN_find_by_id_THEN_correct_boardgame_is_returned() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameById(savedBoardgame.getId());
        assertTrue(boardgame.isPresent());
        assertEquals("Test Boardgame", boardgame.get().getBoardgameName());
    }

    @Test
    @Order(7)
    public void GIVEN_existing_boardgame_WHEN_add_review_THEN_review_is_added_successfully() {
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
    @Order(6)
    public void GIVEN_boardgame_is_saved_WHEN_finding_boardgame_by_id_THEN_matching_boardgame_is_returned() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameById(savedBoardgame.getId());
        assertTrue(boardgame.isPresent());
        assertEquals("Test Boardgame", boardgame.get().getBoardgameName());
    }

    @Test
    @Order(7)
    public void GIVEN_boardgame_exists_WHEN_adding_review_to_boardgame_array_THEN_review_is_successfully_added() {
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
    public void GIVEN_boardgames_are_available_WHEN_getting_boardgame_tags_THEN_correct_tags_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<String> tags = boardgameDBMongo.getBoardgameTags();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertTrue(tags.contains("Test Boardgame"));
    }

    @Test
    @Order(9)
    public void GIVEN_boardgames_have_categories_WHEN_getting_boardgame_categories_THEN_correct_categories_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<String> categories = boardgameDBMongo.getBoardgamesCategories();
        assertNotNull(categories);
        assertTrue(categories.contains("Strategic"));
    }

    @Test
    @Order(10)
    public void GIVEN_boardgames_have_category_WHEN_finding_boardgames_by_category_THEN_matching_boardgames_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findBoardgamesByCategory("Strategic", 10, 0);
        assertNotNull(boardgames);
        assertFalse(boardgames.isEmpty());
        assertEquals("Test Boardgame", boardgames.get(0).getBoardgameName());
    }

    @Test
    @Order(11)
    public void GIVEN_boardgame_with_review_WHEN_deleting_review_by_id_THEN_review_is_removed() {
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
    public void GIVEN_boardgame_exists_WHEN_deleting_boardgame_THEN_boardgame_is_removed() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isPresent());
        boardgameDBMongo.deleteBoardgame(boardgame.get());
        boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        assertTrue(boardgame.isEmpty());
    }
}
