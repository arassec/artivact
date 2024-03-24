# Configuring the Application

## General
Artivact is written in Java using the popular open source [Spring framework](https://spring.io/).

Thus, it can be configured like any other Spring-Boot application, be it with JVM parameters, an 
application.properties file in the root directory or any other method supported by Spring.

## The Project Directory

The project root directory can be configured by the following parameter:

::: code-group
```[Command line parameter]
$> java -jar artivact-server-v0.0.0.jar \
        -Dartivact.project.root=/opt/artivact-server/project-root
```

```[application.properties]
artivact.project.root=/opt/artivact-server/project-root
```
:::

## Database Configuration

Artivact supports *H2* and *PostgreSQL* databases.
It uses an embedded H2 database as default which will be stored in a dbdata directory in the project folder. 
For production use, PostgreSQL is advised. 
You can configure the Database to use by configuring it with standard Spring mechanisms.

::: code-group
```[Command line parameter]
$> java -jar artivact-server-v0.0.0.jar \
        -Dspring.datasource.url=jdbc:postgresql://localhost:5432/postgres \
        -Dspring.datasource.username=artivact \
        -Dspring.datasource.password=artivact \
        -Dspring.datasource.driver-class-name=org.postgresql.Driver \
```

```[application.properties]
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=artivact
spring.datasource.password=artivact
spring.datasource.driver-class-name=org.postgresql.Driver
```
:::

## Further Customization

The following can be configured inside the application itself:

- Administrator and user accounts.
- Application appearance like color theme, title, favicons, etc.
- Supported locales for I18N.
- Item properties, tags and the license for media files.