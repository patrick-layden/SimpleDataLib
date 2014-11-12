SimpleDataLib
===========

SimpleDataLib provides fast and easy to use database access, YAML access, CSV access, serialization, and error logging.  SimpleDataLib supports SQLite and MySQL.  Database writes can be queued and written asynchronously in transactions to prevent slowing of the main thread.  Fields can be added, removed, and changed easily for both MySQL and SQLite.

Want to use SimpleDataLib?
---------


SimpleDataLib uses Maven and can be obtained from my repository.  Just add the repository to your pom.xml as such:
```    	
<repository>
	<id>regalowl-repo</id>
	<url>http://www.regalowl.net:8080/repository/snapshots/</url>
</repository>
```

Also add SimpleDataLib as a dependency with:
```
<dependency>
	<groupId>regalowl.simpledatalib</groupId>
	<artifactId>simpledatalib</artifactId>
	<version>0.1.024-SNAPSHOT</version>
</dependency>
```
To shade SimpleDataLib into your jar add the following:
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

