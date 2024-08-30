import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.Cursor;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SimpleGUI extends Application {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final int CANVAS_SIZE = 4000;  // Increased canvas size for higher resolution
    public static final int DISPLAY_SIZE = 500;  // Display size remains the same

    private final Scale scaleTransform = new Scale(1, 1, 0, 0);
    private final Translate translateTransform = new Translate(0, 0);
    private ViewController viewController;

    @Override
    public void start(Stage primaryStage) {
        // Create the drawing Pane for fractal (Canvas)
        Canvas canvas = new Canvas(DISPLAY_SIZE, DISPLAY_SIZE); // Display size canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Apply transformations to the canvas
        canvas.getTransforms().addAll(scaleTransform, translateTransform);

        // Read the fractal data to determine the grid size
        int gridSize = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("result.txt"))) {
            String line = br.readLine();
            if (line != null) {
                gridSize = line.trim().split(" ").length;  // Get grid size from the first line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate the correct scale factor to fit the fractal within the desired display size
        double scale = (double) CANVAS_SIZE / gridSize;  // Scale to fill larger canvas

        // Compute offsets to center the fractal within the display area
        double offsetX = (CANVAS_SIZE - gridSize * scale) / 2.0;  // Centering offset for X
        double offsetY = (CANVAS_SIZE - gridSize * scale) / 2.0;  // Centering offset for Y

        // Scale to fit within the display size
        double displayScale = (double) DISPLAY_SIZE / CANVAS_SIZE;

        // Render the fractal on the larger virtual canvas size
        gc.save();  // Save the current state of the GraphicsContext
        gc.scale(displayScale, displayScale);  // Scale down to fit the display size
        try (BufferedReader br = new BufferedReader(new FileReader("result.txt"))) {
            String line;
            int y = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.trim().split(" ");
                for (int x = 0; x < values.length; x++) {
                    if (values[x].equals("1")) {
                        // Draw each rectangle at the scaled position on the virtual canvas
                        gc.fillRect(offsetX + x * scale, offsetY + y * scale, scale, scale);
                    }
                }
                y++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        gc.restore();  // Restore the GraphicsContext to remove scaling after drawing

        // Create the toolbar with zoom and reset buttons
        ToolBar toolBar = new ToolBar();
        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
        Button resetButton = new Button("Reset View");

        // Initialize ViewController with the scale and translate transformations
        viewController = new ViewController(scaleTransform, translateTransform);

        // Set actions for buttons
        zoomInButton.setOnAction(e -> viewController.zoom(1.2)); // zoom in by factor 1.2
        zoomOutButton.setOnAction(e -> viewController.zoom(0.8)); // zoom out by factor 0.8
        resetButton.setOnAction(e -> viewController.resetView(WINDOW_WIDTH, WINDOW_HEIGHT)); // reset view

        toolBar.getItems().addAll(zoomInButton, zoomOutButton, resetButton); // add buttons to toolbar

        // Arrange components using BorderPane for the toolbar and StackPane for overall layout
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar);  // place toolbar at the top

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(canvas, borderPane); // Canvas at the bottom, BorderPane on top

        Scene scene = new Scene(stackPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Enable mouse dragging for panning
        scene.setOnMousePressed(viewController::startDrag);  // handle mouse press event
        scene.setOnMouseReleased(e -> scene.setCursor(Cursor.DEFAULT));  // handle mouse release event
        scene.setOnMouseDragged(viewController::drag);  // handle mouse drag event

        primaryStage.setScene(scene);
        primaryStage.setTitle("Sierpinski Carpet");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
