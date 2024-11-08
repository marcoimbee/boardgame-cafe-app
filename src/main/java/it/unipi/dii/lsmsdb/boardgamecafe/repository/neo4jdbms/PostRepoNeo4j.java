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

    @Query("MATCH (p:Post)<-[:WRITES_POST]-(u:User {username: $username}) RETURN DISTINCT p")
    List<PostModelNeo4j> findByAuthorName(@Param("username") String username);

    //To_evaluate usage
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

    @Query("MATCH (p:Post {id: $postId})<-[:LIKES]-(:User) RETURN COUNT(*)")
    int findPostLikesById(@Param("postId") String postId);

    @Query("MATCH (p:Post)<-[:LIKES]-(:User) RETURN p, COUNT(*) AS likeCount ORDER BY likeCount DESC LIMIT 1")
    PostModelNeo4j findMostLikedPost();

    @Query("MATCH (u:User{username: $username})-[:FOLLOWS]->(following:User)-\n" +
            "[:LIKES]->(p:Post), (p)<-[l:LIKES]-(:User)\n" +
            "WHERE NOT EXISTS{MATCH (u)-[:LIKES]->(p)}\n" +
            "WITH p.id as id, COUNT(l) as likes\n" +
            "RETURN DISTINCT id, likes\n" +
            "ORDER BY likes desc\n" +
            "SKIP $skipCounter\n" +
            "LIMIT $limit")
    List<PostModelNeo4j> findPostsLikedByFollowedUsers(@Param("username") String username,
                                                       @Param("limit") int limitResults,
                                                       @Param("skipCounter") int skipCounter);


    //Valutare se lasciare l'ordinamento in base ai likes (rallenta molto)
    @Query("MATCH (u:User{username: $username})-[:FOLLOWS]->(following:User)-[:WRITES_COMMENT]->(c:Comment)-[:REPLY]->(p:Post)\n" +
            "OPTIONAL MATCH (p)<-[l:LIKES]-(:User)\n" + //permette di includere anche post che non hanno ricevuto "mi piace"
            "WHERE NOT EXISTS{ MATCH (u)-[:WRITES]->(c)-[:REPLY]->(p) }\n" +
            "WITH p.id AS id, COUNT(l) AS likes\n" +
            "RETURN DISTINCT id, likes\n" +
            "ORDER BY likes DESC\n" +
            "SKIP $skipCounter\n" +
            "LIMIT $limit")
    List<PostModelNeo4j> findPostsCommentedByFollowedUsers(@Param("username") String username,
                                                           @Param("limit") int limitResults,
                                                           @Param("skipCounter") int skipCounter);

    @Query("MATCH (currentUser:User {username: $username})-[:FOLLOWS]->(followedUser:User)-[:WRITES_POST]->(post:Post)\n" +
            "OPTIONAL MATCH (post)-[:LIKES]->(likedBy:User)\n" +
            "WITH post, COUNT(likedBy) AS likeCount\n" +
            "RETURN post\n" +
            "ORDER BY likeCount DESC\n" +
            "SKIP $skipCounter\n" +
            "LIMIT $limit")
    List<PostModelNeo4j> findPostsCreatedByFollowedUsers(@Param("username") String username,
                                                         @Param("limit") int limitResults,
                                                         @Param("skipCounter") int skipCounter);

}