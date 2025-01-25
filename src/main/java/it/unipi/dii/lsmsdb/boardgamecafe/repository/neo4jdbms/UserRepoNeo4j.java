package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

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

    @Query("MATCH (u: User {username: $username}) RETURN u")
    Optional<UserModelNeo4j> findByUsername(String username);

    @Query("MATCH (u:User {username: $username}) DETACH DELETE u")
    void deleteAndDetachUserByUsername(@Param("username") String username);

    @Query("MATCH (:User {username: $userName})<-[:FOLLOWS]-(follower:User) RETURN COUNT(DISTINCT follower)")
    int countFollowersByUsername(@Param("userName") String username);

    @Query("MATCH (:User {username: $userName})-[:FOLLOWS]->(followed:User) RETURN COUNT(DISTINCT followed)")
    int countFollowingByUsername(@Param("userName") String username);

    @Query("""
            MATCH (me:User{username: $username})-[:WRITES_POST]->(myPosts:Post)-[:REFERS_TO]->(myBGames:Boardgame)
            WITH me, COLLECT(DISTINCT myBGames.boardgameName) as myBGamesNames
            MATCH (notFriend:User)-[:WRITES_POST]->(post:Post)-[:REFERS_TO]->(notFriendBgames:Boardgame)
            WHERE (notFriendBgames.boardgameName IN myBGamesNames)
            AND (NOT (me)-[:FOLLOWS]->(notFriend)) AND me <> notFriend
            RETURN notFriend.username SKIP $skipCounter LIMIT $limit
            """)
    List<String> usersByCommonBoardgamePosted(@Param("username") String username, @Param("limit") int limit, @Param("skipCounter") int skipCounter);

    @Query("""
            MATCH (u)<-[followRel:FOLLOWS]-(follower:User)
            WITH u, COUNT(DISTINCT followRel) AS followersCount
            WHERE followersCount >= $minFollowersCount
            RETURN u.username AS username
            ORDER BY followersCount DESC
            LIMIT $limit
            """)
    List<String> findMostFollowedUsersUsernames(@Param("minFollowersCount") long minFollowersCount, @Param("limit") int limit);

    @Query("""
            MATCH(myPost:Post)<-[:LIKES]-(me:User{username: $username})
            WITH myPost, COLLECT(DISTINCT myPost.id) as myPostID, me
            MATCH (notFriend:User)-[:LIKES]->(p:Post)
            WHERE (p.id IN myPostID) AND (notFriend.username <> $username)
            AND NOT (me)-[:FOLLOWS]->(notFriend)
            WITH notFriend, COUNT(p) as sameLikedPosts
            RETURN notFriend.username
            ORDER BY sameLikedPosts DESC
            SKIP $skipCounter LIMIT $limit;
            """)
    List<String> findUsersBySameLikedPosts(@Param("username")String username, @Param("limit") int limit, @Param("skipCounter") int skipCounter);


    @Query("""
            MATCH (follower:User {username: $username})-[r:FOLLOWS]->(followed:User)
            RETURN followed.username
            """)
    List<String> findFollowedUsernamesByUsername(@Param("username") String username);

    @Query("""
            MATCH (u1:User {username: $username}), (u2:User {username: $followed})
            MERGE (u1)-[:FOLLOWS]->(u2)
            """)
    void addFollowRelationship(@Param("username") String followingUser, @Param("followed") String followedUser);

    @Query("""
            MATCH (u1:User {username: $username})-[f:FOLLOWS]->(u2:User {username: $unfollowed})
            DELETE f
            """)
    void removeFollowRelationship(@Param("username") String unfollowingUser, @Param("unfollowed") String unfollowedUser);

    @Query("""
            MATCH (u:User {username: $username})-[:FOLLOWS]->(followed:User)-[:WRITES_POST]->(p:Post)
            RETURN DISTINCT followed.username SKIP $skip LIMIT $limit
            """)
    List<String> findFollowedUsernamesWhoCreatedAtLeastOnePost(@Param("username") String username,
                                       @Param("limit") int limitResults,
                                       @Param("skip") int skipCounter);
}