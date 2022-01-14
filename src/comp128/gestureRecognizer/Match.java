package comp128.gestureRecognizer;

import java.util.Deque;

import edu.macalester.graphics.Point;

/**
 * Has name of mached gesture points, name, and the score
 */
public class Match{
    private String name;
    private double matchScore;
    private Deque<Point> points;

    public Match(String name, double matchScore, Deque<Point> points){
        this.name = name;
        this.matchScore = matchScore;
        this.points = points;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public String getName() {
        return name;
    }

    public Deque<Point> getPoints() {
        return points;
    }

}