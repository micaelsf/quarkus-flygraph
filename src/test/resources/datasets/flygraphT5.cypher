CREATE (r:FlygraphRoot{id: 'root', currentVersion: '2.0.1.1'})
CREATE (m:FlygraphMigration{id: 'V2.0.1.1__test', checksum:'C89CF99E', executed: true})
CREATE (q:FlygraphQuery{query:'RETURN "Hello world 2.0.1.1"'})
CREATE (r)<-[:MIGRATED_WITHIN]-(m)<-[:EXECUTED_WITHIN]-(q)