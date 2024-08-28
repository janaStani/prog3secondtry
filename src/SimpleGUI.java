import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SimpleGUI extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int CARPET_SIZE = 500;  // Desired size of the fractal

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Canvas canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        // Center the fractal
        double scale = CARPET_SIZE / (double) 27;  // Fractal grid size
        double offsetX = (WINDOW_WIDTH - CARPET_SIZE) / 2.0;
        double offsetY = (WINDOW_HEIGHT - CARPET_SIZE) / 2.0;

        // Read and render the fractal
        try (BufferedReader br = new BufferedReader(new FileReader("result.txt"))) {
            String line;
            int fractalSize = 27;  // The size of the fractal grid
            int y = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                for (int x = 0; x < values.length; x++) {
                    if (values[x].equals("1")) {
                        gc.fillRect(offsetX + x * scale, offsetY + y * scale, scale, scale);
                    }
                }
                y++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setTitle("Sierpiński Carpet");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
