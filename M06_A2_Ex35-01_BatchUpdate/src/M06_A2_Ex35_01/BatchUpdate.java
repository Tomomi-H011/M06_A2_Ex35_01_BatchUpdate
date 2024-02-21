/**
* Assignment: SDEV200_M06_A2_Ex35_01
* File: BatchUpdate.java
* Version: 1.0
* Date: 2/21/2024
* Author: Tomomi Hobara
* Description: This program shows a dialog box to connect to a database and creates a table on the database.
                Then, it updates the table with 1000 rows of random numbers through a batch update and individual updates.
                At the end, the elapsed times for the two update processes are displayed in the text area for a comparison. 
* Variables: 
    - txtStatus: Text object for displaying the status of updates.
    - taResult: TextArea for displaying the execution result.
    - btnBatchUpdate: Button to perform a batch update on the table.
    - btnNonBatchUpdate: Button to perform non-batch updates on the table.
    - txtDbInfo: Text object for displaying database connection information.
    - cboJdbcDriver: ComboBox for selecting JDBC driver.
    - cboDbUrl: ComboBox for selecting the database URL.
    - tfUsername: TextField for entering the database username.
    - pfPassword: PasswordField for entering the database password.
    - batchMessage: String for storing the execution result for the batch update.
    - nonBatchMessage: String for storing the execution result for the non-batch update.
* Steps:
    1. Create the GUI with JavaFX components.
    2. Set up the event handlers for pressing the buttons.
    3. Create the database connection dialog.
    4. Connect to the database, create the table, and insert 1000 rows.
    5. Activate the update buttons after the table has been created.
    6. Define the updateWithBatch method for batch updates.
    7. Define the updateWithNonBatch method for individual updates.
    8. Display the elapsed time for both batch and non-batch updates in the text area.
     
*/

package M06_A2_Ex35_01;

import java.sql.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;



public class BatchUpdate extends Application {
    // Fields for the first stage (SQL execution)
    private Text txtStatus = new Text("");
    private Button btnConnect = new Button("Connect to Database");
    private TextArea taResult = new TextArea("");
    private Button btnBatchUpdate = new Button("Batch Update");
    private Button btnNonBatchUpdate = new Button("Non-Batch Update");
    
    // Fields for the second stage (Database connection)
    private Text txtDbInfo = new Text("Connect to ");
    private ComboBox<String> cboJdbcDriver = new ComboBox<>();
    private ComboBox<String> cboDbUrl= new ComboBox<>();
    private TextField tfUsername = new TextField("");
    private PasswordField pfPassword = new PasswordField();
    private Label lblJdbcDriver = new Label("JDBC Drive");
    private Label lblUrl = new Label("Database URL");
    private Label lblUsername = new Label("Username");
    private Label lblPassword = new Label("Password");
    private Button btnConnectToDb = new Button("Connect to DB");
    private Button btnCloseDialog = new Button("Close Dialog");
    private Stage connectToDbStage;

    // Fields for connecting to SQL
    private Connection connection;
    private Statement stmt;
    private String tableName = "Temp";

