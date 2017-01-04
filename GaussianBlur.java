import java.awt.Color;
import java.io.File;
/**
 * Comments??
 */
public class GaussianBlur {
  public static double sd = 0.78;
  public static double[][] kernel;
  
  public static void main(String[] args) {
    File in = new File(args[0]);
    Picture img = new Picture(in);
    makeKernel(5);
    img.show();
    blur(img).show();
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
  public static void makeKernel(int r) {
    // TODO: Make r the actual radius so 2r+1 by 2r+1 matrix
    kernel = new double[r][r];
    int xmid = (int)(r/2.0);
    int ymid = xmid; //square
    double sum = 0;
    for(int x = 0; x < kernel.length; x++) {
      for(int y = 0; y < kernel[x].length; y++) {
        kernel[x][y] = g(x - xmid, y - ymid);
        System.out.println(kernel[x][y]);
        sum += kernel[x][y];
      }
    }
    System.out.println(sum);
    // TODO: Multiply all elements of matrix by reciprocal of sum if sum != 1
  }
  /**
   * returns blurred image, only uses red channel for now so result is also gray
   */
  public static Picture blur(Picture orig) {
    int r = kernel.length;
    // TODO: r = (int) kernel.length / 2;
    Picture res = new Picture(orig.width(),orig.height());
    //iterates through each pixel in image exluding a border based on r
    // TODO: Don't exclude the border
    for(int x = (int)(r/2.0); x < orig.width() - (int)(r/2.0); x++) {
      for(int y = (int)(r/2.0); y < orig.height() - (int)(r/2.0); y++) {
        //apply kernel to each pixel to get value for blurry image
        int red = 0;
        int green = 0;
        int blue = 0;
        for(int dx = -(int)(r/2.0); dx <= (int)(r/2.0); dx++) {
          for(int dy = -(int)(r/2.0); dy <= (int)(r/2.0); dy++) {
            int x1 = x + dx;
            int y1 = y + dy;
            if (x1 < 0 || y1 < 0 || x1 >= orig.width() || y1 >= orig.height()) {
              continue;
            }
            red += ((orig.get(x + dx,y + dy).getRed()) * (kernel[dx + (int)(r/2.0)][dy + (int)(r/2.0)]));
            green += ((orig.get(x + dx,y + dy).getGreen()) * (kernel[dx + (int)(r/2.0)][dy + (int)(r/2.0)]));
            blue += ((orig.get(x + dx,y + dy).getBlue()) * (kernel[dx + (int)(r/2.0)][dy + (int)(r/2.0)]));
          }
        }
        res.set(x,y,new Color(red,green,blue));
      }
    }
    return res;
  }
}
