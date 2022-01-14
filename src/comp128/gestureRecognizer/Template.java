package comp128.gestureRecognizer;

import java.util.Deque;
import edu.macalester.graphics.*;

/**
 * This class have String name and Deque Point for gesture that are processed and to be stored in its object instantiated in 
 * the recognizer class.
 */
public class Template {
    private String name;
    private Deque<Point> points;

    public Template(String name, Deque<Point> points){
        this.name = name;
        this.points = points;
    }

    public Deque<Point> getPoints(){
        return points;
    }

    public String getName(){
        return name;
    }
}
