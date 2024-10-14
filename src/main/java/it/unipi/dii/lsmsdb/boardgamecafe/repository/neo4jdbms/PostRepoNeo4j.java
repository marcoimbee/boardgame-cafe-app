package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepoNeo4j extends Neo4jRepository<PostModelNeo4j, String> {
    @Query("MATCH (p:Post {id: $id}) DETACH DELETE p")
    void deleteAndDetach(@Param("id") String id);

    @Query("MATCH (p:Post)-[:REFERS_TO]->(b:Boardgame {boardgameName: $bgName}) DETACH DELETE p")
    void deleteByReferredBoardgame(@Param("bgName") String bgName);

    @Query("MATCH (p:Post)<-[:WRITES]-(u:User {username: $username}) DETACH DELETE p")
    void deleteByUsername(@Param("username") String username);

    @Query("MATCH (c:Comment {id: $id})-[:REPLY]->(p:Post) RETURN p")
    Optional<PostModelNeo4j> findFromCommentId(@Param("id") String commentId);

    @Query("MATCH (p:Post)-[:REFERS_TO]->(b:Boardgame {boardgameName: $bgn}) RETURN p")
    List<PostModelNeo4j> findFromReferredBoardgame(@Param("bgn") String boardgameName);

    @Query("MATCH (u:User {username: $username}), (p:Post {id: $postId}) MERGE (u)-[:LIKES]->(p)")
    void addLike(@Param("username") String username, @Param("postId") String postId);

    @Query("MATCH (u:User {username: $username})-[r:LIKES]->(p:Post {id: $postId}) DELETE r")
    void removeLike(@Param("username") String username, @Param("postId") String postId);

    @Query("MATCH (u:User {username: $username})-[:LIKES]->(p:Post {id: $postId}) RETURN COUNT(p) > 0")
    boolean hasLiked(@Param("username") String username, @Param("postId") String postId);
}