package SchematicsReader;

import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.CvSlice;

import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.cvCvtSeqToArray;

public class RectangleParser {

    private CvPoint rectangle;
    private CvPoint a;
    private CvPoint b;
    private CvPoint c;
    private CvPoint d;

    public RectangleParser(CvPoint givenRectangle) {
        this.a = new CvPoint(givenRectangle.position(0).x(), givenRectangle.position(0).y());
        this.b = new CvPoint(givenRectangle.position(1).x(), givenRectangle.position(1).y());
        this.c = new CvPoint(givenRectangle.position(2).x(), givenRectangle.position(2).y());
        this.d = new CvPoint(givenRectangle.position(3).x(), givenRectangle.position(3).y());

        orderPoints();
    }

    private void orderPoints() {
        Integer maxX;
        Integer minX;
        Integer maxY;
        Integer minY;

        maxX = findMaxFrom4Values(a.x(), b.x(), c.x(), d.x());
        maxY = findMaxFrom4Values(a.y(), b.y(), c.y(), d.y());
        minX = findMinFrom4Values(a.x(), b.x(), c.x(), d.x());
        minY = findMinFrom4Values(a.y(), b.y(), c.y(), d.y());

        a.x(minX);
        a.y(minY);

        b.x(minX);
        b.y(maxY);

        c.x(maxX);
        c.y(maxY);

        d.x(maxX);
        d.y(minY);

        rectangle = new CvPoint(4);
        rectangle.put(a.x(), a.y(), b.x(), b.y(), c.x(), c.y(), d.x(), d.y());
    }

    public Integer findMaxFrom4Values(Integer first, Integer second, Integer third, Integer fourth){
        return Integer.max((Integer.max(first, second)),Integer.max(third, fourth));
    }

    public Integer findMinFrom4Values(Integer first, Integer second, Integer third, Integer fourth){
        return Integer.min((Integer.min(first, second)),Integer.min(third, fourth));
    }

    public CvPoint getOrderedCvPoint4(){
        return this.rectangle;
    }

    public CvPoint getA() { return a; }

    public CvPoint getB() {
        return b;
    }

    public CvPoint getC() {
        return c;
    }

    public CvPoint getD() {
        return d;
    }

    public static boolean haveTheSameCoordinates(CvPoint firstRectangle, CvPoint secondRectangle) {
        if( (firstRectangle.position(0).x() == secondRectangle.position(0).x()) &&
            (firstRectangle.position(1).x() == secondRectangle.position(1).x()) &&
            (firstRectangle.position(2).x() == secondRectangle.position(2).x()) &&
            (firstRectangle.position(3).x() == secondRectangle.position(3).x()) &&
            (firstRectangle.position(0).y() == secondRectangle.position(0).y()) &&
            (firstRectangle.position(1).y() == secondRectangle.position(1).y()) &&
            (firstRectangle.position(2).y() == secondRectangle.position(2).y()) &&
            (firstRectangle.position(3).y() == secondRectangle.position(3).y())){
            return true;
        }
        return false;
    }

