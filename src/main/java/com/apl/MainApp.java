package com.apl;

import com.apl.db.DBConnection;
import com.apl.db.Exporter;
import com.apl.controller.MainOverviewController;
import com.apl.controller.RootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

public class MainApp extends Application {
    // common constants
    public static final String CONFIG_FILE = "apl-db-attachments.properties";
    public static final String LOG4J_FILE = "log4j.properties";
    public static final String SYSTEM_PROP_LOG4J_FILE = "log4j2.configurationFile";
    // property names in config file
    public static final String PROP_USERNAME = "UserName";
    public static final String PROP_PASSWORD = "Password";
    public static final String PROP_CONNECTION_STRING = "ConnectionString";
    public static final String PROP_OUTPUT_DIRECTORY = "OutputDirectory";
    public static final String PROP_FORM_LIST = "FormList";
    public static final String PROP_FIELD_LIST = "FieldList";
    public static final String PROP_WHERE_CLAUSE = "Where";
    public static final String PROP_APP_VERSION = "AppVersion";

    private static final String APP_NAME = "APL DB Attachments";

    static {
        System.setProperty(SYSTEM_PROP_LOG4J_FILE, LOG4J_FILE);
    }
    private static final Logger logger = LogManager.getLogger(MainApp.class);

    private static final PropertiesConfiguration config = readConfigProperties();

    private static boolean gui = true;
    private static String version;

    private MainOverviewController mainOverviewController;
    private Stage primaryStage;
    private RootLayoutController rootController;
    private BorderPane rootLayout;

    public static void main(String[] args) {
        version = config.getString(PROP_APP_VERSION, "1.0.0");

        logger.info("{} {} loaded", APP_NAME, version);

        String userName = config.getString(PROP_USERNAME);
        String password = config.getString(PROP_PASSWORD);
        String connectionString = config.getString(PROP_CONNECTION_STRING);
        String outputDir = config.getString(PROP_OUTPUT_DIRECTORY);
        String formArray = config.getString(PROP_FORM_LIST);
        String fieldArray = config.getString(PROP_FIELD_LIST);
        String whereClause = config.getString(PROP_WHERE_CLAUSE);

        boolean formFolder = false;
        boolean entryIDFolder = false;
        boolean fieldNameFolder = false;

        for (int i = 0; i < args.length && args[i].startsWith("-"); ) {
            String arg = args[i++];
            if (arg.equals("-clm")) {
                gui = false;
            } else if ("-u".equals(arg)) {
                userName = getOption(args, i++);
            } else if ("-p".equals(arg)) {
                password = getOption(args, i++);
            } else if ("-cs".equals(arg)) {
                connectionString = getOption(args, i++);
            } else if ("-od".equals(arg)) {
                outputDir = getOption(args, i++);
            } else if ("-formlist".equals(arg)) {
                formArray = getOption(args, i++);
            } else if ("-fieldlist".equals(arg)) {
                fieldArray = getOption(args, i++);
            } else if ("-formFile".equals(arg)) {
                formFolder = false;
                entryIDFolder = false;
                fieldNameFolder = false;
            } else if ("-entryIDFolder".equals(arg)) {
                formFolder = true;
                entryIDFolder = true;
            } else if ("-fieldNameFolder".equals(arg)) {
                formFolder = true;
                entryIDFolder = true;
                fieldNameFolder = true;
            } else if ("-where".equals(arg)) {
                whereClause = getOption(args, i++);
            } else {
                printUsageAndExit("Unknown parameter '" + arg + "'", 1);
            }
        }

        try {
            if (isGui()) {
                Application.launch(MainApp.class, args);
            } else {
                try {
                    new Exporter(connectionString, userName, password).export(outputDir, formArray, fieldArray, whereClause, formFolder, entryIDFolder, fieldNameFolder);
                } catch (SQLException | IOException e) {
                    logger.error("error in attachment export", e);
                    System.exit(1);
                }
            }
        } finally {
            logger.info("finally closing db connection");
            DBConnection.getInstance(connectionString, userName, password).close();
        }
    }

    public static String getAppName() {
        return APP_NAME;
    }

    public static String getVersion() {
        return version;
    }

