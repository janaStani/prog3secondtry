import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class ViewController {

    // store the scale and translation transformations
    private final Scale scaleTransform;
    private final Translate translateTransform;

    // store initial coordinates of mouse pointer and view translation for drag operation
    private double initialX, initialY;
    private double startX, startY;

    // constructor
    public ViewController(Scale scaleTransform, Translate translateTransform) {
        this.scaleTransform = scaleTransform;
        this.translateTransform = translateTransform;
    }

    // method to adjust the scale of the drawing pane based on zoom factor
    public void zoom(double factor) {
        // retrieve the current horizontal and vertical scale factor and multiply it by the zoom factor
        scaleTransform.setX(scaleTransform.getX() * factor);
        scaleTransform.setY(scaleTransform.getY() * factor);
    }

    // method to reset the scale and position of the drawing pane to the default
    public void resetView(double sceneWidth, double sceneHeight) {
        scaleTransform.setX(1);
        scaleTransform.setY(1);

        // calculate the distances needed to center the drawing pane
        double offsetX = (sceneWidth - SimpleGUI.WINDOW_WIDTH) / 2.0;
        double offsetY = (sceneHeight - SimpleGUI.WINDOW_HEIGHT) / 2.0;

        // set translation to center the fractal
        translateTransform.setX(offsetX);
        translateTransform.setY(offsetY);
    }

    // method initializes the drag operation
    public void startDrag(MouseEvent event) {
        // get x, y coordinates of mouse pointer in scene when drag starts (initial position)
        initialX = event.getSceneX();
        initialY = event.getSceneY();

        // store x, y coordinates of current translation of view (starting point for drag)
        startX = translateTransform.getX();
        startY = translateTransform.getY();

        ((Scene) event.getSource()).setCursor(Cursor.CLOSED_HAND); // set cursor to hand when dragging starts
    }

    // method update view's translation based on mouse movement during the drag operation
    public void drag(MouseEvent event) {
        // calculate x, y offset of mouse pointer from initial position
        double offsetX = event.getSceneX() - initialX;
        double offsetY = event.getSceneY() - initialY;

        // update translation of view based on calculated offset
        translateTransform.setX(startX + offsetX);
        translateTransform.setY(startY + offsetY);
    }
}
