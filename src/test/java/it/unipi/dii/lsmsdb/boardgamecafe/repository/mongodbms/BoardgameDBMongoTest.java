package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
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
    @Autowired
    private ReviewDBMongo reviewDBMongo;
    @Autowired
    private UserDBMongo userDBMongo;

    private BoardgameModelMongo testBoardgame;

    @BeforeEach
    public void start() {
        testBoardgame = init();
    }

    @AfterEach
    public void clean() {
        if (testBoardgame != null)
        {
            Optional<BoardgameModelMongo> boardgameToDeleteOptional = boardgameDBMongo.findBoardgameByName(testBoardgame.getBoardgameName());
            boardgameToDeleteOptional.ifPresent(boardgameToDelete -> boardgameDBMongo.deleteBoardgame(boardgameToDelete));
        }
    }

    private BoardgameModelMongo init() {
        BoardgameModelMongo boardgame = new BoardgameModelMongo();
        boardgame.setBoardgameName("Test Boardgame");
        boardgame.setYearPublished(2025);
        boardgame.setDescription("A test boardgame");
        boardgame.setImage("url_image");
        boardgame.setMinPlayers(2);
        boardgame.setMaxPlayers(6);
        boardgame.setPlayingTime(120);
        boardgame.setMinAge(8);
        boardgame.setAvgRating(-1.0);
        boardgame.setReviewCount(0);

        List<String> categories = new ArrayList<>();
        List<String> designers = new ArrayList<>();
        List<String> publishers = new ArrayList<>();
        categories.add("Strategic");
        designers.add("designer");
        publishers.add("publisher");

        boardgame.setBoardgameCategory(categories);
        boardgame.setBoardgameDesigner(designers);
        boardgame.setBoardgamePublisher(publishers);

        return boardgame;
    }

    @Test
    @Order(10)
    public void GIVEN_valid_boardgame_WHEN_add_boardgame_THEN_boardgame_is_added_successfully() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);
        assertNotNull(savedBoardgame);
        assertEquals("Test Boardgame", savedBoardgame.getBoardgameName());
    }

    @Test
    @Order(20)
    public void GIVEN_existing_boardgame_WHEN_find_by_name_THEN_correct_boardgame_is_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName(testBoardgame.getBoardgameName());
        assertTrue(boardgame.isPresent());
        assertEquals(testBoardgame.getBoardgameName(), boardgame.get().getBoardgameName());
    }

    @Test
    @Order(30)
    public void GIVEN_existing_boardgame_WHEN_update_boardgame_THEN_boardgame_is_updated_successfully() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);
        String updataedBoardgameName = "updated_TestBoardgame";
        savedBoardgame.setBoardgameName(updataedBoardgameName);
        boolean shouldReturnTrue = boardgameDBMongo.updateBoardgameMongo(savedBoardgame.getId(), savedBoardgame);
        assertTrue(shouldReturnTrue);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName(updataedBoardgameName);
        assertTrue(boardgame.isPresent());
    }

    @Test
    @Order(40)
    public void GIVEN_multiple_boardgames_WHEN_find_recent_boardgames_THEN_correct_boardgames_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findRecentBoardgames(5, 0);
        assertNotNull(boardgames);
        assertTrue(boardgames.stream().anyMatch(b -> b.getBoardgameName().equals("Test Boardgame")));
    }

    @Test
    @Order(50)
    public void GIVEN_existing_boardgame_WHEN_search_by_prefix_THEN_matching_boardgames_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        String prefixBoardgameName = "Test Board";
        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findBoardgamesStartingWith(prefixBoardgameName, 5, 0);
        assertNotNull(boardgames);
        long shouldContainsOnlyOneMatch = boardgames.stream().filter( b -> b.getBoardgameName().startsWith(prefixBoardgameName)).count();
        assertEquals(1, shouldContainsOnlyOneMatch);
    }

    @Test
    @Order(60)
    public void GIVEN_valid_boardgame_id_WHEN_find_by_id_THEN_correct_boardgame_is_returned() {
        BoardgameModelMongo savedBoardgame = boardgameDBMongo.addBoardgame(testBoardgame);

        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameById(savedBoardgame.getId());
        assertEquals("Test Boardgame", boardgame.get().getBoardgameName());
    }

    @Test
    @Order(70)
    public void GIVEN_boardgames_are_available_WHEN_getting_boardgame_tags_THEN_correct_tags_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<String> tags = boardgameDBMongo.getBoardgameTags();
        boolean shouldBeTrue = tags.contains("Test Boardgame");
        assertTrue(shouldBeTrue);
    }

    @Test
    @Order(80)
    public void GIVEN_boardgames_have_categories_WHEN_getting_boardgame_categories_THEN_correct_categories_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<String> categories = boardgameDBMongo.getBoardgamesCategories();
        boolean shouldBeTrue = categories.contains("Strategic");
        assertTrue(shouldBeTrue);
    }

    @Test
    @Order(90)
    public void GIVEN_boardgames_have_category_WHEN_finding_boardgames_by_category_THEN_matching_boardgames_are_returned() {
        boardgameDBMongo.addBoardgame(testBoardgame);

        List<BoardgameModelMongo> boardgames = boardgameDBMongo.findBoardgamesByCategory("Strategic", 10, 0);
        assertEquals("Test Boardgame", boardgames.get(0).getBoardgameName());
    }

    @Test
    @Order(100)
    public void GIVEN_boardgame_exists_WHEN_deleting_boardgame_THEN_boardgame_is_removed() {
        boardgameDBMongo.addBoardgame(testBoardgame);
        Optional<BoardgameModelMongo> boardgame = boardgameDBMongo.findBoardgameByName(testBoardgame.getBoardgameName());
        BoardgameModelMongo b = boardgame.get();
        boolean shouldReturnTrue = boardgameDBMongo.deleteBoardgame(b);
        assertTrue(shouldReturnTrue);

        boardgame = boardgameDBMongo.findBoardgameByName("Test Boardgame");
        boolean shouldBeAnEmptyDocument = boardgame.isEmpty();
        assertTrue(shouldBeAnEmptyDocument);
    }

    @Test
    @Order(110)
    public void GIVEN_boardgame_with_only_review_by_user_WHEN_deleting_that_user_THEN_boardgames_rating_is_resetted() {
        testBoardgame.setAvgRating(10);
        testBoardgame.setReviewCount(1);
        boardgameDBMongo.addBoardgame(testBoardgame);
        boolean shouldReturnTrue = boardgameDBMongo.updateRatingAfterUserDeletion(testBoardgame.getBoardgameName(), List.of(10));
        assertTrue(shouldReturnTrue);

        double noAvgRatingValue = -1.0;
        int zeroReviewCount = 0;
        BoardgameModelMongo boardgameUpdatedWithoutAnyReviews = boardgameDBMongo.findBoardgameByName(testBoardgame.getBoardgameName()).get();
        boolean shouldBeTrue = (boardgameUpdatedWithoutAnyReviews.getAvgRating() == noAvgRatingValue)
                                &&
                                (boardgameUpdatedWithoutAnyReviews.getReviewCount() == zeroReviewCount);
        assertTrue(shouldBeTrue);
    }
}
