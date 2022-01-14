package comp128.gestureRecognizer;

import edu.macalester.graphics.CanvasWindow;
import edu.macalester.graphics.Ellipse;
import edu.macalester.graphics.GraphicsGroup;
import edu.macalester.graphics.Point;

import java.security.cert.CertPath;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.sql.XADataSource;


import java.util.Iterator;

/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {

    private Deque<Template> templates;
 
    private static double RESAMPLE_POINTS = 64.0; //amount of points to be resampled into
    private static double SIZE = 250.0; //size to scale the resampled gesture

    /**
     * Constructs a recognizer object
     */
    public Recognizer(){
        templates = new ArrayDeque<Template>(); // creates new Templates when recognizer contructor is called
    }


    /**
     * Create a template to use for matching
     * @param name of the template
     * @param points in the template gesture's path
     */
    public void addTemplate(String name, Deque<Point> points){
        Template templatesAdd = new Template(name, processedPoints(points));
        templates.add(templatesAdd);
     
        System.out.println("Template added : " + name);
    }

    /**
     * 
     * @return templates points
     */
    public Deque<Template> getTemplates(){
        return templates;
    }

    /**
     * 
     * @param p to be processed (resample, rotate, scale, and translate)
     * @return a processed Deque points
     */
    public Deque<Point> processedPoints(Deque<Point> p){
        Deque<Point> r = resample(p,RESAMPLE_POINTS);
        Double indicativeAngle = indicativeAngle(r);
        Deque<Point> rT = rotateBy(r,-indicativeAngle);
        Deque<Point> sT = scaleTo(rT,SIZE);
        Deque<Point> tT = translateTo(sT, new Point(0,0));

        return tT;
    }


    /**
     * 
     * @param points points to be resampled
     * @param n no. of points to be resampled into
     * @return newly resampled points
     * Had to massively change this method from the last commit to address missing conditions comparison and removing duplicative/jargen line of codes (simplified)
     */
    public Deque<Point> resample (Deque<Point> points, double n){
  
        Deque<Point> resampledPoints = new ArrayDeque<>();
        double pathLength = pathLength(points);
        double resampleInterval = pathLength / (n - 1);
       
        double accumulatedDistance = 0;
        double segmentDistance = 0;

        Iterator<Point> itr = points.iterator();
        Point previousPoint = itr.next();
        Point currentPoint = itr.next();

        resampledPoints.add(previousPoint);
        
 
        while (itr.hasNext()) {
            segmentDistance = currentPoint.distance(previousPoint);

            if ((segmentDistance + accumulatedDistance) < resampleInterval) {
                accumulatedDistance += segmentDistance; // accumulated distance by adding previous point to current point
                previousPoint = currentPoint; // update the previous point
                currentPoint = itr.next(); // update the current points
            } else {  // segmentDistance + accumulatedDistance) > resampleInterval
                Point newPoint = Point.interpolate(previousPoint, currentPoint, (resampleInterval - accumulatedDistance) / segmentDistance);
                resampledPoints.add(newPoint); // add it to the resampled points
                previousPoint = newPoint; //update previous point to new point
                accumulatedDistance = 0; // make accumulated distance to zero
            }
        }
        if (segmentDistance < resampleInterval) {
            resampledPoints.add(points.getLast()); // if segment distance is less than the resample interval, add the last points of the original points to get the 64 points or the resample interval amount
        }
        return resampledPoints; // return resampled points
    }

    /**
     * 
     * @param points : to find its path lenght by looping by an iterator
     * @return total path length of these points
     */
    public double pathLength(Deque<Point> points){
  
        Iterator <Point> itr = points.iterator();
        double pthLth = 0.0; // path length set to 0 as default
        Point firstPoint = points.getFirst();

        while (itr.hasNext()){
            Point currentPoint = itr.next();
            pthLth += firstPoint.distance(currentPoint); // add the path length (first point and current distance)

            firstPoint = currentPoint; // first point is updated to current point
        }
 
        return pthLth;  // return path length
    }

 
    /**
     * 
     * @param points takes in deque of points to find the indicative angle using the centroid of the points
     * @return indicative angle 
     */
    public double indicativeAngle(Deque<Point> points){
        return centroidCalc(points).subtract(points.getFirst()).angle(); // angle calculated by the centroid of points and the the first point 
    }

    /**
     * 
     * @param points points to calculate its centroid
     * @return centroid point
     */
    public Point centroidCalc(Deque<Point> points){
        Double xSum = 0.0;
        Double ySum = 0.0;

        for(Point p: points){
            xSum += p.getX();
            ySum += p.getY();
        }

        Point centroid = new Point (xSum/points.size() , ySum/points.size());

        return centroid;
    }

    /**
     * 
     * @param resampled resampled points to be rotated
     * @param theta angle to ratate the points by
     * @return new points that is resampled and rotated to zero
     */
    public Deque<Point> rotateBy(Deque<Point> resampled, double theta){
        Deque<Point> rotatedPoints = new ArrayDeque<>();
        for(Point p : resampled){
            rotatedPoints.add(p.rotate(theta, centroidCalc(resampled)));
        }
        return rotatedPoints;
    }


    /**
     * 
     * @param rotatedP take the rotated points and scale it to a desired size
     */
    public Deque<Point> scaleTo(Deque<Point> rotatedP, double size){
        Deque<Point> scaledPoints = new ArrayDeque<>();
      
        Point boundingBox  = getBoundingSize(rotatedP);
        double width = boundingBox.getX(), height = boundingBox.getY();

        for(Point p : rotatedP){
            scaledPoints.add(new Point(p.getX()*(size/width),p.getY()*(size/height))); // adds the scaled points to the scaled deque
        }

        System.out.println("points scaled");

        return scaledPoints;
    }

    /**
     * 
     * @param rotatedP to get the bounding size
     * @return a point that contains bounding point (max - min)
     */
    public Point getBoundingSize(Deque<Point> rotatedP){

        Point max = rotatedP.getFirst();
        Point min = rotatedP.getFirst();

        for(Point r : rotatedP){
            max = Point.max(r, max);
            min = Point.min(r, min);
        }

        return max.subtract(min);
    }

    /**
     * 
     * @param scaled and translate it to the origin points as the centroid
     * @param p 
     * @return Deque of translated gesture
     */
    public Deque<Point> translateTo(Deque<Point> scaled, Point k){
        Point centroid = centroidCalc(scaled);
        Deque<Point> translatePoints = new ArrayDeque<>();

        for(Point p: scaled){
            translatePoints.add(new Point(p.getX() + k.getX() - centroid.getX(), p.getY() + k.getY() - centroid.getY()));
        }

        //System.out.println(centroidCalc(translatePoints));

        return translatePoints;
    }

    /**
     * 
     * @param gesturePoints
     * @return matched gesture points, name and score
     */
    public Match recognize(Deque<Point> gesturePoints){
        double distance = 0.0;
        //int count = 0;
        double minDistance = Double.MAX_VALUE;

        String name = new String();
        Deque<Point> mTemplate = new ArrayDeque<>();
        Deque<Point> g = processedPoints(gesturePoints);


        for(Template t : templates){
            distance = distanceAtBestAngle(g, t.getPoints()); 
            // System.out.println("Distance iteration" + count + " "+ distance);
            if(distance <= minDistance){
                minDistance = distance;
                name = t.getName();
                mTemplate = t.getPoints();

                System.out.println("minDistance : "+ minDistance);
                System.out.println("Temp Name : " + name);
            }

            //count++;
            System.out.println("Templates " + t.getName() + " with score: " + calcScore(minDistance));
        }

        // Below were test codes 

        //System.out.println("Rec:" + name);

        // System.out.println("Template explored : ");
        // for(Template t : templates){
        //     System.out.println(t.getName());
        // }
        
        return new Match(name, calcScore(minDistance), mTemplate);
    }

    public double calcScore(double minDistance){
        double score = 0.0;

        //score = 1 - ( minDistance / (0.5 * Math.sqrt(2 * SIZE * SIZE))  );
        score = 1 - (minDistance) /(Math.sqrt(0.5)*SIZE); 

        if(score < 0){
            return 0;
        } else{
            return score;
        }
    }
     
    /**
     * Uses a golden section search to calculate rotation that minimizes the distance between the gesture and the template points.
     * @param points
     * @param templatePoints
     * @return best distance
     */
    public double distanceAtBestAngle(Deque<Point> points, Deque<Point> templatePoints){
        double thetaA = -Math.toRadians(45);
        double thetaB = Math.toRadians(45);
        final double deltaTheta = Math.toRadians(2);
        double phi = 0.5*(-1.0 + Math.sqrt(5.0));// golden ratio
        double x1 = phi*thetaA + (1-phi)*thetaB;
        double f1 = distanceAtAngle(points, templatePoints, x1);
        double x2 = (1 - phi)*thetaA + phi*thetaB;
        double f2 = distanceAtAngle(points, templatePoints, x2);
        while(Math.abs(thetaB-thetaA) > deltaTheta){
            if (f1 < f2){
                thetaB = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi*thetaA + (1-phi)*thetaB;
                f1 = distanceAtAngle(points, templatePoints, x1);
            }
            else{
                thetaA = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1-phi)*thetaA + phi*thetaB;
                f2 = distanceAtAngle(points, templatePoints, x2);
            }
        }
        return Math.min(f1, f2);
    }

    /**
     * 
     * @param points
     * @param templatePoints
     * @param theta
     * @return path distance that is between the gesture points and template points
     */
    public double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta){
        Deque<Point> rotatedPoints = null;
        rotatedPoints = rotateBy(points, theta);
        return pathDistance(rotatedPoints, templatePoints);
    }

    /**
     * 
     * @param a point a 
     * @param b point b
     * @return its distance between point a and point b
     */
    public double pathDistance(Deque<Point> a, Deque<Point> b){
        double distance = 0.0;
        Iterator <Point> itrA = a.iterator();
        Iterator <Point> itrB = b.iterator();

        while(itrA.hasNext() && itrB.hasNext()){
            Point aPoints = itrA.next();
            Point bPoints = itrB.next();
            
            distance += aPoints.distance(bPoints);
            //System.out.println(distance);
        }

       return distance/a.size();
    }


}