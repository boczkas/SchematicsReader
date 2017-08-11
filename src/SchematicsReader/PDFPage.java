package SchematicsReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFPage {
    private String pdfInputFilePath;
    private String pngOutputFilePath;


    public PDFPage(String pdfInputFilePath, String pngOutputFilePath) throws IOException {
        this.pdfInputFilePath = pdfInputFilePath;
        this.pngOutputFilePath = pngOutputFilePath;
    }

    public void renderPDFtoRGBImage() throws IOException {
        PDDocument pdDocument = PDDocument.load(new File(pdfInputFilePath));
        PDFRenderer renderer = new PDFRenderer(pdDocument);
        BufferedImage image = renderer.renderImageWithDPI(0, 600, ImageType.RGB);
        ImageIOUtil.writeImage(image, pngOutputFilePath, 600);
        pdDocument.close();
    }


    public String getPdfInputFilePath() {
        return pdfInputFilePath;
    }

    public String getPngOutputFilePath() {
        return pngOutputFilePath;
    }

}
