package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestUserDBMongo
{
    @Autowired
    UserDBMongo userDBMongo;
    static UserModelMongo user1;
    static UserModelMongo user2;
    static UserModelMongo user3;
    static final String testBoardgameName1 = "TestBoardgame1";
    static final String testBoardgameName2 = "TestBoardgame2";
    static final String testUsername = "testUsername";

    @BeforeAll
    public static void setUp() {
        init();
    }

    @AfterAll
    public static void clean(){ }

    private static void init()
    {

    }

    @Test @Order(10)
    void addUser()
    {

    }
    @Test
    void getUserMongo()
    {

    }

    @Test
    void updateUser() {
    }


    @Test
    void findByUsername() {
    }

    @Test
    void findByEmail() {
    }

    @Test
    void findUserById() {
    }

    @Test
    void deleteUserById() {
    }

    @Test
    void deleteReviewInUserReviewsById() {
    }



    @Test
    void addReviewInUserArray() {
    }

    @Test
    void getUserUsernames() {
    }

    @Test
    void getBannedUsers() {
    }
    @Test @Order(200)
    void deleteUser() {
    }
}