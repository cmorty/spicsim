Compiling   
=========

Install Avrora
--------------

```sh
cd dependencies
mvn install
```

Package
-------
```sh
mvn package
```

Run
---
```sh
java -jar target *-shaded.jar
```