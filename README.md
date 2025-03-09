# apl-db-attachments

Java tool to extract ticket attachments from a BMC Remedy Database. This is a refactoring of the Remedy Legacy Tool [DB Attachments](https://remedylegacy.com/tools/db-attachments/) to replace outdated dependencies, especially log4j 1.x. and Oracle JDK 1.8.
To build a release zip simply execute `mvn package`. You find the resulting zip file in the target directory.  
To download the zip go to the [release page](https://github.com/clickot/apl-db-attachments/releases).
To start the application unzip the zip file, enter the `apl-db-attachments-<version>` directory and execute `apl-db-attachments` 
or `apl-db-attachments.bat`, depending on your platform (or click it in your explorer).

>Note that you need at least a JDK 17 and a JavaFX SDK to start the application. Don't forget to set the PATH_TO_FX environment variable, pointing to [PATH-TO-JAVAFX-SDK]/lib. 
