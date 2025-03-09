# apl-db-attachments

Java tool to extract ticket attachments from a BMC Remedy Database. This is a refactoring of the Remedy Legacy Tool [DB Attachments](https://remedylegacy.com/tools/db-attachments/) to replace old third party libraries, especially log4j 1.x.  
To build a release zip simply execute `mvn package` (using an Oracle jdk 1.8). You find the resulting zip file in the target directory.  
To download the zip go to the [release page](https://github.com/clickot/apl-db-attachments/releases).
To start the application unzip the zip file, enter the `apl-db-attachments-<version>` directory and execute `java -jar apl-db-attachments.jar` (or click it in your explorer).

> Note that you have to use a **Oracle JDK 1.8** both to build and to run this application
