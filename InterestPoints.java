import java.io.File;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;

/**
 * finds the pixels on an image that are most cornerlike
 * 2/22/17
 * 
 * optional command line argument "test" if you want things to print for testing purposes
 */
public class InterestPoints {
  
  private static Picture i; //original image
  private static Picture i_corners; //image with corners drawn on
  private static double[][] ix; //x derivative
  private static double[][] iy; //y derivative
  private static int r = 1; //windowing radius
  private static int threshold = 10000; //minimun value for a pixel to be a corner
  private static boolean testing = false;
  private static JSlider thresholdSlider;
  private static JFrame frame;
  
  public static void main(String[] args) {
    //get input stuff
    File filepath = null;    
    JFileChooser fileChooser = new JFileChooser();
    int choice = fileChooser.showOpenDialog(null);
    if(choice == JFileChooser.APPROVE_OPTION) {
      filepath = fileChooser.getSelectedFile();
    }
    if(args.length > 0 && args[0].equals("test")) {
      testing = true;
    }
    i = new Picture(filepath);
    i_corners = new Picture(i);
    //find x and y image derivative
    xDeriv();
    yDeriv();
    drawCorners();
    makeSlider();
  } 
  /**
   * image derivative in x direction
   */
  public static void xDeriv() {
    ix = new double[i.width()][i.height()];
    for(int x = 1; x < i.width(); x++) {
      for(int y = 0; y < i.height(); y++) {
        double dx = grayscale(i.get(x,y)) - grayscale(i.get(x-1,y));
        ix[x][y] = Math.abs(dx); //not sure whether I should use absolute val or not
      }
    }
  }
  /**
   * image derivative in y direction
   */
  public static void yDeriv() {
    iy = new double[i.width()][i.height()];
    for(int x = 0; x < i.width(); x++) {
      for(int y = 1; y < i.height(); y++) {
        double dy = grayscale(i.get(x,y)) - grayscale(i.get(x,y-1));
        iy[x][y] = Math.abs(dy);
      }
    }
  }
  /**
   * for a given point (x,y) on the image finds a matrix
   *  | ix*ix ix*iy |
   *  | ix*iy iy*iy | 
   * where ix is the sum of the pixel values in the x image derivative in a radius r from point (x,y)
   */
  public static double[][] autoCorrelation(int x0, int y0) {
    double ixSum = 0;
    double iySum = 0;
    for(int x = x0 - r; x < x0 + r; x++) {
      for(int y = y0 - r; y < y0 + r; y++) {
        ixSum += ix[x][y];
        iySum += iy[x][y];
      }
    }
    if(testing) {
      System.out.println(x0 + "," + y0 + " -> " + "ixSum: " + ixSum + " iySum: " + iySum);
    }
    return new double[][] {{ixSum * ixSum, ixSum * iySum},{ixSum * iySum, iySum * iySum}};
  }
  /**
   * finds the smaller of two eigenvalues of a 2x2 matrix
   */
  public static double eigenVal(double[][] m) {
    double a = m[0][0];
    double b = m[0][1];
    double c = m[1][0];
    double d = m[1][1];
    double det = a*d - b*c;
    
    double l1 = (a+d)/2 + Math.sqrt((a+d)*(a+d)/(4-det));
    l1 = Math.abs(l1);
    if(testing) {
      System.out.println("l1: " + l1);
    }
    return l1;
  }
  /**
   * creates list of coordinates on the image that are corners
   */
  public static ArrayList<int[]> findCorners() {
    ArrayList<int[]> corners = new ArrayList<int[]>();
    //goes through image except for 2*r sized border
    for(int x = 2*r; x < i.width() - 2*r; x++) {
      for(int y = 2*r; y < i.height() - 2*r; y++) {
        double[][] m = autoCorrelation(x,y); //finds autocorrelation matrix
        double l = eigenVal(m); //smaller eigenvalue of matrix m
        
        if(l >= threshold) { 
          corners.add(new int[] {x,y}); //adds coordinate to list
        }
      }
    }
    return corners;
  }
  /**
   * creates a new image with corners drawn as red dots
   */
  public static void drawCorners() {
    ArrayList<int[]> corners = findCorners();
    //clear previous points
    for(int x = 0; x < i.width(); x++) {
      for(int y = 0; y < i.height(); y++) {
        i_corners.set(x,y,i.get(x,y));
      }
    }
    Color red = new Color(255,0,0);
    for(int i = 0; i < corners.size(); i++) {
      int x0 = corners.get(i)[0];
      int y0 = corners.get(i)[1];
      //draw small dot centered on image marked as corner
      for(int x = x0 - 1; x < x0 + 1; x++) {
        i_corners.set(x,y0 + 2,red);
      }
      for(int x = x0 - 2; x < x0 + 2; x++) {
        for(int y = y0 - 1; y < y0 + 1; y++) {
          i_corners.set(x,y,red);
        }
      }
      for(int x = x0 - 1; x < x0 + 1; x++) {
        i_corners.set(x,y0-2,red);
      }
    }
    i_corners.show();
    i_corners.setTitle("corners");
  }
  /**
   * creates a slider for threshold
   */
  public static void makeSlider() {
    frame = new JFrame("Threshold");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    thresholdSlider = new JSlider(0,500000,threshold);
    thresholdSlider.setPaintTicks(true);
    thresholdSlider.setPaintLabels(true);
    thresholdSlider.setMajorTickSpacing(50000);
    thresholdSlider.setSnapToTicks(false);
    thresholdSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
          threshold = source.getValue();
          drawCorners();
          if(testing) {
            System.out.println("threshold: " + threshold);
          }
        }
      }
    });
    //dimensions for standard deviation slider
    int sliderWidth = 1000;
    Dimension sliderDimOrig = thresholdSlider.getPreferredSize();
    sliderWidth = Math.max(sliderDimOrig.width, sliderWidth);
    Dimension sliderDimNew =
      new Dimension(sliderWidth, sliderDimOrig.height);
    thresholdSlider.setPreferredSize(sliderDimNew);
    //put slider into frame
    frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
    frame.add(new JLabel("threshold slider"));
    frame.add(thresholdSlider);
    frame.pack();
    frame.setVisible(true);
  }
  /**
   * returns the grayscale equivalent of color
   */
  public static double grayscale(Color c) {
    return 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
  }
}
