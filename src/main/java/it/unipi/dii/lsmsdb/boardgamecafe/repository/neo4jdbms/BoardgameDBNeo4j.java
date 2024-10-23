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


    public boolean addBoardgame(BoardgameModelNeo4j boardgameNeo4j) {
        boolean result = true;
        try {
            boardgameRepoNeo4j.save(boardgameNeo4j);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteBoardgameDetach(String boardgameName) {
        try {
            boardgameRepoNeo4j.deleteAndDetachBoardgameByBoardgameName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateBoardgameNeo4j(String id, BoardgameModelNeo4j newBoardgame) {

        boolean result = true;
        try {
            Optional<BoardgameModelNeo4j> boardgameNeo = boardgameRepoNeo4j.findById(id);
            if (boardgameNeo.isPresent()) {
                boardgameNeo.get().setBoardgameName(newBoardgame.getBoardgameName());
                boardgameNeo.get().setThumbnail(newBoardgame.getThumbnail());
                boardgameNeo.get().setYearPublished(newBoardgame.getYearPublished());

                boardgameRepoNeo4j.save(boardgameNeo.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public Optional<BoardgameModelNeo4j> findById(String id) {
        Optional<BoardgameModelNeo4j> bg = Optional.empty();
        try {
            bg = boardgameRepoNeo4j.findById(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return bg;
    }

    public Optional<BoardgameModelNeo4j> findByBoardgameName(String boardgameName) {
        Optional<BoardgameModelNeo4j> bg = Optional.empty();
        try {
            bg = boardgameRepoNeo4j.findByBoardgameName(boardgameName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return bg;
    }

    // Suggerisci Boardgame su cui hanno fatto post utenti che segui
    public List<String> getBoardgamesWithPostsByFollowedUsers(String username) {
        List<String> boardgames = new ArrayList<>();
        try {
            boardgames = boardgameRepoNeo4j.getBoardgamesWithPostsByFollowedUsers(username);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return boardgames;
    }
}
