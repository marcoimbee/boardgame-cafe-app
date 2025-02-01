# DB setup
The following guide is useful to populate the DBs with the datasets that can be found in the *"datasets"* folder.

*Note:* please do not forget to correctly include in the *src/main/resources/application.properties* file the information of the DBMSs with which the application must communicate.

## MongoDB (MongoDB Compass environment)
1) Create the Database
2) Create the various collections within it
3) Populate the collections just by loading the JSON file related to the specific collection through the MongoDB Compass' import procedure

## Neo4J
Please pay attention that the DB is properly configured and that the *“apoc-x.y.z-core.jar”* jar file
(x.y.z will correspond to your Neo4J version) is located inside the *“var/lib/neo4j/plugins”* directory.
The set of datasets files (.csv) to be loaded must be placed in the *“var/lib/neo4j/import”* directory.
If everything has been properly organized then the following commands can be ran (respecting their order) on 
*“Neo4J Browser”* to populate the database.

*Note*: some of the following operations may take several minutes to complete (a large number 
of nodes and relationships needs to be created).

## - Loading nodes on Neo4J -

#### User nodes
```
LOAD CSV WITH HEADERS FROM 'file:///usersNeo.csv' AS line
CREATE (:User {id: line._id, username: line.username})
```
#### Boardgame nodes
```
LOAD CSV WITH HEADERS FROM 'file:///boardgamesNeo.csv' AS line
CREATE (:Boardgame {id: line._id, boardgameName: line.boardgameName,
        image: line.image, description: line.description,
        yearPublished: toInteger(line.yearPublished)})
```
#### Post nodes
#### 1) *With tag (REFERS_TO and WRITES_POST relationships included)*
```
LOAD CSV WITH HEADERS FROM 'file:///postsWithTagNeo.csv' AS line
MATCH (u:User {username: line.username})
MATCH (b:Boardgame {boardgameName: line.tag})
CREATE (p:Post {id: line._id})
CREATE (u)-[:WRITES_POST]->(p)
CREATE (p)-[:REFERS_TO]->(b)
```

#### 2) *Without tag (WRITES_POST relationships included)*
```
LOAD CSV WITH HEADERS FROM 'file:///postsWithoutTagNeo.csv' AS line
MATCH (u:User {username: line.username})
CREATE (p:Post {id: line._id})
CREATE (u)-[:WRITES_POST]->(p)
```

### Creating 'FOLLOWS' and 'LIKES' relationships
#### User FOLLOWS User - ONE WAY: User 'A' cannot follow himself (the following script guarantees it)
```
WITH range(0, 10) as usersRange
MATCH (u1:User)
WITH collect(u1) as users, usersRange
MATCH (u2:User)
WITH u2, apoc.coll.randomItems(users, apoc.coll.randomItem(usersRange)) as randomUsers
WHERE u2 <> any(user IN randomUsers WHERE user = u2)
FOREACH (user in randomUsers | CREATE (u2)-[:FOLLOWS]->(user))
```

#### User LIKES Post - ONE WAY: User 'A' cannot like a post 2/more times (the following script guarantees it)
```
WITH range(1, 100) as likesRange
MATCH (u:User)
WITH collect(u) as users, likesRange
MATCH (p:Post)
WITH p, apoc.coll.randomItems(users, apoc.coll.randomItem(likesRange)) as randomUsers
FOREACH (user in randomUsers |
MERGE (user)-[:LIKES]->(p)
)
```
