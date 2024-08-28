import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;




public class SimpleGUI extends Application {
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        // Read the result from the file
        String result;
        try (BufferedReader br = new BufferedReader(new FileReader("result.txt"))) {
            result = "Global Sum: " + br.readLine();
        } catch (IOException e) {
            result = "Error reading result";
        }

        // Display the result
        Label resultLabel = new Label(result);
        root.getChildren().add(resultLabel);

        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.setTitle("MPI Result");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