    public static boolean isInnerRectangle(CvSeq squares, CvPoint rectangle, int imageWidth, int imageHeight) {
        CvSlice slice = new CvSlice(squares);
        CvPoint givenRectangle = rectangle;

        for(int i = 0; i < squares.total(); i += 4) {
            CvPoint rect = new CvPoint(4);
            cvCvtSeqToArray(squares, rect, slice.start_index(i - 4).end_index(i));
            rect = (new RectangleParser(rect)).getOrderedCvPoint4();
            givenRectangle = (new RectangleParser(givenRectangle)).getOrderedCvPoint4();

            if(     (givenRectangle.position(0).x() > imageWidth) || (givenRectangle.position(1).x() > imageWidth) || (givenRectangle.position(2).x() > imageWidth) || (givenRectangle.position(3).x() > imageWidth) ||
                    (givenRectangle.position(0).y() > imageHeight) || (givenRectangle.position(1).y() > imageHeight) || (givenRectangle.position(2).y() > imageHeight) || (givenRectangle.position(3).y() > imageHeight) ||
                    (givenRectangle.position(0).x() < 0) || (givenRectangle.position(1).x() < 0) || (givenRectangle.position(2).x()< 0) || (givenRectangle.position(3).x() < 0) ||
                    (givenRectangle.position(0).y() < 0) || (givenRectangle.position(1).y() < 0) || (givenRectangle.position(2).y()< 0) || (givenRectangle.position(3).y() < 0)){
                System.out.println("Rectangle outside the picture");
                return true;
            }

            if(     (rect.position(0).x() > imageWidth) || (rect.position(1).x() > imageWidth) || (rect.position(2).x() > imageWidth) || (rect.position(3).x() > imageWidth) ||
                    (rect.position(0).y() > imageHeight) || (rect.position(1).y() > imageHeight) || (rect.position(2).y() > imageHeight) || (rect.position(3).y() > imageHeight) ||
                    (rect.position(0).x() < 0) || (rect.position(1).x() < 0) || (rect.position(2).x()< 0) || (rect.position(3).x() < 0) ||
                    (rect.position(0).y() < 0) || (rect.position(1).y() < 0) || (rect.position(2).y()< 0) || (rect.position(3).y() < 0)){
                System.out.println("Rectangle outside the picture");
                return true;
            }
            else{
                if(  (givenRectangle.position(0).x() > rect.position(0).x()) && (givenRectangle.position(0).y() > rect.position(0).y()) &&
                        (givenRectangle.position(1).x() > rect.position(1).x()) && (givenRectangle.position(1).y() < rect.position(1).y()) &&
                        (givenRectangle.position(2).x() < rect.position(2).x()) && (givenRectangle.position(2).y() < rect.position(2).y()) &&
                        (givenRectangle.position(3).x() < rect.position(3).x()) && (givenRectangle.position(3).y() > rect.position(3).y())){

                    System.out.println("IF Given rectangle " + givenRectangle);
                    System.out.println("IF Current rectangle " + rect);
                    System.out.println("Przemek Przemek Przemek Przemek");

                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isConnectedWithOthers(CvSeq squares, CvPoint givenRectangle, int toleranceInPixels) {
        CvPoint currentRectangle;

        currentRectangle = (new RectangleParser(givenRectangle)).getOrderedCvPoint4();
        CvSlice slice = new CvSlice(squares);

        for(int i = 0; i < squares.total(); i += 4) {
            CvPoint firstRectangle = new CvPoint(4);
            cvCvtSeqToArray(squares, firstRectangle, slice.start_index(i - 4).end_index(i));
            firstRectangle = (new RectangleParser(firstRectangle)).getOrderedCvPoint4();

            int topDifference;
            int bottomDifference;
            int total = squares.total();

            for(int j = 0; j < total; j += 4) {
                CvPoint secondRectangle = new CvPoint(4);
                cvCvtSeqToArray(squares, secondRectangle, slice.start_index(j - 4).end_index(j));
                secondRectangle = (new RectangleParser(secondRectangle)).getOrderedCvPoint4();

                topDifference = Math.abs(currentRectangle.position(0).y() - firstRectangle.position(1).y());
                bottomDifference = Math.abs(currentRectangle.position(1).y() - secondRectangle.position(0).y());

                if(topDifference < toleranceInPixels && bottomDifference < toleranceInPixels){
                    System.out.println("Taka roznica top: " + topDifference + "  bot: " + bottomDifference);
                }

                if((topDifference < toleranceInPixels) && (bottomDifference < toleranceInPixels) &&
                        (currentRectangle.position(0).x() > firstRectangle.position(0).x()) &&
                        (currentRectangle.position(2).x() < firstRectangle.position(2).x()) &&
                        (currentRectangle.position(0).x() > secondRectangle.position(0).x()) &&
                        (currentRectangle.position(2).x() < secondRectangle.position(2).x())){
                    System.out.println("Bedziemy usuwac j " + j + " i " + i);
                    System.out.println("First: " + firstRectangle);
                    System.out.println("Second: " + secondRectangle);
                    System.out.println("Current: " + currentRectangle);
                    return true;
                }
            }
        }
        return false;
    }

    public static void showRectanglesVertices(ArrayList<Integer> vertices) {
        int size = vertices.size();
        for(int j = 0; j < size; j++){
            if(0 == (j%4)){
                System.out.println();
            }
            System.out.print(" vertice[" + j + "] = " + vertices.get(j));
        }
        System.out.println();
    }

}
