package SchematicsReader;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class PNGParser {

    private BufferedImage originalImage;
    private BufferedImage outputImage;
    private String pngPath;
    private int valueOfBlackPixel = -16777216;
    private int [][] pngPixelsValues;
    CvMemStorage storage = null;
    static int widthMargin = 0;
    static int heightMargin = 0;


    PNGParser(String pngPath) throws IOException {
        this.pngPath = pngPath;
        originalImage = ImageIO.read(new File(pngPath));
        this.storage = cvCreateMemStorage(0);
    }

    PNGParser(BufferedImage image) {
        originalImage = image;
    }


    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public String getPngPath() {
        return pngPath;
    }

    public BufferedImage getOutputImage() {
        return outputImage;
    }

    /**
     * Method does parsing of schematic PNG to internal pngPixelsValues int array
     * After it there is no holes in components contours
     * @throws IOException
     */

    public void parsePNGtoIntArray() throws IOException {

        pngPixelsValues = convertToDataUsingGetRGB(originalImage);


        fillHorizontalHolesInComponentsImages(pngPixelsValues);
        fillVerticalHolesInComponentsImages(pngPixelsValues);


        System.out.println("Data file created");
        System.out.println(pngPixelsValues[0].length);
        System.out.println(pngPixelsValues.length);
    }

    /**
     * Converts PNG to Int array WITHOUT any parsing
     */
    public void convertPNGtoIntArray(){
        pngPixelsValues = convertToDataUsingGetRGB(originalImage);
    }

    private void fillHorizontalHolesInComponentsImages(int[][] pngPixelsValues) {
//        String dataFileOutputPath = "C:/Users/przem/workspace/SchematicsReader/dateHorizontal.txt";
//        StringBuilder builder = new StringBuilder();
        int blackPixelsInHorizontal = 0;
        boolean goingDownHorizontalSequence = false;

        for (int i = 0; i < pngPixelsValues.length; i++){
            for (int j = 0; j < pngPixelsValues[i].length; j++){

                if (-1 == pngPixelsValues[i][j]){
                    if(blackPixelsInHorizontal > 1){
                        pngPixelsValues[i][j] = valueOfBlackPixel;
                        blackPixelsInHorizontal--;
                        goingDownHorizontalSequence = true;
                    }
                    if ((blackPixelsInHorizontal <= 1) && goingDownHorizontalSequence && (j > 7)){
                        changeValueOfLastHorizontalPixels(-1, i, j, 7);
                    }
                }
                else{
                    if(blackPixelsInHorizontal < 8){
                        blackPixelsInHorizontal++;
                    }
                    goingDownHorizontalSequence = false;
                    pngPixelsValues[i][j] = valueOfBlackPixel;
                }
                // builder.append(pngPixelsValues[i][j] + " ");
            }
            blackPixelsInHorizontal = 0;
            // builder.append("\n");
        }
//        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFileOutputPath));
//        writer.write(builder.toString());
//        writer.close();
//
// You can uncomment writing to file for debugging purpose
    }

    private void fillVerticalHolesInComponentsImages(int[][] pngPixelsValues) {
//        String dataFileOutputPath = "C:/Users/przem/workspace/SchematicsReader/dateVertical.txt";
//        StringBuilder builder = new StringBuilder();
        int blackPixelsInVertical = 0;
        boolean goingDownVerticalSequence = false;

        for (int i = 0; i <  pngPixelsValues[i].length; i++){
            for (int j = 0; j < pngPixelsValues.length; j++){

                if (-1 == pngPixelsValues[j][i]){
                    if(blackPixelsInVertical > 1){
                        pngPixelsValues[j][i] = valueOfBlackPixel;
                        blackPixelsInVertical--;
                        goingDownVerticalSequence = true;
                    }
                    if ((blackPixelsInVertical <= 1) && goingDownVerticalSequence && (j > 7)){
                        changeValueOfLastVerticalPixels(-1, i, j, 7);
                    }
                }
                else{
                    if(blackPixelsInVertical < 8){
                        blackPixelsInVertical++;
                    }
                    goingDownVerticalSequence = false;
                    pngPixelsValues[j][i] = valueOfBlackPixel;
                }
                // builder.append(pngPixelsValues[i][j] + " ");
            }
            blackPixelsInVertical = 0;
            // builder.append("\n");
        }
        //        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFileOutputPath));
//        writer.write(builder.toString());
//        writer.close();

        // You can uncomment writing to file for debugging purpose
    }

    private void changeValueOfLastHorizontalPixels(int value, int rowNr, int startingColumnNr, int amountOfPixelsToBeChanged) {
        for(int i = 0; i < amountOfPixelsToBeChanged; i++){
            pngPixelsValues[rowNr][startingColumnNr - i] = value;
        }
    }

    private void changeValueOfLastVerticalPixels(int value, int columnNr, int startingRowNr, int amountOfPixelsToBeChanged) {
        for(int i = 0; i < amountOfPixelsToBeChanged; i++){
            pngPixelsValues[startingRowNr - i][columnNr] = value;
        }
    }

    /**
     * Converts BufferedImage image to int array
     * It does not do any parsing
     * @param image
     * @return
     */
    private int[][] convertToDataUsingGetRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = image.getRGB(col, row);
            }
        }

        return result;
    }

    /**
     * Converts RGB int values to RBG image
     * @param outputPath output path of the image file
     * @param widthMargin width margin of the data which shouldn't be used in conversion
     * @param heightMargin height margin of the data which shouldn't be used in conversion
     * @throws IOException
     */
    public void convertToRGBFromData(String outputPath,  int widthMargin, int heightMargin) throws IOException { //String dataFilePath,

        // margin has been introduced to delete frame
        outputImage = new BufferedImage(originalImage.getWidth() - widthMargin * 2, originalImage.getHeight() - heightMargin * 2, originalImage.getType());

        System.out.println(originalImage.getWidth() + "   " + originalImage.getHeight());
        for (int i = heightMargin; i < pngPixelsValues.length - heightMargin; i++){
            for (int j = widthMargin; j < pngPixelsValues[i].length - widthMargin ; j++){
                outputImage.setRGB(j - widthMargin, i - heightMargin, pngPixelsValues[i][j]);
            }
        }
        ImageIOUtil.writeImage(outputImage, outputPath, 600);
        PNGParser.widthMargin = widthMargin;
        PNGParser.heightMargin = heightMargin;
    }

    CvSeq findSquares4(IplImage img) {
        // Java translation: moved into loop
        // CvSeq contours = new CvSeq();

        int i, c, l, N = 2;
        int thresh = 20;

        CvSize sz = cvSize(img.width() & -2, img.height() & -2);
        IplImage timg = cvCloneImage(img); // make a copy of input image
        IplImage gray = cvCreateImage(sz, 8, 1);
        IplImage pyr = cvCreateImage(cvSize(sz.width()/2, sz.height()/2), 8, 3);
        IplImage tgray = null;
        // Java translation: moved into loop
        // CvSeq result = null;
        // double s = 0.0, t = 0.0;

        // create empty sequence that will contain points -
        // 4 points per square (the square's vertices)
        CvSeq squares = cvCreateSeq(0, Loader.sizeof(CvSeq.class), Loader.sizeof(CvPoint.class), storage);

        // select the maximum ROI in the image
        // with the width and height divisible by 2
        cvSetImageROI(timg, cvRect(0, 0, sz.width(), sz.height()));

        // down-scale and upscale the image to filter out the noise

        cvPyrDown(timg, pyr, 7);
        cvPyrUp(pyr, timg, 7);
        tgray = cvCreateImage(sz, 8, 1);

        // find squares in every color plane of the image
        for (c = 0; c < 3; c++) {
            // extract the c-th color plane
            cvSetImageCOI(timg, c+1);
            cvCopy(timg, tgray);

            // try several threshold levels
            for (l = 0; l < N; l++) {
                // hack: use Canny instead of zero threshold level.
                // Canny helps to catch squares with gradient shading
                if (l == 0) {
                    // apply Canny. Take the upper threshold from slider
                    // and set the lower to 0 (which forces edges merging)
                    cvCanny(tgray, gray, 4, thresh, 5);
                    // dilate canny output to remove potential
                    // holes between edge segments
                    cvDilate(gray, gray, null, 1);
                } else {
                    // apply threshold if l!=0:
                    //     tgray(x,y) = gray(x,y) < (l+1)*255/N ? 255 : 0
                    cvThreshold(tgray, gray, (l+1)*255/N, 255, CV_THRESH_BINARY);
                }

                // find contours and store them all as a list
                // Java translation: moved into the loop
                CvSeq contours = new CvSeq();
                cvFindContours(gray, storage, contours, Loader.sizeof(CvContour.class), CV_RETR_LIST , CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));

                // test each contour
                while (contours != null && !contours.isNull()) {
                    // approximate contour with accuracy proportional
                    // to the contour perimeter
                    // Java translation: moved into the loop
                    CvSeq result = cvApproxPoly(contours, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, cvContourPerimeter(contours)*0.02, 0);
                    // square contours should have 4 vertices after approximation
                    // relatively large area (to filter out noisy contours)
                    // and be convex.
                    // Note: absolute value of an area is used because
                    // area may be positive or negative - in accordance with the
                    // contour orientation
                    if(result.total() == 4 && Math.abs(cvContourArea(result, CV_WHOLE_SEQ, 0)) > 2000 && cvCheckContourConvexity(result) != 0) {

                        // Java translation: moved into loop
                        double s = 0.0, t = 0.0;

                        for( i = 0; i < 5; i++ ) {
                            // find minimum angle between joint
                            // edges (maximum of cosine)
                            if( i >= 2 ) {
                                //      Java translation:
                                //          Comment from the HoughLines.java sample code:
                                //          "    Based on JavaCPP, the equivalent of the C code:
                                //                  CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
                                //                  CvPoint first=line[0];
                                //                  CvPoint second=line[1];
                                //          is:
                                //                  Pointer line = cvGetSeqElem(lines, i);
                                //                  CvPoint first = new CvPoint(line).position(0);
                                //                  CvPoint second = new CvPoint(line).position(1);
                                //          "
                                //          ... so after some trial and error this seem to work
//                                t = fabs(angle(
//                                        (CvPoint*)cvGetSeqElem( result, i ),
//                                        (CvPoint*)cvGetSeqElem( result, i-2 ),
//                                        (CvPoint*)cvGetSeqElem( result, i-1 )));
                                t = Math.abs(angle(new CvPoint(cvGetSeqElem(result, i)),
                                        new CvPoint(cvGetSeqElem(result, i-2)),
                                        new CvPoint(cvGetSeqElem(result, i-1))));
                                s = s > t ? s : t;
                            }
                        }

                        // if cosines of all angles are small
                        // (all angles are ~90 degree) then write quandrange
                        // vertices to resultant sequence
                        if (s < 0.1)
                            for( i = 0; i < 4; i++ ) {
                                cvSeqPush(squares, cvGetSeqElem(result, i));
                            }
                    }

                    // take the next contour

                    contours = contours.h_next();
                }
            }
        }

        // release all the temporary images
        cvReleaseImage(gray);
        cvReleaseImage(pyr);
        cvReleaseImage(tgray);
        cvReleaseImage(timg);

        return squares;
    }

    double angle(CvPoint pt1, CvPoint pt2, CvPoint pt0){
        double dx1 = pt1.x() - pt0.x();
        double dy1 = pt1.y() - pt0.y();
        double dx2 = pt2.x() - pt0.x();
        double dy2 = pt2.y() - pt0.y();

        return (dx1*dx2 + dy1*dy2) / Math.sqrt((dx1*dx1 + dy1*dy1) * (dx2*dx2 + dy2*dy2) + 1e-10);
    }

    public void drawSquares(IplImage img, CvSeq squares, CanvasFrame canvas, OpenCVFrameConverter.ToIplImage converter, int r, int g, int b, String savePath) throws InterruptedException {
        //      Java translation: Here the code is somewhat different from the C version.
        //      I was unable to get straight forward CvPoint[] arrays
        //      working with "reader" and the "CV_READ_SEQ_ELEM".

//        CvSeqReader reader = new CvSeqReader();

        IplImage cpy = cvCloneImage(img);
        int i = 0;

        // Used by attempt 3
        // Create a "super"-slice, consisting of the entire sequence of squares
        CvSlice slice = new CvSlice(squares);

        // initialize reader of the sequence
//        cvStartReadSeq(squares, reader, 0);

        // read 4 sequence elements at a time (all vertices of a square)
        for(i = 0; i < squares.total(); i += 4) {

//              // Attempt 1:
//              // This does not work, uses the "reader"
//              CvPoint pt[] = new CvPoint[]{new CvPoint(1), new CvPoint(1), new CvPoint(1), new CvPoint(1)};
//              PointerPointer rect = new PointerPointer(pt);
//              int count[] = new int[]{4};
//
//              CV_READ_SEQ_ELEM(pt[0], reader);
//              CV_READ_SEQ_ELEM(pt[1], reader);
//              CV_READ_SEQ_ELEM(pt[2], reader);
//              CV_READ_SEQ_ELEM(pt[3], reader);

//              // Attempt 2:
//              // This works, somewhat similar to the C code, somewhat messy, does not use the "reader"
//              CvPoint pt[] = new CvPoint[]{
//                      new CvPoint(cvGetSeqElem(squares, i)),
//                      new CvPoint(cvGetSeqElem(squares, i + 1)),
//                      new CvPoint(cvGetSeqElem(squares, i + 2)),
//                      new CvPoint(cvGetSeqElem(squares, i + 3))};
//              PointerPointer rect = new PointerPointer(pt);
//              int count[] = new int[]{4};

            // Attempt 3:
            // This works, may be the "cleanest" solution, does not use the "reader"
            CvPoint rect = new CvPoint(4);
            IntPointer count = new IntPointer(1).put(4);
            // get the 4 corner slice from the "super"-slice
            cvCvtSeqToArray(squares, rect, slice.start_index(i).end_index(i + 4));

//             // Attempt 4:
//             // This works, and look the most like the original C code, uses the "reader"
//             CvPoint rect = new CvPoint(4);
//             int count[] = new int[]{4};
//
//             // read 4 vertices
//             CV_READ_SEQ_ELEM(rect.position(0), reader);
//             CV_READ_SEQ_ELEM(rect.position(1), reader);
//             CV_READ_SEQ_ELEM(rect.position(2), reader);
//             CV_READ_SEQ_ELEM(rect.position(3), reader);

            // draw the square as a closed polyline
            // Java translation: gotcha (re-)setting the opening "position" of the CvPoint sequence thing
            cvPolyLine(cpy, rect.position(0), count, 1, 1, CV_RGB(r, g, b), 2, CV_AA, 0);

        }

        // show the resultant image
        // cvShowImage(wndname, cpy);
        cvSaveImage(savePath, cpy);

        canvas.showImage(converter.convert(cpy));

//        Thread.sleep(3000);
//
//        canvas.showImage(converter.convert(img));
        cvReleaseImage(cpy);
    }

    public static String doOCRonPNG(String pngPath){ //

        System.out.println("Do OCRa " + pngPath);
        File imageFile = new File(pngPath); //this.getPngPath()
        ITesseract instance = new Tesseract();
        instance.setDatapath("C:/Users/przem/workspace/SchematicsReader/Tess4J");

        try{
            System.out.println("Probuje cos znalezc");
            String result = instance.doOCR(imageFile);
            result = result.replace("\n", "");
            return result;
        } catch (TesseractException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void doOCRonPNG() {
        PNGParser.doOCRonPNG(this.getPngPath());
    }

    public static void extractAndSaveSliceOfPNG(String inputPngPath, String outputSlicePath, CvPoint sliceRectangle) throws IOException {
        BufferedImage inputImage = ImageIO.read(new File(inputPngPath));
        BufferedImage outputImage;
        RectangleParser rectangleParser = new RectangleParser(sliceRectangle);

        int minX = rectangleParser.getA().x();
        int minY = rectangleParser.getA().y();

        int maxX = rectangleParser.getC().x();
        int maxY = rectangleParser.getC().y();

        outputImage = inputImage.getSubimage(minX + widthMargin, minY + heightMargin, maxX - minX, maxY - minY);
        ImageIOUtil.writeImage(outputImage, outputSlicePath , 600);
    }

    public int findIndexOfFirstBlackPixelInSequence(int minX, int maxX, int Y, int minimumSequenceLength, int maximumSequenceLength) {

        int sequenceLength = 0;
        int i;

        for( i = minX; i < maxX; i++){
            if(pngPixelsValues[Y][i] == valueOfBlackPixel){
                sequenceLength++;
            }
            if ((pngPixelsValues[Y][i] != valueOfBlackPixel) && (sequenceLength > 0)){
                break;
            }
        }
        System.out.println("Przemek minimumSequenceLength " + minimumSequenceLength);
        System.out.println("Przemek maximumSequenceLength " + maximumSequenceLength);
        System.out.println("Przemek sequenceLength " + sequenceLength);
        System.out.println("Przemek i " + i);
        if((minimumSequenceLength < sequenceLength) && (sequenceLength < maximumSequenceLength)){
            return i - sequenceLength;
        }
        return -1;
    }
}
