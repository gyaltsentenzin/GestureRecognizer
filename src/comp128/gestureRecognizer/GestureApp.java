package comp128.gestureRecognizer;

import edu.macalester.graphics.*;
import edu.macalester.graphics.events.MouseButtonEvent;
import edu.macalester.graphics.events.MouseMotionEvent;
import edu.macalester.graphics.ui.Button;
import edu.macalester.graphics.ui.TextField;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * The window and user interface for drawing gestures and automatically recognizing them
 * Created by bjackson on 10/29/2016.
 */
public class GestureApp {

    private CanvasWindow canvas;
    private Recognizer recognizer; // instantiate recognizer object to carry out recognizer 
    private IOManager ioManager;
    private GraphicsGroup uiGroup;
    private Button addTemplateButton;
    private TextField templateNameField;
    private GraphicsText matchLabel;
    private Deque<Point> path; //for the drawn points

    public GestureApp(){
        canvas = new CanvasWindow("Gesture Recognizer", 600, 600);
        recognizer = new Recognizer();
        path = new ArrayDeque<>();
        ioManager = new IOManager();
        setupUI();
    }

    /**
     * Create the user interface
     */
    private void setupUI(){
        matchLabel = new GraphicsText("Match: ");
        matchLabel.setFont(FontStyle.PLAIN, 24);
        canvas.add(matchLabel, 10, 30);

        uiGroup = new GraphicsGroup();

        templateNameField = new TextField();

        addTemplateButton = new Button("Add Template");
        addTemplateButton.onClick( () -> addTemplate() );

        Point center = canvas.getCenter();
        double fieldWidthWithMargin = templateNameField.getSize().getX() + 5;
        double totalWidth = fieldWidthWithMargin + addTemplateButton.getSize().getX();

 
        uiGroup.add(templateNameField, center.getX() - totalWidth/2.0, 0);
        uiGroup.add(addTemplateButton, templateNameField.getPosition().getX() + fieldWidthWithMargin, 0);
        canvas.add(uiGroup, 0, canvas.getHeight() - uiGroup.getHeight());

        Consumer<Character> handleKeyCommand = ch -> keyTyped(ch);
        canvas.onCharacterTyped(handleKeyCommand);

        draw(path); // draws the path by invoking mouse down event

        /**
         * This mouse up event check if there are templates stored or the path (line drawn) on canvas.
         * This leads to matching the gesture on the canvas to be matched with available gestures 
         * templates in the list of templates.
         * It sets the lable with the name of the matched gesture, and the score it gets with the matched gestures.
         */
        canvas.onMouseUp(event->{ 
            if(path.size() !=0 && recognizer.getTemplates().size()!=0){
                System.out.println(recognizer.getTemplates());
                Match matching = recognizer.recognize(path);
                String name = matching.getName();
                double scoreNo = matching.getMatchScore();
                
                matchLabel.setText("Match: " + name + "= " + scoreNo);
            }
        }); 

 
        /**
         * when the mouse is clicked, 
         * it removes the previous graphics points but preserves UI objects
         * calls the removeAllNonUIGraphicsObjects method
         */
        canvas.onMouseDown(event->{ 
            removeAllNonUIGraphicsObjects();
        });


    }

    /**
     * This method draws the line on canvas
     * When drawing line, it connects the line from the previous points and thus creates
     * consistent line for drawing gestures.
     * @param dPoints is the deque to collect/store the points in the line
     */
    private void draw(Deque<Point> dPoints) {
        canvas.onDrag(event->{ // when dragging it, it does the actions below
        Line gLine = new Line(event.getPreviousPosition().getX(),  // previous points with the new points combination makes it a uniform/continuous line
                            event.getPreviousPosition().getY(), 
                            event.getPosition().getX(),
                            event.getPosition().getY()
                            );
            dPoints.add(gLine.getPosition()); // adds it into the Deque 
            canvas.add(gLine); // adds it to the canvas after creating the gesture 
        });
    }


    /**
     * Clears the canvas, but preserves all the UI objects
     */
    private void removeAllNonUIGraphicsObjects() {
        canvas.removeAll();
        path.clear(); // clearing it is important because it will not lead to adding duplicate gesture points which mess up gesture recognition

        canvas.add(matchLabel);
        canvas.add(uiGroup);
    }

    /**
     * Handle what happens when the add template button is pressed. This method adds the points stored in path as a template
     * with the name from the templateNameField textbox. If no text has been entered then the template is named with "no name gesture"
     */
    private void addTemplate() {
        String name = templateNameField.getText();
        if (name.isEmpty()){
            name = "no name gesture";
        }
        recognizer.addTemplate(name, path); // Add the points stored in the path as a template

    }

    /**
     * Handles keyboard commands used to save and load gestures for debugging and to write tests.
     * Note, once you type in the templateNameField, you need to call canvas.requestFocus() in order to get
     * keyboard events. This is best done in the mouseDown callback on the canvas.
     */
    public void keyTyped(Character ch) {
        if (ch.equals('L')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            Deque<Point> points = ioManager.loadGesture(name+".xml");
            if (points != null){
                recognizer.addTemplate(name, points);
                System.out.println("Loaded "+name);
            }
        }
        else if (ch.equals('s')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            ioManager.saveGesture(path, name, name+".xml");
            System.out.println("Saved "+name);
        }
    }

    public static void main(String[] args){
        GestureApp window = new GestureApp();
    }
}
