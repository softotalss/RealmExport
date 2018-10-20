# RealmExport

RealmExport is a [Realm](https://realm.io/) helper to export Realm Databases to:

- Json
- Json File

You can use this Json Object to send by email or for any other things that you need.

**Only increase the size of your project by 10 KB ;)**

Download
-------------------
By Gradle:

```groovy
repositories {
    maven { url 'https://github.com/softotalss/realmexport/raw/master/maven-repository' }
}
```

```groovy
dependencies {
    implementation 'com.google.code.gson:gson:x.x.x'
    implementation 'com.github.softotalss:realm_export:1.0.0'
}
```

How do I use RealmExport?
-------------------
You can use `RealmExport` in any part of your code, but it's recommended that you use it in a background task.

```java
// Export to Json:
JsonObject dbJson = RealmExport.init((new RealmConfiguration.Builder()).build()).toJson();  
```

```java
// Export to Json File:
JsonObject dbJson = RealmExport.init((new RealmConfiguration.Builder()).build()).toJsonFile("path/bd.json");  
```
> Not forget replace "(new RealmConfiguration.Builder()).build()" for your specific Realm Configuration

Deployment
------
1. update version information
2. gradlew assemble :real_export:publishMavenPublicationToMavenRepository
3. git add, commit, push (on master branch)

Author
------
Alejandro Santana - @softotalss on GitHub

> This project is based on [stetho-realm](https://github.com/wickedev/stetho-realm)

License
-------
BSD, part MIT and Apache 2.0. See the [LICENSE][2] file for details.

[1]: https://github.com/softotalss/realmexport/releases
[2]: https://github.com/softotalss/RealmExport/blob/master/LICENSE