SimpleDataLib
===========

SimpleDataLib provides fast and easy to use database access, YAML access, CSV access, serialization, and error logging.  SimpleDataLib supports SQLite and MySQL.  Database writes can be queued and written asynchronously in transactions to prevent slowing of the main thread.  Fields can be added, removed, and changed easily for both MySQL and SQLite.

Want to use SimpleDataLib?
---------
To create a SQLite database:
```
SimpleDataLib sdl = new SimpleDataLib("Demo");
sdl.initialize();
sdl.getSQLManager().createDatabase();
```

To create a MySQL database:
```
SimpleDataLib sdl = new SimpleDataLib("Demo");
sdl.initialize();
sdl.getSQLManager().enableMySQL("host", "database", "username", "password", port);
sdl.getSQLManager().createDatabase();
```

An example database write:
```
SQLWrite sw = sdl.getSQLManager().getSQLWrite();
HashMap<String,String> values = new HashMap<String,String>();
values.put("FIELD1", "value1");
values.put("FIELD2", "value2");
values.put("FIELD3", "value3");
sw.performInsert("table_name", values);
```

An example database read:
```
SQLRead sr = sdl.getSQLManager().getSQLRead();
QueryResult data = sr.select("SELECT * FROM table_name");
while (data.next()) {
	double field1 = playerData.getDouble("FIELD1");
	String field2 = playerData.getString("FIELD2");
	{...do something here...}
}
data.close();
```

SimpleDataLib uses Maven and can be obtained from my repository.  Just add the repository to your pom.xml as such:
```
<repositories>
	<repository>
		<id>regalowl-repo</id>
		<url>http://www.regalowl.net:8080/repository/snapshots/</url>
	</repository>
</repositories>
```

Also add SimpleDataLib as a dependency with:
```
<dependencies>
	<dependency>
		<groupId>regalowl.simpledatalib</groupId>
		<artifactId>simpledatalib</artifactId>
		<version>0.1.024-SNAPSHOT</version>
	</dependency>
</dependencies>
```
To shade SimpleDataLib into your jar add the following to your pom.xml:
```
<build>
	<plugins>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-shade-plugin</artifactId>
			<version>2.1</version>
			<executions>
				<execution>
					<phase>package</phase>
					<goals>
						<goal>shade</goal>
					</goals>
					<configuration>
						<minimizeJar>true</minimizeJar>
						<filters>
							<filter>
								<artifact>*:*</artifact>
								<excludes>
									<exclude>META-INF/*.SF</exclude>
									<exclude>META-INF/*.DSA</exclude>
									<exclude>META-INF/*.RSA</exclude>
								</excludes>
							</filter>
						</filters>
					</configuration>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

