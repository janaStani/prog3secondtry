import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class ViewController {

    // Fields to store the scale and translation transformations
    private final Scale scaleTransform;
    private final Translate translateTransform;

    // Store initial coordinates of mouse pointer and view translation for drag operation
    private double initialX, initialY;
    private double startX, startY;

    // Constructor
    public ViewController(Scale scaleTransform, Translate translateTransform) {
        this.scaleTransform = scaleTransform;
        this.translateTransform = translateTransform;
    }

    // Method to adjust the scale of the drawing pane based on a zoom factor
    public void zoom(double factor) {
        // Retrieve the current horizontal and vertical scale factor and multiply it by the zoom factor
        scaleTransform.setX(scaleTransform.getX() * factor);
        scaleTransform.setY(scaleTransform.getY() * factor);
    }

    // Method to reset the scale and position of the drawing pane to the default values
    public void resetView(double sceneWidth, double sceneHeight) {
        // Set scale back to default (1.0 for both X and Y)
        scaleTransform.setX(1);
        scaleTransform.setY(1);

        // Calculate the distances needed to center the drawing pane
        double offsetX = (sceneWidth - SimpleGUI.WINDOW_WIDTH) / 2.0;
        double offsetY = (sceneHeight - SimpleGUI.WINDOW_HEIGHT) / 2.0;

        // Set translation to center the fractal
        translateTransform.setX(offsetX);
        translateTransform.setY(offsetY);
    }

    // Method initializes the drag operation
    public void startDrag(MouseEvent event) {
        // Get the x and y coordinates of the mouse pointer in the scene when the drag starts; this will be the initial position
        initialX = event.getSceneX();
        initialY = event.getSceneY();

        // Store the x and y coordinates of the current translation of the view; this will be the starting point of the drag operation
        startX = translateTransform.getX();
        startY = translateTransform.getY();

        ((Scene) event.getSource()).setCursor(Cursor.CLOSED_HAND); // Set cursor to hand when dragging starts
    }

    // Method updates the view's translation based on the mouse movement during the drag operation
    public void drag(MouseEvent event) {
        // Calculate the x and y offset of the mouse pointer from the initial position
        double offsetX = event.getSceneX() - initialX;
        double offsetY = event.getSceneY() - initialY;

        // Update the translation of the view based on the calculated offset
        translateTransform.setX(startX + offsetX);
        translateTransform.setY(startY + offsetY);
    }
}
