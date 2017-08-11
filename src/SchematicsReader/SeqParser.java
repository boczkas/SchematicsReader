package SchematicsReader;

import org.bytedeco.javacpp.Loader;

import java.util.*;

import static org.bytedeco.javacpp.opencv_core.*;

public class SeqParser {
    private CvMemStorage allRectanglesStorage;
    private CvMemStorage componentNameRectanglesStorage;
    private CvSeq allRectangles;
    private CvSeq textRectangles;


    public SeqParser(CvSeq allRectangles) {
        this.allRectanglesStorage = cvCreateMemStorage(0);
        this.componentNameRectanglesStorage = cvCreateMemStorage(0);
        this.allRectangles = cvCloneSeq(allRectangles, allRectanglesStorage);
        this.textRectangles = cvCreateSeq(0, Loader.sizeof(CvSeq.class), Loader.sizeof(CvPoint.class), componentNameRectanglesStorage);
    }

    public CvSeq getAllRectangles() {
        return allRectangles;
    }

    public CvSeq getTextRectangles() {
        return textRectangles;
    }

    public void showAllRectanglesSequence(){
        CvPoint givenRectangle;
        CvSlice slice = new CvSlice(allRectangles);

        System.out.println("Rectangle sequence: ");
        int total = allRectangles.total();
        for(int i = 0; i < total; i += 4) {
            givenRectangle = new CvPoint(4);
            cvCvtSeqToArray(allRectangles, givenRectangle, slice.start_index(i).end_index(i + 4));

            System.out.println(" [" + i/4 + "]: " + givenRectangle);
        }
    }

    public void removeNotComponentContoursVertices(int imageWidth, int imageHeight) {
        ArrayList<Integer> verticesToBeDeleted = new ArrayList<>();

        verticesToBeDeleted.addAll(SeqParser.findNotComponentContours(allRectangles, imageWidth, imageHeight));
        verticesToBeDeleted = SeqParser.reverseSortAndRemoveDuplicatesFromArrayList(verticesToBeDeleted);

        RectangleParser.showRectanglesVertices(verticesToBeDeleted);

        SeqParser.removeIndexesFromSeq(allRectangles, verticesToBeDeleted);
    }

    static public ArrayList<Integer> findNotComponentContours(CvSeq squares, int imageWidth, int imageHeight){

        CvPoint givenRectangle;
        ArrayList<Integer> vertices = new ArrayList<>();
        CvSlice slice = new CvSlice(squares);
        int total = squares.total();
        for(int i = 0; i < total; i += 4) {
            givenRectangle = new CvPoint(4);

            cvCvtSeqToArray(squares, givenRectangle, slice.start_index(i).end_index(i + 4));
            System.out.println("Given rectangle w forze " + givenRectangle);
            System.out.println("Przemek wieksza dupa " + squares.total());

            if(RectangleParser.isInnerRectangle(squares, givenRectangle, imageWidth, imageHeight)){
                vertices.add(i);
                vertices.add(i + 1);
                vertices.add(i + 2);
                vertices.add(i + 3);
            }

            if(RectangleParser.isConnectedWithOthers(squares, givenRectangle, 10)){
                System.out.println("Przemek jest polaczony");
                vertices.add(i);
                vertices.add(i + 1);
                vertices.add(i + 2);
                vertices.add(i + 3);
            }
        }
        return vertices;
    }

    public void addRectangleToSequence(CvSeq squares, CvPoint a, CvPoint b, CvPoint c, CvPoint d) {
        cvSeqPush(squares, a);
        cvSeqPush(squares, b);
        cvSeqPush(squares, c);
        cvSeqPush(squares, d);
    }

    public void addRectangleToSequence(CvPoint a, CvPoint b, CvPoint c, CvPoint d) {
        cvSeqPush(allRectangles, a);
        cvSeqPush(allRectangles, b);
        cvSeqPush(allRectangles, c);
        cvSeqPush(allRectangles, d);
    }
    static public void removeIndexesFromSeq(CvSeq squares, ArrayList<Integer> vertices) {
        for(int i = 0; i < vertices.size(); i++){
            cvSeqRemove(squares,vertices.get(i));
        }
    }

