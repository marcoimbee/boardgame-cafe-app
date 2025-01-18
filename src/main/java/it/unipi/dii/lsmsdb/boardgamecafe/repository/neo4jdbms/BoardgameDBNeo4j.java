package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BoardgameDBNeo4j {

    @Autowired
    BoardgameRepoNeo4j boardgameRepoNeo4j;
    @Autowired
    Neo4jOperations neo4jOperations;

    public BoardgameRepoNeo4j getUserNeo4jDB() {
        return boardgameRepoNeo4j;
    }

    public BoardgameModelNeo4j addBoardgame(BoardgameModelNeo4j boardgame) {
        try {
            return boardgameRepoNeo4j.save(boardgame);
        } catch (Exception e) {
            System.err.println("[ERROR] addBoardgame()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteBoardgameDetach(String boardgameName) {
        try {
            boardgameRepoNeo4j.deleteAndDetachBoardgameByBoardgameName(boardgameName);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteBoardgameDetach()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBoardgameNeo4j(String id, BoardgameModelNeo4j newBoardgame) {
        try {
            Optional<BoardgameModelNeo4j> boardgameNeo = boardgameRepoNeo4j.findById(id);
            if (boardgameNeo.isPresent()) {

                BoardgameModelNeo4j boardgameToBeUpdated = boardgameNeo.get();

                boardgameToBeUpdated.setBoardgameName(newBoardgame.getBoardgameName());
                boardgameToBeUpdated.setImage(newBoardgame.getImage());
                boardgameToBeUpdated.setDescription(newBoardgame.getDescription());
                boardgameToBeUpdated.setYearPublished(newBoardgame.getYearPublished());

                boardgameRepoNeo4j.save(boardgameToBeUpdated);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] updateBoardgameNeo4j()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public Optional<BoardgameModelNeo4j> findById(String id) {
        Optional<BoardgameModelNeo4j> bg = Optional.empty();
        try {
            bg = boardgameRepoNeo4j.findById(id);
        } catch (Exception e) {
            System.err.println("[ERROR] findById()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return bg;
    }

    public Optional<BoardgameModelNeo4j> findByBoardgameName(String boardgameName) {
        Optional<BoardgameModelNeo4j> bg = Optional.empty();
        try {
            bg = boardgameRepoNeo4j.findByBoardgameName(boardgameName);
        } catch (Exception e) {
            System.err.println("[ERROR] findByBoardgameName()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return bg;
    }

    // Suggest boardgames which users you follow posted about
    public List<BoardgameModelNeo4j> getBoardgamesWithPostsByFollowedUsers(String username, int limit, int skipCounter) {
        List<BoardgameModelNeo4j> boardgames = new ArrayList<>();
        try {
            boardgames = boardgameRepoNeo4j.getBoardgamesWithPostsByFollowedUsers(username, limit, skipCounter);
        } catch (Exception e) {
            System.err.println("[ERROR] getBoardgamesWithPostsByFollowedUsers()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
        }
        return boardgames;
    }

    public List<BoardgameModelNeo4j> findRecentBoardgames(int limit, int skip) {
        Optional<List<BoardgameModelNeo4j>> optionalRecentBoardgames = Optional.empty();
        try {
            optionalRecentBoardgames = boardgameRepoNeo4j.findRecentBoardgames(skip, limit);
        } catch (Exception e) {
            System.err.println("[ERROR] findRecentBoardgames()@BoardgameDBNeo4j.java raised an exception: " + e.getMessage());
        }

        return optionalRecentBoardgames.orElseGet(ArrayList::new);      // Return an empty list
    }
}
