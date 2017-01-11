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
  public static Picture blurred;
  public static double sd = 1;
  public static int r = 1;
  private static JFrame frame;
  private static JSlider sdSlider;
  private static JSlider rSlider;
  
  public static void main(String[] args) {
    File in = new File(args[0]);
    orig = new Picture(in);
    blurred = padEdges(orig);
    makeSliders();
    updateBlur();
  }
  private static void makeSliders() {
    //frame for slider
    frame = new JFrame("Sliders");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    //standard deviation slider
    sdSlider = new JSlider(1,10,(int)sd);
    sdSlider.setPaintTicks(true);
    sdSlider.setPaintLabels(true);
    sdSlider.setMajorTickSpacing(1);
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
    double[][] kernel = makeKernel();
    Picture res = new Picture(orig.width(),orig.height());
    //iterates through each pixel in image exluding a border based on r
    for(int x = r; x < orig.width() - r; x++) {
      for(int y = r; y < orig.height() - r; y++) {
        //apply kernel to each pixel to get value for blurry image
        res.set(x,y,applyKernel(kernel, orig, x, y));
      }
    }
    res = removePadding(res);
    res.show();
  }
  /**
   * 2D gaussian function (turns out the 1D equivalent expresses the normal distribution from statistics class!)
   */
  public static double g(int x, int y) {
    return ((1/(2*Math.PI*sd*sd)) * Math.exp(-1*(x*x + y*y)/(2*sd*sd))); 
  }
  /**
   * increases image size by r on all sides and pads image so that the padding contains the same
   * value as the closest pixel in the original image
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
   * r sized matrix aprox. of gaussian function where x and y are distance from center of matrix
   * dimensions of matrix (and therefore r) must be odd
   */
  public static double[][] makeKernel() {
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
    for(int x = 0; x < kernel.length; x++) {
      for(int y = 0; y < kernel[x].length; y++) {
        kernel[x][y] = kernel[x][y]/sum;
      }
    }
    return kernel;
  }
  /**
   * returns color of pixel once kernel has been applied
   */
  public static Color applyKernel(double[][] kernel, Picture orig, int x, int y) {
    int red = 0;
    int green = 0;
    int blue = 0;
    for(int dx = -r; dx <= r; dx++) {
      for(int dy = -r; dy <= r; dy++) {
        red += ((orig.get(x + dx,y + dy).getRed()) * (kernel[dx + r][dy + r]));
        green += ((orig.get(x + dx,y + dy).getGreen()) * (kernel[dx + r][dy + r]));
        blue += ((orig.get(x + dx,y + dy).getBlue()) * (kernel[dx + r][dy + r]));
      }
    }
    return new Color(red,green,blue);
  }
  /**
   * padding has served its purpose and shows up as black pixels in blurred image, remove it
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
}
