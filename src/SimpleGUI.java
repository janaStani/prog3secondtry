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

    public static final int WINDOW_WIDTH = 800;    // 800x600 window size
    public static final int WINDOW_HEIGHT = 600;
    public static final int CANVAS_SIZE = 4000;  // virtual canvas size
    public static final int DISPLAY_SIZE = 500;  // display size

    private final Scale scaleTransform = new Scale(1, 1, 0, 0); // zoom in/out
    private final Translate translateTransform = new Translate(0, 0);  // move the canvas
    private ViewController viewController; // view controller class

    @Override
    public void start(Stage primaryStage) {

        Canvas canvas = new Canvas(DISPLAY_SIZE, DISPLAY_SIZE); // 500x500 canvas to draw the fractal
        GraphicsContext gc = canvas.getGraphicsContext2D();     // get the graphics context

        // apply transformations to the canvas, scale and translate
        canvas.getTransforms().addAll(scaleTransform, translateTransform);

        int gridSize = 0; // initialize grid size

        // read from file to get the grid size
        try (BufferedReader br = new BufferedReader(new FileReader("result.txt"))) {
            String line = br.readLine();                         // read first line
            if (line != null) {
                gridSize = line.trim().split(" ").length;  // get grid size from the first line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        double scale = (double) CANVAS_SIZE / gridSize;  // scale virtual canvas to fit the grid size

        // compute offsets to center the fractal within the display area
        double offsetX = (CANVAS_SIZE - gridSize * scale) / 2.0;  // Centering offset for X
        double offsetY = (CANVAS_SIZE - gridSize * scale) / 2.0;  // Centering offset for Y

        // scale canvas down to fit the display window
        double displayScale = (double) DISPLAY_SIZE / CANVAS_SIZE;

        // render the fractal on the larger virtual canvas size
        gc.setImageSmoothing(true);  // enables anti-aliasing
        gc.save();  // save the current state of the GraphicsContext
        gc.scale(displayScale, displayScale);  // scale down to fit the display size

        // read the file
        try (BufferedReader br = new BufferedReader(new FileReader("result.txt"))) {
            String line;
            int y = 0;       // current row
            while ((line = br.readLine()) != null) {
                String[] values = line.trim().split(" ");        // split data in array of strings
                for (int x = 0; x < values.length; x++) {
                    if (values[x].equals("1")) {     // if value is 1, draw a black square
                        gc.fillRect(offsetX + x * scale, offsetY + y * scale, scale, scale);
                        // upper-left corner (x,y), width, height
                    }
                }
                y++;    // go to next row
            }
        } catch (IOException e) {    // exception handling
            e.printStackTrace();
        }
        gc.restore();  // restore the GraphicsContext to remove scaling after drawing

        // toolbar with zoom and reset buttons
        ToolBar toolBar = new ToolBar();
        Button zoomInButton = new Button("Zoom In");
        Button zoomOutButton = new Button("Zoom Out");
        Button resetButton = new Button("Reset View");

        // initialize ViewController with the scale and translate transformations
        viewController = new ViewController(scaleTransform, translateTransform);

        // set actions for buttons
        zoomInButton.setOnAction(e -> viewController.zoom(1.2)); // zoom in by factor 1.2
        zoomOutButton.setOnAction(e -> viewController.zoom(0.8)); // zoom out by factor 0.8
        resetButton.setOnAction(e -> viewController.resetView(WINDOW_WIDTH, WINDOW_HEIGHT)); // reset view

        toolBar.getItems().addAll(zoomInButton, zoomOutButton, resetButton); // add buttons to toolbar

        // arrange components using BorderPane for the toolbar and StackPane for overall layout
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar);  // place toolbar at the top

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(canvas, borderPane); // canvas at the bottom, BorderPane on top

        Scene scene = new Scene(stackPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        // enable mouse dragging for panning
        scene.setOnMousePressed(viewController::startDrag);  // handle mouse press event
        scene.setOnMouseReleased(e -> scene.setCursor(Cursor.DEFAULT));  // handle mouse release event
        scene.setOnMouseDragged(viewController::drag);  // handle mouse drag event

        primaryStage.setScene(scene);
        primaryStage.setTitle("Sierpinski Carpet");
        primaryStage.show();
    }

    // launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
