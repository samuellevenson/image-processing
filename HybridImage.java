import java.io.File;
import java.util.Hashtable;
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

/**
 * Mixes two images together,a lowpass of one and a highpass of the other one 1/31
 * images must have the same dimensions (allowing different sized images would be possible however)
 * 
 * CHANGES: 
 * fixed labels on standard deviation slider (no longer displays 10x)
 * 
 * PROBLEMS:
 * gets kinda slow when radius > 3 (apparently gaussian blur is faster if it is done seperately in the x and y direction)
 * will not work if images are different sizes (should probably scale images to smallest width and height between the two)
 */
public class HybridImage {
  private static Picture img1;
  private static Picture img2;
  private static Picture lowpass;
  private static Picture highpass;
  private static Picture combined;
  private static int width;
  private static int height;
  private static double sd = 1;
  private static int r = 1;
  private static boolean debug = false;
  private static JFrame frame;
  private static JSlider sdSlider;
  private static JSlider rSlider;
  /**
   * initializes sliders for adjusting radius and standard deviation of blur as well as the frame they are in
   */
  private static void makeSliders() {
    //frame for slider
    frame = new JFrame("Sliders");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    //standard deviation slider
    Hashtable<Integer, JLabel> sdLabels = new Hashtable<Integer, JLabel>();
    for(int i = 0; i <= 30; i++) {
      sdLabels.put(i, new JLabel(Double.toString(i/10.0)));
    }
    sdSlider = new JSlider(0,30,(int)(sd));
    sdSlider.setLabelTable(sdLabels);
    sdSlider.setPaintTicks(true);
    sdSlider.setPaintLabels(true);
    sdSlider.setMajorTickSpacing(5);
    sdSlider.setSnapToTicks(false);
    sdSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
          sd = source.getValue()/10.0;
          updateBlur();
        }
      }
    });
    //dimensions for standard deviation slider
    int sliderWidth = 1000;
    Dimension sliderDimOrig = sdSlider.getPreferredSize();
    sliderWidth = Math.max(sliderDimOrig.width, sliderWidth);
    Dimension sliderDimNew =
      new Dimension(sliderWidth, sliderDimOrig.height);
    sdSlider.setPreferredSize(sliderDimNew);
    //radius slider
    rSlider = new JSlider(1,4,r);
    rSlider.setPaintTicks(true);
    rSlider.setPaintLabels(true);
    rSlider.setMajorTickSpacing(1);
    rSlider.setSnapToTicks(true);
    rSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
          r = source.getValue();
          updateBlur();
        }
      }
    });
    //dimensions for radius slider
    rSlider.setPreferredSize(sliderDimNew);
    
    //put sliders into frame
    frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
    frame.add(new JLabel("standard deviation and radius sliders"));
    frame.add(sdSlider);
    frame.add(rSlider);
    frame.pack();
    frame.setVisible(true);
  }
  /**
   * 2D gaussian function (turns out the 1D equivalent expresses the normal distribution from statistics class!)
   */
  public static double g(int x, int y) {
    return ((1/(2*Math.PI*sd*sd)) * Math.exp(-1*(x*x + y*y)/(2*sd*sd))); 
  }
  /**
   * matrix aprox. of gaussian function where x and y are distance from center of matrix
   * r is the radius not including the center pixel (.length = 2r+1)
   */
  public static double[][] lowpassKernel() {
    double[][] kernel = new double[(2*r)+1][(2*r)+1];
    int xmid = r;
    int ymid = r; //square
    double sum = 0;
    for(int x = 0; x < kernel.length; x++) {
      for(int y = 0; y < kernel[x].length; y++) {
        kernel[x][y] = g(x - xmid, y - ymid);
        sum += kernel[x][y];
      }
    }
    double sum2 = 0;;
    for(int x = 0; x < kernel.length; x++) {
      for(int y = 0; y < kernel[x].length; y++) {
        kernel[x][y] = (kernel[x][y])/sum; //sum of elements in kernel should equal 1
        sum2 += kernel[x][y];
      }
    }
    if(debug) {
      System.out.println("lowpass:");
      System.out.println("sum1: " + sum);
      System.out.println("sum2: " + sum2);
    }
    return kernel;
  }
  /**
   * returns color of pixel once kernel has been applied
   */
  public static Color applyKernel(Picture img, double[][] kernel, int x, int y) {
    int red = 0;
    int green = 0;
    int blue = 0;
    for(int dx = -r; dx <= r; dx++) {
      for(int dy = -r; dy <= r; dy++) {
        int x1 = x + dx;
        int y1 = y + dy;
        //check if pixel is valid
        if(x1 < 0 || y1 < 0 || x1 >= width || y1 >= height) {
          continue;
        }
        red += (int)((img.get(x1,y1).getRed()) * (kernel[dx + r][dy + r]));
        green += (int)((img.get(x1,y1).getGreen()) * (kernel[dx + r][dy + r]));
        blue += (int)((img.get(x1,y1).getBlue()) * (kernel[dx + r][dy + r]));
      }
    }
    return new Color(red,green,blue);
  }
  /**
   * prints values contained in kernel
   */
  public static void printKernel(double[][] k) {
    for(double[] row: k) {
      for(double col: row) {
        System.out.print(col + " ");
      }
      System.out.println();
    }
  }
  /**
   * applies low pass kernel to original image to get blurred image
   */
  public static void updateLowPass() {
    double[][] lp = lowpassKernel();
    for(int x = 0; x < width; x++) {
      for(int y = 0; y < height; y++) {
        //apply kernel to each pixel to get value for new image
        lowpass.set(x,y,applyKernel(img1,lp, x, y));
      }
    }
    if(debug) {
      System.out.println("sd: " + sd);
      printKernel(lp);
    }
  }
  /**
   * tbd
   */
  public static void updateHighPass() {
    //create lowpass image of img2
    Picture lowpass = new Picture(img2);
    double[][] lp = lowpassKernel();
    for(int x = 0; x < width; x++) {
      for(int y = 0; y < height; y++) {
        //apply kernel to each pixel to get value for new image
        lowpass.set(x,y,applyKernel(img2,lp, x, y));
      }
    }
    //subtract lowpass from original
    for(int x = 0; x < width; x++) {
      for(int y = 0; y < height; y++) {
        int red = 25 + (int)(img2.get(x,y).getRed() - lowpass.get(x,y).getRed());
        if(red < 0) {
          red = 0;
        }
        int green = 25 + (int)(img2.get(x,y).getGreen() - lowpass.get(x,y).getGreen());
        if(green < 0) {
          green = 0;
        }
        int blue = 25 + (int)(img2.get(x,y).getBlue() - lowpass.get(x,y).getBlue());
        if(blue < 0) {
          blue = 0;
        }
        highpass.set(x,y,new Color(red,green,blue));
      }
    }
  }
  /**
   * combines the lowpass of img1 and the highpass of img2 into one image by averaging the values of each pixel
   */
  public static void updateCombined() {
    for(int x = 0; x < combined.width(); x++) {
      for(int y = 0; y < combined.height(); y++) {
        int red = (lowpass.get(x,y).getRed() + highpass.get(x,y).getRed())/2;
        int green = (lowpass.get(x,y).getGreen() + highpass.get(x,y).getGreen())/2;
        int blue = (lowpass.get(x,y).getBlue() + highpass.get(x,y).getBlue())/2;
        combined.set(x,y,new Color(red,green,blue));
      }
    }
  }
  /**
   * blur image based on new values of sd and r and show new image
   */
  public static void updateBlur() {
    updateLowPass();
    lowpass.show();
    lowpass.setTitle("lowpass");
    updateHighPass();
    highpass.show();
    highpass.setTitle("highpass");
    updateCombined();
    combined.show();
    combined.setTitle("combined image");
  }
  public static void main(String[] args) {
    if(args.length < 2) {
      System.out.println("enter two filepaths as seperate command line arguments");
    }
    if(args.length >= 2) {
      img1 = new Picture(new File(args[0]));
      img2 = new Picture(new File(args[1]));
      lowpass = new Picture(img1);
      highpass = new Picture(img2);
      width = img2.width();
      height = img1.height();
      combined = new Picture(width,height);
    }
    if(args.length > 2) {
      if(args[2].equals("debug") || args[2].equals("true")) {
        debug = true;
      }
    }
    makeSliders();
    updateBlur();
  }
}