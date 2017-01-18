import java.awt.Color;
import java.io.File;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Gaussian blur of image now with sliders! 10/11
 * 
 * CHANGES: 
 * added image padding so that there is now black border around blurred image
 * added applyKernel method
 * r is now the radius of the kernel matrix, not the dimensions
 * added sliders to adjust sd and r
 * 
 * PROBLEMS:
 * everytime sliders are updated, a new picture window is opened (is this something to do with blurred being static?)
 * getting pretty slow, updating image after sliding takes a noticeable amount of time 
 */
public class GaussianBlur {
  public static Picture orig;
  public static Picture lowpass;
  public static Picture highpass;
  public static double sd = 1;
  public static int r = 1;
  private static JFrame frame;
  private static JSlider sdSlider;
  private static JSlider rSlider;
  
  public static void main(String[] args) {
    File in = new File(args[0]);
    orig = new Picture(in);
    lowpass = orig;
    highpass = orig;
    makeSliders();
    updateBlur();
  }
  private static void makeSliders() {
    //frame for slider
    frame = new JFrame("Sliders");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    //standard deviation slider
    sdSlider = new JSlider(1,10,(int)(sd));
    sdSlider.setPaintTicks(true);
    sdSlider.setPaintLabels(true);
    sdSlider.setMajorTickSpacing(1);
    sdSlider.setSnapToTicks(true);
    sdSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
          sd = (double)source.getValue();
          updateBlur();
        }
      }
    });
    //dimensions for standard deviation slider
    int sliderWidth = 100;
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
   * blur image based on updated values of sd and r and show new image
   */
  public static void updateBlur() {
    lowpass = orig;
    highpass = orig;
    double[][] lp = lowpassKernel();
    double[][] hp = highpassKernel();
    for(int x = 0; x < orig.width(); x++) {
      for(int y = 0; y < orig.height(); y++) {
        //apply kernel to each pixel to get value for new image
        lowpass.set(x,y,applyKernel(lp, x, y));
        highpass.set(x,y,applyKernel(hp, x, y));
      }
    }
    System.out.println("sd: " + sd);
    printKernel(lp);
    System.out.println();
    printKernel(hp);
    lowpass.show();
    highpass.show();
  }
  /**
   * 2D gaussian function (turns out the 1D equivalent expresses the normal distribution from statistics class!)
   */
  public static double g(int x, int y) {
    return ((1/(2*Math.PI*sd*sd)) * Math.exp(-1*(x*x + y*y)/(2*sd*sd))); 
  }
  
  /**
   * r sized matrix aprox. of gaussian function where x and y are distance from center of matrix
   * dimensions of matrix (and therefore r) must be odd
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
    System.out.println("lowpass:");
    System.out.println("sum1: " + sum);
    System.out.println("sum2: " + sum2);
    return kernel;
  }
  /**
   * high pass equivalent of above method
   */
  public static double[][] highpassKernel() {
    double[][] kernel = new double[(2*r)+1][(2*r)+1];
    int xmid = r;
    int ymid = r; //square
    double sum = 0;
    double height = 1.0/(Math.sqrt(2*Math.PI) * sd); //height of normal curve
    for(int x = 0; x < kernel.length; x++) {
      for(int y = 0; y < kernel[x].length; y++) {
        kernel[x][y] = height - g(x - xmid, y - ymid);
        sum += kernel[x][y];
      }
    }
    double sum2 = 0;
    for(int x = 0; x < kernel.length; x++) {
      for(int y = 0; y < kernel[x].length; y++) {
        kernel[x][y] = ((kernel[x][y])/sum);
        sum2 += kernel[x][y];
      }
    }
    System.out.println("highpass:");
    System.out.println("sum1: " + sum);
    System.out.println("sum2: " + sum2);
    return kernel;
  }
  /**
   * returns color of pixel once kernel has been applied
   */
  public static Color applyKernel(double[][] kernel, int x, int y) {
    int red = 0;
    int green = 0;
    int blue = 0;
    for(int dx = -r; dx <= r; dx++) {
      for(int dy = -r; dy <= r; dy++) {
        int x1 = x + dx;
        int y1 = y + dy;
        //check if pixel is valid
        if(x1 < 0 || y1 < 0 || x1 >= orig.width() || y1 >= orig.height()) {
          continue;
        }
        red += (int)((orig.get(x1,y1).getRed()) * (kernel[dx + r][dy + r]));
        green += (int)((orig.get(x1,y1).getGreen()) * (kernel[dx + r][dy + r]));
        blue += (int)((orig.get(x1,y1).getBlue()) * (kernel[dx + r][dy + r]));
      }
    }
    return new Color(red,green,blue);
  }
  /**
   * increases image size by r on all sides and pads image so that the padding contains the same
   * value as the closest pixel in the original image
   * not in use at the moment
   */
  public static Picture padEdges(Picture orig) {
    Picture res = new Picture(orig.width() + 2*r, orig.height() + 2*r);
    //copy into center of new image
    for(int x = 0; x < orig.width(); x++) { 
      for(int y = 0; y < orig.height(); y++) {
        res.set(x+r,y+r,orig.get(x,y));
      }
    }
    //pad top left corner(s)
    for(int x = 0; x < r-1; x++) {
      for(int y = 0; y < r-1; y++) {
        res.set(x,y,orig.get(0,0));
      }
    }
    //pad top right corner(s)
    for(int x = res.width() - 1; x >= res.width() - r; x--) {
      for(int y = 0; y < r-1; y++) {
        res.set(x,y,orig.get(orig.width()-1,0));
      }
    }
    //pad bottom left corner(s)
    for(int x = 0; x < r-1; x++) {
      for(int y = res.height() - 1; y >= res.height() - r; y--) {
        res.set(x,y,orig.get(0,orig.height()-1));
      }
    }
    //pad bottom right corners(s)
    for(int x = res.width() - 1; x >= res.width() - r; x--) {
      for(int y = res.height() - 1; y >= res.height() - r; y--) {
        res.set(x,y,orig.get(orig.width()-1,orig.height()-1));
      }
    }
    //pad top row(s)
    for(int x = r; x < res.width() - r; x++) {
      for(int y = 0; y < r; y++) {
        res.set(x,y,orig.get(x-r,0));
      }
    }
    //pad left column(s)
    for(int x = 0; x < r; x++) {
      for(int y = r; y < res.height() - r; y++) {
        res.set(x,y,orig.get(0,y-r));
      }
    }
    //pad bottom row(s)
    for(int x = r; x < res.width() - r; x++) {
      for(int y = res.height() - r; y < res.height(); y++) {
        res.set(x,y,orig.get(x-r,orig.height()-1));
      }
    }
    //pad right column(s)
    for(int x = res.width() - r; x < res.width(); x++) {
      for(int y = r; y < res.height() - r; y++) {
        res.set(x,y,orig.get(orig.width()-1,y-r));
      }
    }
    return res;
  }
  /**
   * padding has served its purpose and shows up as black pixels in blurred image, remove it
   * not in use right now
   */
  public static Picture removePadding(Picture orig) {
    Picture res = new Picture(orig.width() - 2*r, orig.height() - 2*r);
    for(int x = 0; x < res.width(); x++) {
      for(int y = 0; y < res.height(); y++) {
        res.set(x,y,orig.get(x+r,y+r));
      }
    }
    return res;
  }
  public static void printKernel(double[][] k) {
    for(double[] row: k) {
      for(double col: row) {
        System.out.print(col + " ");
      }
      System.out.println();
    }
  }
}
