package SchematicsReader;

import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Component {
    // Component name (from OCR)
    // Body rectangle
    // Text rectangle
    // Connections

    private static BufferedImage originalCutMarginImage;
    private static String orginalCutMarginFilePath;
    private static IplImage parsedImage;
    private String name;
    private CvPoint bodyRectangleCoordinates;
    private CvPoint nameTextRectangleCoordinates;
    private List<ComponentConnection> componentConnections;
    private static CvSeq squares;

    public Component() {
    }

    public Component(CvPoint bodyRectangleCoordinates, CvPoint nameTextRectangleCoordinates){
        this("default", bodyRectangleCoordinates, nameTextRectangleCoordinates);
    }

    public Component(String name, CvPoint bodyRectangleCoordinates, CvPoint nameTextRectangleCoordinates) {
        this.name = name;
        this.bodyRectangleCoordinates = bodyRectangleCoordinates;
        this.nameTextRectangleCoordinates = nameTextRectangleCoordinates;
        this.componentConnections = new ArrayList<ComponentConnection>();
    }

    public CvPoint getBodyRectangleCoordinates() {
        return bodyRectangleCoordinates;
    }

    public CvPoint getNameTextRectangleCoordinates() {
        return nameTextRectangleCoordinates;
    }

    public String getName() { return name; }

    public static BufferedImage getOriginalCutMarginImage() { return originalCutMarginImage; }

    public static String getOrginalCutMarginFilePath() { return orginalCutMarginFilePath; }

    public static void setOrginalCutMarginImage(String originalImagePath, int widthMargin, int heightMargin) throws IOException {
        PNGParser pngParser = new PNGParser(originalImagePath);
        orginalCutMarginFilePath = "C:/Users/przem/workspace/SchematicsReader/orginalCutMargin.png";
        pngParser.convertPNGtoIntArray();
        pngParser.convertToRGBFromData(orginalCutMarginFilePath, widthMargin, heightMargin);
        originalCutMarginImage = ImageIO.read(new File(orginalCutMarginFilePath));
    }

    public static IplImage getParsedImage() { return parsedImage; }

    public static void setParsedImage(IplImage parsedImage) {
        Component.parsedImage = parsedImage;
    }

    public void readComponentNameFromPNG() throws IOException {
        // odhardcodowac to, trzeba tu jakos przekazywac/ujednolicic nazwy
        // dorobic tworzenie folderu jesli nie istnieje
        PNGParser.extractAndSaveSliceOfPNG("C:/Users/przem/workspace/SchematicsReader/cos2.png",
                                "C:/Users/przem/workspace/SchematicsReader/Components/" + nameTextRectangleCoordinates + ".png",
                nameTextRectangleCoordinates);
        this.name = PNGParser.doOCRonPNG("C:/Users/przem/workspace/SchematicsReader/Components/" + nameTextRectangleCoordinates + ".png");
    }

    public void extractBodyRectangleFromPNG() throws IOException {
        System.out.println("bodyCoordinates: " + bodyRectangleCoordinates);
        CvPoint rectangleForPNGSlice = (new RectangleParser(bodyRectangleCoordinates)).getOrderedCvPoint4();

        rectangleForPNGSlice.position(0).x(rectangleForPNGSlice.position(0).x() - 30);
        rectangleForPNGSlice.position(0).y(rectangleForPNGSlice.position(0).y() - 40);
        rectangleForPNGSlice.position(1).x(rectangleForPNGSlice.position(1).x() - 30);
        rectangleForPNGSlice.position(1).y(rectangleForPNGSlice.position(1).y() + 40);
        rectangleForPNGSlice.position(2).x(rectangleForPNGSlice.position(2).x() + 30);
        rectangleForPNGSlice.position(2).y(rectangleForPNGSlice.position(2).y() + 40);
        rectangleForPNGSlice.position(3).x(rectangleForPNGSlice.position(3).x() + 30);
        rectangleForPNGSlice.position(3).y(rectangleForPNGSlice.position(3).y() - 40);
        System.out.println("bodyCoordinates: " + rectangleForPNGSlice);

        PNGParser.extractAndSaveSliceOfPNG("C:/Users/przem/workspace/SchematicsReader/cos2.png",
                "C:/Users/przem/workspace/SchematicsReader/Components/" + name + ".png",
                rectangleForPNGSlice);
    }

    public void findComponentPins(){
        PNGParser pngParser = new PNGParser(originalCutMarginImage);
        pngParser.convertPNGtoIntArray();

        int aX = bodyRectangleCoordinates.position(0).x();
        int aY = bodyRectangleCoordinates.position(0).y() - 13;

        int cX = bodyRectangleCoordinates.position(2).x();
        int cY = bodyRectangleCoordinates.position(2).y();

        int cos = pngParser.findIndexOfFirstBlackPixelInSequence(aX, cX, aY, 5, 10);

        System.out.println("Pierwszy index " + cos);
    }

}
