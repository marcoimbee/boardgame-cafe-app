package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardgameRepoMongo extends MongoRepository<BoardgameModelMongo, String> {

    Optional<BoardgameModelMongo> findByBoardgameName(String boardgameName);

    @Query(value = "{'name': {$regex : ?0, $options: 'i'}}")
    List<BoardgameModelMongo> findByNameRegexOrderByYearPublicationDesc(String name, Sort sort);
}