    public void removeDuplicatesFromSequence() {

        ArrayList<Integer> vertices = new ArrayList<>();
        CvSlice slice = new CvSlice(this.allRectangles);

        for (int i = 0; i < this.allRectangles.total(); i += 4) {
            CvPoint firstRectangle = new CvPoint(4);
            cvCvtSeqToArray(this.allRectangles, firstRectangle, slice.start_index(i).end_index(i + 4));

            for (int j = i + 4; j < this.allRectangles.total(); j += 4) {
                CvPoint secondRectangle = new CvPoint(4);
                cvCvtSeqToArray(this.allRectangles, secondRectangle, slice.start_index(j).end_index(j + 4));

                if(RectangleParser.haveTheSameCoordinates(firstRectangle, secondRectangle)){
//                    System.out.println("Takie same: ");
//                    System.out.println("Pierwszy: " + firstRectangle);
//                    System.out.println("Drugi: " + secondRectangle);
                    vertices.add(j);
                    vertices.add(j + 1);
                    vertices.add(j + 2);
                    vertices.add(j + 3);
                }
            }
        }

        vertices = reverseSortAndRemoveDuplicatesFromArrayList(vertices);
        SeqParser.removeIndexesFromSeq(allRectangles, vertices);
    }

    static public ArrayList<Integer> reverseSortAndRemoveDuplicatesFromArrayList(ArrayList<Integer> vertices) {
        Set<Integer> s = new LinkedHashSet<>(vertices);
        vertices = new ArrayList<>(s);
        Collections.sort(vertices);
        Collections.reverse(vertices);
        return vertices;
    }

    public void addTextRectanglesToContoursRectangles() {
        CvPoint givenRectangle;
        CvPoint textRectangle;
        CvSlice slice = new CvSlice(allRectangles);

        int total = allRectangles.total();
        for(int i = 0; i < total; i += 4){
            givenRectangle = new CvPoint(4);

            cvCvtSeqToArray(allRectangles, givenRectangle, slice.start_index(i).end_index(i + 4));
            givenRectangle = (new RectangleParser(givenRectangle)).getOrderedCvPoint4();

            textRectangle = addTextRectangleForSingleRectangle(givenRectangle);

            addRectangleToSequence(allRectangles,
                    new CvPoint(textRectangle.position(0).x(), textRectangle.position(0).y()),
                    new CvPoint(textRectangle.position(1).x(), textRectangle.position(1).y()),
                    new CvPoint(textRectangle.position(2).x(), textRectangle.position(2).y()),
                    new CvPoint(textRectangle.position(3).x(), textRectangle.position(3).y())
            );

            addRectangleToSequence(textRectangles,
                    new CvPoint(textRectangle.position(0).x(), textRectangle.position(0).y()),
                    new CvPoint(textRectangle.position(1).x(), textRectangle.position(1).y()),
                    new CvPoint(textRectangle.position(2).x(), textRectangle.position(2).y()),
                    new CvPoint(textRectangle.position(3).x(), textRectangle.position(3).y())
            );
        }

        showAllRectanglesSequence();
        System.out.println("Text rectangles " + textRectangles.total());
        System.out.println("Total rectangles " + allRectangles.total());
    }

    private CvPoint addTextRectangleForSingleRectangle(CvPoint givenRectangle) {
        // can be upgrated by adding parameters instead of
        CvPoint textRectangle = new CvPoint(4);
        textRectangle.put(
          givenRectangle.position(3).x() + 25,givenRectangle.position(3).y() - 20,
                givenRectangle.position(3).x() + 25, givenRectangle.position(3).y() + 40,
                givenRectangle.position(3).x() + 200,givenRectangle.position(3).y() + 40,
                givenRectangle.position(3).x() + 200,givenRectangle.position(3).y() - 20
        );
        return textRectangle;
    }
}
