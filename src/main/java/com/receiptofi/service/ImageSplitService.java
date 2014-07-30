package com.receiptofi.service;

import com.receiptofi.domain.shared.UploadReceiptImage;
import com.receiptofi.utils.CreateTempFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;

import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 10/18/13 10:58 PM
 */
@Service
public final class ImageSplitService {
    private static final Logger log = LoggerFactory.getLogger(ImageSplitService.class);

    //TODO remove main
    public static void main(String[] args) throws IOException {
        ImageSplitService splitService = new ImageSplitService();

        File file = new File("/Users/hitender/Downloads/" + "20130429_171952.jpg"); // I have bear.jpg in my working directory
        File image = splitService.decreaseResolution(file);
        splitService.splitImage(image);
    }

    public void splitImage(File file) throws IOException {
        BufferedImage image = bufferedImage(file);
        log.debug("W: " + image.getWidth() + ", " + "H: " + image.getHeight());

        splitImage(image);
    }

    private void splitImage(BufferedImage image) throws IOException {
        int rows = 4; //You should decide the values for rows and cols variables
        int cols = 4;
        int chunks = rows * cols;

        int chunkWidth = image.getWidth() / cols; // determines the chunk width and height
        int chunkHeight = image.getHeight() / rows;
        int count = 0;
        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                //Initialize the image array with image chunks
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                // draws the image chunk
                Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }
        log.debug("Splitting done");

        //writing mini images into image files
        for (int i = 0; i < imgs.length; i++) {
            ImageIO.write(imgs[i], "png", CreateTempFile.file("image_" + i + "-", "png"));
        }
        log.debug("Mini images created");
    }

    /**
     * Decrease the resolution of the receipt image with PNG file format for better resolution
     *
     * @param file
     * @return
     * @throws IOException
     */
    public File decreaseResolution(File file) throws IOException {
        BufferedImage image = bufferedImage(file);

        log.debug("W: " + image.getWidth() + ", " + "H: " + image.getHeight());
        double aspectRatio = (double) image.getWidth(null)/(double) image.getHeight(null);

        BufferedImage bufferedImage = resizeImage(image, 750, (int) (750/aspectRatio));
        File scaled = CreateTempFile.file(FilenameUtils.getBaseName(file.getName()) + UploadReceiptImage.SCALED, FilenameUtils.getExtension(file.getName()));
        ImageIO.write(bufferedImage, "png", scaled);
        return scaled;
    }

    /**
     * Decrease the resolution of the receipt image with PNG file format for better resolution
     *
     * @return
     * @throws IOException
     */
    public void decreaseResolution(InputStream is, OutputStream os) throws IOException {
        BufferedImage image = bufferedImage(is);

        log.debug("W: " + image.getWidth() + ", " + "H: " + image.getHeight());
        double aspectRatio = (double) image.getWidth(null)/(double) image.getHeight(null);

        BufferedImage bufferedImage = resizeImage(image, 750, (int) (750 / aspectRatio));
        ImageIO.write(bufferedImage, "png", os);
    }

    /**
     * Can be used for calculating height and width of an image
     *
     * @param file
     * @return
     * @throws IOException
     */
    public BufferedImage bufferedImage(File file) throws IOException {
        return bufferedImage(new FileInputStream(file));
    }

    public BufferedImage bufferedImage(InputStream is) throws IOException {
        return ImageIO.read(is);
    }

    /**
     * This function resize the image file and returns the BufferedImage object that can be saved to file system.
     *
     * @param image
     * @param width
     * @param height
     * @return
     */
    private static BufferedImage resizeImage(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
}
