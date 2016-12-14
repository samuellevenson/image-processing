import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * practice detecting features in images
 * ways to improve this: convert image to grayscale, apply some sort of smoothing filter,
 * 
 * most helpful resources: https://blog.saush.com/2011/04/20/edge-detection-with-the-sobel-operator-in-ruby/
 */
public class FeatureExtraction extends JPanel{
  public static void main(String[] args) {
    File f = new File(args[0]);
    BufferedImage original = readImage(f);
    showImage(original);
    BufferedImage edges = sobelFilter(original);
    showImage(edges);
  }
  /**
   * applies sobel filter to image, used to detect edges
   * "an image gradient is a change in intensity (or color) or an image" - from website
   */
  public static BufferedImage sobelFilter(BufferedImage orig) {
    int width = orig.getWidth();
    int height = orig.getHeight();
    
    BufferedImage sobel = new BufferedImage(width-2,height-2,BufferedImage.TYPE_BYTE_GRAY);
    for(int h = 1; h < height - 2; h++) {
      for(int w = 1; w < width - 2; w++) {
        int pix_x = ((-1*orig.getRGB(w-1,h-1)) + (1*orig.getRGB(w+1,h-1)) + (-2*orig.getRGB(w-1,h))
                       + (2*orig.getRGB(w+1,h)) + (-1*orig.getRGB(w-1,h+1)) + (1*orig.getRGB(w+1,h+1))); //find the x gradient of the pixel
        int pix_y = ((-1*orig.getRGB(w-1,h-1)) + (-2*orig.getRGB(w,h-1)) + (-1*orig.getRGB(w+1,h-1))
                       + (1*orig.getRGB(w-1,h+1)) + (2*orig.getRGB(w,h+1)) + (1*orig.getRGB(w+1,h+1))); //find the y gradient of the pixel
        
        int pix = (int)(Math.sqrt(pix_x*pix_x + pix_y*pix_y) + 0.5); //find the overall magnitude of the gradient
        sobel.setRGB(w,h,pix);
      }
    }
    return sobel;
  }
  /**
   * displays an image in a jframe
   */
  public static void showImage(BufferedImage b) {
    JFrame imgframe = new JFrame();
    imgframe.getContentPane().add(new JLabel(new ImageIcon(b)));
    imgframe.pack();
    imgframe.setVisible(true);
  }
  /**
   * reads image from file
   */
  public static BufferedImage readImage(File f) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(f);
    } catch (IOException e) {
      System.out.println("Unable to read image: " + e.getMessage());
      System.exit(1);
    }
    return image;
  }
}