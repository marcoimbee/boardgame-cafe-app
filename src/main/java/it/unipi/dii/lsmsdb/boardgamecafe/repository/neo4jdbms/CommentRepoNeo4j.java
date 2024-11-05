package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepoNeo4j extends Neo4jRepository<CommentModelNeo4j, String> {
    @Query("MATCH (c:Comment {id: $id}) DETACH DELETE c")
    void deleteAndDetach(@Param("id") String id);
    @Query("MATCH (c:Comment)-[]-(p:Post {id: $postId}) DETACH DELETE c")
    void deleteByPost(@Param("postId") String postId);
    @Query("MATCH (c:Comment)<-[:WRITES]-(u:User {username: $username}) DETACH DELETE c")
    void deleteByUsername(@Param("username") String username);
    @NotNull
    @Query("MATCH (c: Comment {id: $id}) RETURN c")
    Optional<CommentModelNeo4j> findById(@Param("id") @NotNull String id);

}
