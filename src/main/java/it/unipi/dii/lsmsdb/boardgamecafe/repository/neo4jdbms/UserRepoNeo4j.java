package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepoNeo4j extends Neo4jRepository<UserModelNeo4j, String> {

    @NotNull
    @Query("MATCH (u: User {id: $id}) RETURN u")
    Optional<UserModelNeo4j> findById(@Param("id") @NotNull String id);


//    @Query("MATCH (n:User{username: $username})-[:WRITES]->(p:Post)\n" +
//            "WITH n, collect(p) AS posts\n" +
//            "RETURN n, posts")
    @Query("MATCH (u: User {username: $username}) RETURN u")
    Optional<UserModelNeo4j> findByUsername(String username);

    @Query("MATCH (u:User {username: $username}) DETACH DELETE u, (u)-[r]-()")
    void deleteAndDetachUserByUsername(@Param("username") String username);

    @Query("MATCH (u:User{username: $username})-[:WRITES_COMMENT]->(c:Comment) RETURN u, collect(c) as comments")
    Optional<UserModelNeo4j> findByNameWithComments(@Param("username") String username);
    @Query("MATCH (u1:User {username: $userName})<-[:FOLLOWS]-(u2:User) RETURN DISTINCT u2")
    List<UserModelNeo4j> findFollowersByUsername(@Param("userName") String username);

    @Query("MATCH (u1:User {username: $userName})-[:FOLLOWS]->(u2:User) RETURN DISTINCT u2")
    List<UserModelNeo4j> findFollowingByUsername(@Param("userName") String username);

    @Query("MATCH (:User {username: $userName})<-[:FOLLOWS]-(follower:User) RETURN COUNT(DISTINCT follower)")
    int countFollowersByUsername(@Param("userName") String username);

    @Query("MATCH (:User {username: $userName})-[:FOLLOWS]->(followed:User) RETURN COUNT(DISTINCT followed)")
    int countFollowingByUsername(@Param("userName") String username);

    @Query("MATCH (u1:User)-[a:ADDS]->(b:Boardgame {name: $boardgameName}) RETURN DISTINCT u1")
    List<UserModelNeo4j> findUsersByBoardgameName(@Param("boardgameName") String boardgamename);

    @Query("MATCH (me:User{username: $username})-[:WRITES_POST]->(myPosts:Post)-[:REFERS_TO]->(myBGames:Boardgame)\n" +
            "WITH me, COLLECT(DISTINCT myBGames.boardgameName) as myBGamesNames\n" +
            "MATCH (notFriend:User)-[:WRITES_POST]->(post:Post)-[:REFERS_TO]->(notFriendBgames:Boardgame)\n" +
            "WHERE (notFriendBgames.boardgameName IN myBGamesNames)\n" +
            "AND (NOT (me)-[:FOLLOWS]->(notFriend)) AND me <> notFriend\n" +
            "RETURN notFriend.username " +
            "SKIP $skipCounter " +
            "LIMIT $limit")
    List<String> usersByCommonBoardgamePosted(@Param("username") String username, @Param("limit") int limit, @Param("skipCounter") int skipCounter);

    @Query("MATCH (u)<-[followRel:FOLLOWS]-(follower:User) " +
            "WITH u, COUNT(DISTINCT followRel) AS followersCount " +
            "WHERE followersCount >= $minFollowersCount " +
            "RETURN u.username AS username " +
            "ORDER BY followersCount DESC " +
            "LIMIT $limit")
    List<String> findMostFollowedUsersUsernames(@Param("minFollowersCount") long minFollowersCount, @Param("limit") int limit);

    @Query("MATCH(myPost:Post)<-[:LIKES]-(me:User{username: $username})\n" +
            "WITH myPost, COLLECT(DISTINCT myPost.id) as myPostID, me\n" +
            "MATCH (notFriend:User)-[:LIKES]->(p:Post)\n" +
            "WHERE (p.id IN myPostID) AND (notFriend.username <> $username)\n" +
            "AND NOT (me)-[:FOLLOWS]->(notFriend)\n" +
            "WITH notFriend, COUNT(p) as sameLikedPosts\n" +
            "RETURN notFriend.username\n" +
            "ORDER BY sameLikedPosts DESC\n" +
            "SKIP $skipCounter " +
            "LIMIT $limit")
    List<String> findUsersBySameLikedPosts(@Param("username")String username, @Param("limit") int limit, @Param("skipCounter") int skipCounter);

    @Query("MATCH (u:User {username: $username})\n" +
            "SET u.username = \"[Banned user]\"\n")
    void setUsernameAsBanned(@Param("username") String username);

    @Query("MATCH (u:User {username: $oldUsername})\n" +
            "SET u.username = $oldUsername\n")
    void setNewUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);

    @Query("MATCH (u:User {id: $userId}) " +
            "SET u.username = $username")
    void restoreUserUsername(@Param("userId") String userId, @Param("username") String username);

    @Query("MATCH (follower:User {username: $username})-[r:FOLLOWS]->(followed:User)\n" +
            "RETURN followed.username")
    List<String> findFollowedUsernamesByUsername(@Param("username") String username);

    @Query("MATCH (u1:User {username: $username}), (u2:User {username: $followed}) " +
            "MERGE (u1)-[:FOLLOWS]->(u2)")
    void addFollowRelationship(@Param("username") String followingUser, @Param("followed") String followedUser);

    @Query("MATCH (u1:User {username: $username})-[f:FOLLOWS]->(u2:User {username: $unfollowed}) " +
            "DELETE f")
    void removeFollowRelationship(@Param("username") String unfollowingUser, @Param("unfollowed") String unfollowedUser);
}