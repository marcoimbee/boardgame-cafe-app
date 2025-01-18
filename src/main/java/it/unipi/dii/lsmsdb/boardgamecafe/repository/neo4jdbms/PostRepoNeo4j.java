package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import org.jetbrains.annotations.NotNull;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepoNeo4j extends Neo4jRepository<PostModelNeo4j, String> {

    @Query("MATCH (p:Post {id: $id}) RETURN p")
    @NotNull
    Optional<PostModelNeo4j> findById(@Param("id") @NotNull String id);

    @Query("MATCH (p:Post {id: $id}) DETACH DELETE p")
    void deleteAndDetach(@Param("id") String id);

    @Query("MATCH (p:Post)-[:REFERS_TO]->(b:Boardgame {boardgameName: $bgName}) DETACH DELETE p")
    void deleteByReferredBoardgame(@Param("bgName") String bgName);

    @Query("MATCH (p:Post)<-[:WRITES_POST]-(u:User {username: $username}) DETACH DELETE p")
    void deleteByUsername(@Param("username") String username);

    @Query("MATCH (u:User {username: $username}), (p:Post {id: $postId}) MERGE (u)-[:LIKES]->(p)")
    void addLike(@Param("username") String username, @Param("postId") String postId);

    @Query("MATCH (u:User {username: $username})-[r:LIKES]->(p:Post {id: $postId}) DELETE r")
    void removeLike(@Param("username") String username, @Param("postId") String postId);

    @Query("MATCH (u:User {username: $username})-[:LIKES]->(p:Post {id: $postId}) RETURN COUNT(p) > 0")
    boolean hasLiked(@Param("username") String username, @Param("postId") String postId);

    @Query("MATCH (p:Post {id: $postId})<-[:LIKES]-(:User) RETURN COUNT(*)")
    int findPostLikesById(@Param("postId") String postId);

    @Query("MATCH (p:Post)<-[:LIKES]-(:User) RETURN p, COUNT(*) AS likeCount ORDER BY likeCount DESC LIMIT 1")
    PostModelNeo4j findMostLikedPost();

    @Query("""
            MATCH (u:User{username: $username})-[:FOLLOWS]->(following:User)-
            [:LIKES]->(p:Post), (p)<-[l:LIKES]-(:User)
            WHERE NOT EXISTS{MATCH (u)-[:LIKES]->(p)}
            WITH p.id as id, COUNT(l) as likes
            RETURN DISTINCT id, likes
            ORDER BY likes desc
            SKIP $skipCounter
            LIMIT $limit
            """)
    List<PostModelNeo4j> findPostsLikedByFollowedUsers(@Param("username") String username,
                                                       @Param("limit") int limitResults,
                                                       @Param("skipCounter") int skipCounter);

    @Query("""
            MATCH (currentUser:User {username: $username})-[:FOLLOWS]->(followedUser:User)-[:WRITES_POST]->(post:Post)
            OPTIONAL MATCH (post)-[:LIKES]->(likedBy:User)
            WITH post, COUNT(likedBy) AS likeCount
            RETURN post
            ORDER BY likeCount DESC
            SKIP $skipCounter
            LIMIT $limit
            """)
    List<PostModelNeo4j> findPostsCreatedByFollowedUsers(@Param("username") String username,
                                                         @Param("limit") int limitResults,
                                                         @Param("skipCounter") int skipCounter);

}
