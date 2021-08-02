# quarkus-flygraph

### Neo4j OGM migration system for quarkus applications

Make your migrations easy using versioned .cypher files

## Neo4j support

3.2.x, 3.3.x, 3.4.x, 3.5.x

4.0.x, 4.1.x (only supported over bolt)

## How to use

Import quarkus-flygraph dependency in your pom.xml

Using maven:

        <dependency>
            <groupId>io.github.micaelsf</groupId>
            <artifactId>quarkus-flygraph</artifactId>
            <version>1.0.0</version>
        </dependency>

Config your neo4j connection and credentials in you `application.properties`
where you want the migrations to run at (following is the default configs)

    flygraph.uri=bolt://localhost:7687
    flygraph.database=neo4j
    flygraph.authentication.username=neo4j
    flygraph.authentication.password=""

Create this folder structure under `resources` (this path can be changed at
application.properties using the config `flygraph.migration.path`)

    resources
        |- db
            |- migrations

Add your first migration file at `resources/db/migrations`:

    V1.0.0__my-first-migration.cypher

with the content:

    RETURN "My first migration"

Run your application, and that's it, you must see your migration in your neo4j
graph database! :)

## Properties

### flygraph.uri

#### bolt://localhost:7687 (default)

Set the URI of the database. The driver is determined from the URI based on its
scheme (http/https for HttpDriver, file for EmbeddedDriver, bolt for BoltDriver)

### flygraph.database

#### neo4j (default)

Your neo4j database name

### flygraph.authentication.username

#### neo4j (default)

Authentication name used to connect to the URI

### flygraph.authentication.password

Authentication password used to connect to the URI

### flygraph.migration.path

#### db/migrations (default)

Migrations directory path where all migrations will be searched to be executed
orderly. It is defined at /src/main/java/resources/<migration path>

### flygraph.root.node

#### root (default)

Migration root node id

### flygraph.migrate-at-start

#### true (default)

Define if the migrations occurs at the start of your application

### flygraph.version.baseline

#### 1.0.0.0 (default)

Initial baseline defined at the root node

### flygraph.version.separator

#### . (default)

Migration file version separator token

### flygraph.version.valid.houses

#### 2 (default)

Migration file version maximum decimal houses allowed between each separator
token

### flygraph.mode

#### Available: [prod (default), dev]

Define witch migrations to apply. If `prod` is defined, all migrations under
`/src/main/java/resources/<migration-path>` will be applied. If `dev` is
defined, all migrations under `/src/test/java/resources/<migration-path>` will
be applied