    // Fields for TextArea to show SQL execution time
    private String batchMessage = "";
    private String nonBatchMessage = "";


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage)  {

        // Create a pane for text and button
        BorderPane paneTop = new BorderPane();
        paneTop.setPadding(new Insets(10, 10, 10, 10));
        paneTop.setLeft(txtStatus);
        paneTop.setRight(btnConnect);

        // Create a text area for displaying the execution result
        BorderPane paneForTextArea = new BorderPane();
        paneForTextArea.setPadding(new Insets(10, 10, 10, 10));
        paneForTextArea.setCenter(taResult);

        // Create a pane for buttons
        HBox paneBottom = new HBox();
        paneBottom.getChildren().addAll(btnBatchUpdate, btnNonBatchUpdate);
        paneBottom.setAlignment(Pos.CENTER);
        paneBottom.setSpacing(15);

        // Initially disable the buttons
        btnBatchUpdate.setDisable(true);
        btnNonBatchUpdate.setDisable(true);


        // Create a VBox to stack the two panes vertically
        VBox root = new VBox();
        root.getChildren().addAll(paneTop, paneForTextArea, paneBottom);
    
        // Create a scene and place it in the stage
        Scene scene = new Scene(root, 480, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Exercise35_01");
        primaryStage.show();

        btnConnect.setOnAction(e -> connectToDbStage());
        
        btnBatchUpdate.setOnAction(e -> {
            try {
                updateWithBatch(stmt, tableName);
            }
            catch (java.lang.Exception e0) {
                e0.printStackTrace();
            }
        });
        
        btnNonBatchUpdate.setOnAction(e -> {
            try {
                updateWithNonBatch(stmt, tableName);
            }
            catch (java.lang.Exception e1) {
                e1.printStackTrace();
            }
        });

    }

    // Create the Coonect to DB stage. Invoked when btnConnect on the first Stage is clicked to open the second stage.
    private void connectToDbStage() {
        HBox paneForDbStatus = new HBox();
        paneForDbStatus.setPadding(new Insets(10, 10, 10, 10));
        paneForDbStatus.getChildren().add(txtDbInfo);

        // Create a GridPane to put DB information
        GridPane paneForDBInfo = new GridPane();
        paneForDBInfo.setPadding(new Insets(10, 10, 10, 10));
        paneForDBInfo.setVgap(10);
        paneForDBInfo.setHgap(10);

        // Set width for the ComboBoxes
        cboJdbcDriver.setPrefWidth(350);
        cboDbUrl.setPrefWidth(350);

        // Put driver and url choices in the ComboBoxes
        cboJdbcDriver.getItems().addAll(FXCollections.observableArrayList(
        "com.mysql.jdbc.Driver", 
        "sun.jdbc.odbc.dbcOdbcDriver",
        "oracle.jdbc.driver.OracleDriver"));
        cboJdbcDriver.getSelectionModel().selectFirst();  // Select the first option in the ComboBox

        cboDbUrl.getItems().addAll(FXCollections.observableArrayList(
        "jdbc:mysql://localhost/javabook",
        "jdbc:mysql://liang.armstrong.edu/javabook",
        "jdbc:odbc:exampleMDBDataSource",
        "jdbc:oracle:thin:@liang.armstrong.edu:1521:orcl"));
        cboDbUrl.getSelectionModel().selectFirst();

        // Place nodes to pane
        paneForDBInfo.addRow(0, lblJdbcDriver, cboJdbcDriver);
        paneForDBInfo.addRow(1, lblUrl, cboDbUrl);
        paneForDBInfo.addRow(2, lblUsername, tfUsername);
        paneForDBInfo.addRow(3, lblPassword, pfPassword);

        // Create a pane for btnConnectToDb
        HBox paneForBtnConnectToDb = new HBox();
        paneForBtnConnectToDb.setAlignment(Pos.BASELINE_RIGHT);
        paneForBtnConnectToDb.setPadding(new Insets(10));
        paneForBtnConnectToDb.getChildren().add(btnConnectToDb);

        // Create a pane for btnCloseDialog
        HBox paneForBtnCloseDialog = new HBox();
        paneForBtnCloseDialog.setAlignment(Pos.CENTER);
        paneForBtnCloseDialog.setPadding(new Insets(10));
        paneForBtnCloseDialog.getChildren().add(btnCloseDialog);

        // Add panes to VBox
        VBox rootDB = new VBox();
        rootDB.getChildren().addAll(paneForDbStatus, paneForDBInfo, paneForBtnConnectToDb, paneForBtnCloseDialog);

        // Create a scene 
        Scene sceneConnectDB = new Scene(rootDB, 460, 300);
        
        // Create the second stage for database information
        connectToDbStage = new Stage();
        connectToDbStage.setScene(sceneConnectDB);  // Place scene in stage
        connectToDbStage.initModality(Modality.APPLICATION_MODAL);
        connectToDbStage.setTitle("Connect to DB");
        connectToDbStage.show();

        // Set actions for the buttons
        btnConnectToDb.setOnAction(e -> connectToDb());  // Connect to DB
        btnCloseDialog.setOnAction(e -> closeDialog());  // Close the second stage
    }


    // Connect to database
    private void connectToDb() {
        // Get information from the user input on the second stage
        String driver = cboJdbcDriver.getSelectionModel().getSelectedItem();
        String url = cboDbUrl.getSelectionModel().getSelectedItem();
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();
        
        
        // Connect to the database and create a table
        try {
            if (username.equals("")) {  // Show an alert when tfUsername is empty
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a username.");
                alert.showAndWait();
            } 
            else {
                // Connect to the database
                Class.forName(driver);  // Load JDBC driver
                connection = DriverManager.getConnection(url, username, password);  // Establish a connection
                txtDbInfo.setText("Connected to " + url);
                System.out.println("Database connected");

                // Create a statement
                stmt = connection.createStatement();

                // Check if table exists
                if (checkTableExists(connection, tableName)) {
                // Drop Temp table if it exists
                    dropTable(stmt, tableName);
                }

                // Create table
                createTable(stmt, tableName);

                // Insert 1000 rows with primary keys into the table
                insertData(stmt, tableName);

                // Enable the buttons after the table has been created
                btnBatchUpdate.setDisable(false);
                btnNonBatchUpdate.setDisable(false);
            }
        }

        catch (java.lang.Exception ex) {
        ex.printStackTrace();
        }
    }


    // Close the second stage when btnCloseDialog is clicked
    private void closeDialog() {
        connectToDbStage.close();
    }

    // Check if table exists
    public static boolean checkTableExists(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    // Drop table if exists
    public static void dropTable(Statement stmt, String tableName) throws SQLException {
        String dropTableStmt = "DROP TABLE " + tableName;
        stmt.executeUpdate(dropTableStmt);
        System.out.println(tableName + " dropped");
    }


    // Create a table
    private static void createTable(Statement stmt, String tableName) throws SQLException {
        String createTableStmt = "CREATE TABLE " + tableName + " (num1 double PRIMARY KEY, num2 double, num3 double)";
        
        stmt.executeUpdate(createTableStmt);
        System.out.println(tableName + " created");
    }    

    // Insert 1000 rows with primary keys into the table
    private static void insertData(Statement stmt, String tableName) throws SQLException {
            int rowsInserted = 0;
            for (int i = 1; i <= 1000; i++) {
                String insertRowsStmt = "INSERT INTO " + tableName + " (num1, num2, num3) VALUES (" + i + ", 0.0, 0.0)";
                stmt.executeUpdate(insertRowsStmt);
                rowsInserted += 1;
            }
            
        System.out.println(rowsInserted + " rows inserted");
    }
    
    // Update column num2 by batch
    private void updateWithBatch(Statement stmt, String tableName) {
        try {
            long startTimeBatch = System.currentTimeMillis();
    
            // Define String for the prepared statement
            String updateWithBatchStmt = "UPDATE " + tableName + " SET num2 = ? WHERE num1 = ?";
    
            // Create a prepared statement for the batch
            try (PreparedStatement psBatch = connection.prepareStatement(updateWithBatchStmt)) {
    
                // Get a result set for querying num1 column with primary keys
                ResultSet rs = stmt.executeQuery("SELECT num1 FROM " + tableName);
    
                while (rs.next()) {
                    double randomValue = Math.random() * 1001;  // Generate random values 1 through 1000
                    int primaryKey = rs.getInt("num1");
    
                    // Set the parameter values for each iteration of prepared statement
                    psBatch.setDouble(1, randomValue);
                    psBatch.setInt(2, primaryKey);
    
                    // Add the prepared statement to the batch for each row
                    psBatch.addBatch();
                }
    
                // Execute the batch
                psBatch.executeBatch();
            }
    
            long endTimeBatch = System.currentTimeMillis();
            long timeBatch = endTimeBatch - startTimeBatch;  // Get the elapsed time in milliseconds
    
            batchMessage = "\nBatch update completed" +
                    "\nThe elapsed time is " + timeBatch + " milliseconds";
    
            // Show the update status on the stage
            txtStatus.setText("Batch update succeeded");

            System.out.println(batchMessage);
    
            // Insert the message in the TextArea
            taResult.setText(batchMessage + "\n" + nonBatchMessage);
        } 
        
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update column num3 individually
    private void updateWithNonBatch(Statement stmt, String tableName) {
        try {
            long startTimeNonBatch = System.currentTimeMillis();
    
            // Get a result set for querying num1 column with primary keys
            ResultSet rs = stmt.executeQuery("SELECT num1 FROM " + tableName);
    
            // String for prepared statement
            String updateWithNonBatchStmt = "UPDATE " + tableName + " SET num3 = ? WHERE num1 = ?";
    
            // Use a prepared statement for the non-batch update
            try (PreparedStatement psNonBatch = connection.prepareStatement(updateWithNonBatchStmt)) {
    
                while (rs.next()) {
                    double randomValue = Math.random() * 1001;  // Generate random values 1 through 1000
                    int primaryKey = rs.getInt("num1");
    
                    // Set the parameter values for each iteration of prepared statement
                    psNonBatch.setDouble(1, randomValue);
                    psNonBatch.setInt(2, primaryKey);
    
                    // Execute the update for each row individually
                    psNonBatch.executeUpdate();
                }
            }
    
            long endTimeNonBatch = System.currentTimeMillis();
            long timeNonBatch = endTimeNonBatch - startTimeNonBatch;  // Get the elapsed time in milliseconds
    
            nonBatchMessage = "\nNon-Batch update completed" +
                    "\nThe elapsed time is " + timeNonBatch + " milliseconds";
    
            // Show the update status on the stage
            txtStatus.setText("Non-batch update succeeded");

            System.out.println(nonBatchMessage);
    
            // Insert the message in the TextArea
            taResult.setText(batchMessage + "\n" + nonBatchMessage);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}