    public static boolean isGui() {
        return gui;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_NAME);
        initRootLayout();
        showMainOverview();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void handleBuildConnectionString() {
        rootController.handleBuildConnectionString();
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();
            rootController = loader.getController();
            rootController.setMainApp(this);
            primaryStage.setScene(new Scene(rootLayout));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("error in initRootLayout()", e);
        }
    }

    public void loadConfig() {
        logger.info("Loading Configuration Settings");
        mainOverviewController.setUserName(new TextField(config.getString(PROP_USERNAME)));
        mainOverviewController.setPassword(new TextField(config.getString(PROP_PASSWORD)));
        mainOverviewController.setConnectionString(new TextField(config.getString(PROP_CONNECTION_STRING)));
    }

    public void saveConfig() {
        logger.info("Saving Configuration Settings");
        String userName = mainOverviewController.getUserName();
        if (userName != null && !userName.isEmpty()) {
            config.setProperty("UserName", userName);
        }
        String password = mainOverviewController.getPassword();
        if (password != null && !password.isEmpty()) {
            config.setProperty("Password", password);
        }
        String connectionString = mainOverviewController.getConnectionString();
        if (connectionString != null && !connectionString.isEmpty()) {
            config.setProperty("ConnectionString", connectionString);
        }
        try {
            config.write(new FileWriter(CONFIG_FILE));
        } catch (ConfigurationException | IOException e) {
            logger.error("error in saving configuration file " + CONFIG_FILE, e);
        }
    }

    public void showMainOverview() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/MainOverview.fxml"));
        AnchorPane mainOverview = loader.load();
        rootLayout.setCenter(mainOverview);
        mainOverviewController = loader.getController();
        mainOverviewController.setMainApp(this);
    }

    public void updateConnectionDetails(TextField userName, TextField password, TextField connectionString) {
        mainOverviewController.setUserName(userName);
        mainOverviewController.setPassword(password);
        mainOverviewController.setConnectionString(connectionString);
    }


    private static void printUsageAndExit(String message, int status) {
        if (message != null) {
            System.out.println(message + "\n");
        }
        System.out.println("java -jar apl-db-attachment.jar" + System.lineSeparator());
        System.out.println("All Command line parameters are optional, remembering if a parameter has a space, it must be surrounded by \"'s");
        System.out.println("-clm: start in non gui mode as a client program");
        System.out.println("-u: User name to connect to Database with");
        System.out.println("-p: Password to connect with");
        System.out.println("-cs: Connection string to connect to DB");
        System.out.println("-od: Output Directory, can be absolute or relative to current path");
        System.out.println("-formlist: Comma separated list of forms to pull attachments from");
        System.out.println("-fieldlist: Comma separated list of field names fields to pull attachments from");
        System.out.println("Output Files: By default the output will be that a folder for each form will be created in the directory");
        System.out.println("\tand inside that folder will be a file named for the entry id, and field name and file name from the");
        System.out.println("\tsource record, so HPD_HelpDesk\\HPD000000000123-AttField-FileName.txt.");
        System.out.println("\tYou have the choice of providing any of the below options to change things that would have");
        System.out.println("\tbeen folders into part of the file name, or vis versa");
        System.out.println("-formFile: If you want the form name to be part of the file name");
        System.out.println("-entryIDFolder: If you want the EntryID to be a folder, this will force the Form to be a folder");
        System.out.println("-fieldNameFolder: If you want the field name to be a folder, this will force both Form and EntryID to be folders");
        System.out.println("-where: If specified, this needs to be a syntactically correct where qualification (without the word where) and it");
        System.out.println("-\twill be applied to the query for the form(s) specified");

        System.exit(status);
    }

    private static String getOption(String[] args, int index) {
        if (index < args.length) {
            return args[index];
        }
        printUsageAndExit(null, 1);
        return null;
    }

    private static PropertiesConfiguration readConfigProperties() {
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class).configure(
                new org.apache.commons.configuration2.builder.fluent.Parameters().properties()
                        .setFileName(CONFIG_FILE)
                        .setEncoding("UTF-8")
                        .setBasePath(Paths.get("").toAbsolutePath().toString())
            );
        try {
            return (PropertiesConfiguration) builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Error opening config file '{}': {}", CONFIG_FILE, e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
