package SchematicsReader;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.IOException;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

public class SchematicsReader {

    IplImage img = null;
    IplImage img0 = null;

    String wndname = "Schematics Reader";

    CanvasFrame canvas = null;
    OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();


    public static void main(String args[]) throws Exception {
        new SchematicsReader().main();
    }

    public void main() throws IOException, InterruptedException {
        int widthMargin = 110;
        int heightMargin = 1550;

        String imageFilePath = "C:/Users/przem/workspace/SchematicsReader/cos5.png";
        PDFPage pdfPage = new PDFPage("C:/Users/przem/workspace/SchematicsReader/cos.pdf",
                                 "C:/Users/przem/workspace/SchematicsReader/cos2.png");

        Component.setOrginalCutMarginImage(pdfPage.getPngOutputFilePath(), widthMargin, heightMargin);

        pdfPage.renderPDFtoRGBImage();
        PNGParser pngParser = new PNGParser(pdfPage.getPngOutputFilePath()); //("C:/Users/przem/workspace/SchematicsReader/cos11.png");
        pngParser.parsePNGtoIntArray();



        pngParser.convertToRGBFromData(imageFilePath, widthMargin, heightMargin);
        prepareScene(imageFilePath);

        CvSeq squares = pngParser.findSquares4(img);

        SeqParser seqParser = new SeqParser(squares);
        seqParser.removeDuplicatesFromSequence();

        seqParser.showAllRectanglesSequence();
        seqParser.removeNotComponentContoursVertices(img.width(), img.height());
//        seqParser.addRectangleToSequence(new CvPoint(100, 100), new CvPoint(100, 200), new CvPoint(200, 200), new CvPoint(200, 100));
        seqParser.addTextRectanglesToContoursRectangles();

        ArrayList<Component> components = createComponents(seqParser);








        // zaciagnac calego rectangla i dorobic wykrywanie polaczen









        Component.setParsedImage(img);
        pngParser.drawSquares(img, seqParser.getAllRectangles(), canvas, converter, 0, 255, 0,
                "C:/Users/przem/workspace/SchematicsReader/abc1.png");

        System.out.println("original");
        Thread.sleep(3000);

        pngParser.drawSquares(prepareScene(Component.getOrginalCutMarginFilePath()), seqParser.getAllRectangles(), canvas, converter, 255,0,0,
                "C:/Users/przem/workspace/SchematicsReader/abc2.png");

        //pngParser.drawSquares(Component.getOrginalImage(), seqParser.getAllRectangles(), canvas, converter, 0, 255, 0);

//        System.out.println("parsed");
//        Thread.sleep(3000);
//        pngParser.drawSquares(Component.getParsedImage(), seqParser.getAllRectangles(), canvas, converter, 0, 255, 0);

    }

    private ArrayList<Component> createComponents(SeqParser seqParser) throws IOException {
        ArrayList<Component> components = new ArrayList<>();

        CvSeq textRectangles = seqParser.getTextRectangles();
        CvSeq allRectangles = seqParser.getAllRectangles();

        CvPoint textRectangle;
        CvPoint bodyRectangle;

        CvSlice allRectanglesSlice = new CvSlice(allRectangles);
        CvSlice textRectanglesSlice = new CvSlice(textRectangles);

        System.out.println("Rectangle sequence: ");
        int total = textRectangles.total();
        for(int i = 0; i < total; i += 4) {
            bodyRectangle = new CvPoint(4);
            textRectangle = new CvPoint(4);

            cvCvtSeqToArray(allRectangles, bodyRectangle, allRectanglesSlice.start_index(i).end_index(i + 4));
            cvCvtSeqToArray(textRectangles, textRectangle, textRectanglesSlice.start_index(i).end_index(i + 4));

            System.out.println("TextRectangle");
            System.out.println(" [" + i/4 + "]: " + textRectangle);

            System.out.println("allRectangles");
            System.out.println(" [" + i/4 + "]: " + bodyRectangle);

            components.add(new Component(bodyRectangle, textRectangle));
        }

        for(Component component : components){
            System.out.println("Component: ");
            System.out.println(component.getBodyRectangleCoordinates());
            System.out.println(component.getNameTextRectangleCoordinates());
            component.readComponentNameFromPNG();
            component.extractBodyRectangleFromPNG();
            System.out.println(component.getName());
            component.findComponentPins();
        }

        return components;
    }

    private IplImage prepareScene(String filePathAndName) {
        img0 = cvLoadImage(filePathAndName, 1);

        if(img0 == null){
            System.err.println("Couldn't load file");
        }

        img = cvCloneImage(img0);
        canvas = new CanvasFrame(wndname, 1);
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        return img0;
    }
}