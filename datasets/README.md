
# { Application Usage }
The following guideline is useful to populate de DBMSs with the files that you can find in the current *"datasets"* folder
of this git-hub repository for using properly the application.

N.B. 

# Mongo DB
After creating the db and the various collections within it, in the local MongoDB Compass environment, simply
load, through the appropriate import procedure for the collection previously created, the json files to populate the collection.

## Neo4j DB
For neo4j you need to pay attention first of all that the DB is properly configured and that the jar file *“apoc-x.y.z-core.jar”*
(x.y.z will correspond to the version you are using of neo4j) is inside the directory *“var/lib/neo4j/plugins”*.
The the datasets files (.csv) to be loaded must be placed in the directory *“var/lib/neo4j/import”.*
If everything has been properly organized then you can run, respecting the following order,
these commands on *“neo4j browser”* to populate the database.

(N.B.)
Populating neo4j, in some of the following LOAD runs,
may take several minutes to complete due to the large number 
of nodes and relationships to be created.

### LOAD NODES ON NEO4J DB

#### USER NODES
```
LOAD CSV WITH HEADERS FROM 'file:///usersNeo.csv' AS line
CREATE (:User {id: line._id, username: line.username})
```
#### BOARDGAME NODES
```
LOAD CSV WITH HEADERS FROM 'file:///boardgamesNeo.csv' AS line
CREATE (:Boardgame {id: line._id, boardgameName: line.boardgameName,
        image: line.image, description: line.description,
        yearPublished: toInteger(line.yearPublished)})
```
#### POST NODES
#### 1) *WITH-TAG (WITH RELATIONSHIPS)*
```
LOAD CSV WITH HEADERS FROM 'file:///postsWithTagNeo.csv' AS line
MATCH (u:User {username: line.username})
MATCH (b:Boardgame {boardgameName: line.tag})
CREATE (p:Post {id: line._id})
CREATE (u)-[:WRITES_POST]->(p)
CREATE (p)-[:REFERS_TO]->(b)
```

#### 2) *WITHOUT-TAG (WITH RELATIONSHIPS)*
```
LOAD CSV WITH HEADERS FROM 'file:///postsWithoutTagNeo.csv' AS line
MATCH (u:User {username: line.username})
CREATE (p:Post {id: line._id})
CREATE (u)-[:WRITES_POST]->(p)
```

### CREATE RELATIONSHIPS FOLLOWS AND LIKES ON NEO4J DB

#### USER FOLLOWS USER - UNIQUE WAY: User 'A' cannot follow himself, and this script guarantees it
```
WITH range(0, 10) as usersRange
MATCH (u1:User)
WITH collect(u1) as users, usersRange
MATCH (u2:User)
WITH u2, apoc.coll.randomItems(users, apoc.coll.randomItem(usersRange)) as randomUsers
WHERE u2 <> any(user IN randomUsers WHERE user = u2)
FOREACH (user in randomUsers | CREATE (u2)-[:FOLLOWS]->(user))
```

#### USER LIKES POST - UNIQUE WAY: User A cannot likes a post 2 times, and this script guarantees it
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
