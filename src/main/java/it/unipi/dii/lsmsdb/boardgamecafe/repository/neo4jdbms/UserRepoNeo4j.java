package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepoNeo4j extends Neo4jRepository<UserModelNeo4j, String> {

    Optional<UserModelNeo4j> findByUsername(String username);

    @Query("MATCH (u:User {username: $username}) DETACH DELETE u, (u)-[r]-()")
    void deleteAndDetachUserByUsername(@Param("username") String username);

    @Query("MATCH (u1:User {username: $userName})<-[:FOLLOWS]-(u2:User) RETURN DISTINCT u2")
    List<UserModelNeo4j> findFollowersByUsername(@Param("userName") String username);

    @Query("MATCH (u1:User {username: $userName})-[:FOLLOWS]->(u2:User) RETURN DISTINCT u2")
    List<UserModelNeo4j> findFollowingByUsername(@Param("userName") String username);

    @Query("MATCH (u1:User)-[a:ADDS]->(b:Boardgame {name: $boardgameName}) RETURN DISTINCT u1")
    List<UserModelNeo4j> findUsersByBoardgameName(@Param("boardgameName") String boardgamename);

    @Query("MATCH (me:User{username: $username})-[:WRITES]->(myPosts:Post)-[:REFERS_TO]->(myBGames:Boardgame)\n" +
            "WITH me, COLLECT(DISTINCT myBGames.boardgameName) as myBGamesNames\n" +
            "MATCH (notFriend:User)-[:WRITES]->(post:Post)-[:REFERS_TO]->(notFriendBgames:Boardgame)\n" +
            "WHERE (notFriendBgames.boardgameName IN myBGamesNames)\n" +
            "AND (NOT (me)-[:FOLLOWS]->(notFriend)) AND me <> notFriend\n" +
            "RETURN notFriend.username LIMIT $limit")
    List<String> usersByCommonBoardgamePosted(@Param("username") String username, @Param("limit") int limit);

    @Query("MATCH(myPost:Post)<-[:LIKES]-(me:User{username: $username})\n" +
            "WITH myPost, COLLECT(DISTINCT myPost.id) as myPostID, me\n" +
            "MATCH (notFriend:User)-[:LIKES]->(p:Post)\n" +
            "WHERE (p.id IN myPostID) AND (notFriend.username <> $username)\n" +
            "AND NOT (me)-[:FOLLOWS]->(notFriend)\n" +
            "WITH notFriend, COUNT(p) as sameLikedPosts\n" +
            "RETURN notFriend.username\n" +
            "ORDER BY sameLikedPosts DESC\n" +
            "LIMIT $limit")
    List<String> findUsersBySameLikedPosts(@Param("username")String username, @Param("limit") int limit);

}