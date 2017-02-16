import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
/**
 * resources: http://cs.brown.edu/courses/cs143/2013/proj2/ (project page)
 *            http://mccormickml.com/2013/02/26/image-derivative/ (image derivatives)
 *            http://fourier.eng.hmc.edu/e161/lectures/gradient/node8.html (laplacian operator + gaussian things)
 *            https://www.youtube.com/watch?v=vkWdzWeRfC4
 * 
 * 2/8/17
 * 
 * started interest point detection part of program
 */
public class LocalFeatureMatching {
  
  public static Picture i; //original image
  public static Picture ix; //x derivative
  public static Picture iy; //y derivative
  public static int r = 1; //windowing radius
  public static double threshold = 0; //minimun value for a pixel to be a corner
  
  /**
   * 2D gaussian function
   */
  public static double gaussian(int x, int y, double sd) {
    return ((1/(2*Math.PI*sd*sd)) * Math.exp(-1*(x*x + y*y)/(2*sd*sd))); 
  }
  /**
   * apples a given kernel to a given image
   */
  public static Picture applyKernel(Picture img, double[][] kernel) {
    Picture result = new Picture(img.width(),img.height());
    int r = kernel.length/2;
    for(int x = 0; x < img.width(); x++) {
      for(int y = 0; y < img.height(); y++) {
        int red = 0;
        int green = 0;
        int blue = 0;
        for(int dx = -r; dx <= r; dx++) {
          for(int dy = -r; dy <= r; dy++) {
            int x1 = x + dx;
            int y1 = y + dy;
            //check if pixel is valid
            if(x1 < 0 || y1 < 0 || x1 >= img.width() || y1 >= img.height()) {
              continue;
            }
            red += (int)((img.get(x1,y1).getRed()) * (kernel[dx + r][dy + r]));
            green += (int)((img.get(x1,y1).getGreen()) * (kernel[dx + r][dy + r]));
            blue += (int)((img.get(x1,y1).getBlue()) * (kernel[dx + r][dy + r]));
          }
        }
        result.set(x,y, new Color(red,green,blue));
      }
    }
    return result;
  }
  /**
   * image derivative in x direction of red channel
   */
  public static Picture xDeriv(Picture i) {
    Picture result = new Picture(i.width(),i.height());
    for(int y = 0; y < i.height(); y++) {
      for(int x = 1; x < i.width(); x++) {
        int dx = (int)(i.get(x,y).getRed() - i.get(x-1,y).getRed());
        System.out.println("dx : " + dx);
        dx = (dx+255)/2; //scale from range of -255,255 to range of 0,255
        result.set(x,y,new Color(dx,dx,dx));
      }
    }
    return result;
  }
  /**
   * image derivative in y direction of red channel (but its a grayscale image now so all channels should be the same)
   */
  public static Picture yDeriv(Picture i) {
    Picture result = new Picture(i.width(),i.height());
    for(int x = 0; x < i.width(); x++) {
      for(int y = 1; y < i.height(); y++) {
        int dy = (int)(i.get(x,y).getRed() - i.get(x,y-1).getRed());
        dy = (dy+255)/2; //scale from range of -255,255 to range of 0,255
        result.set(x,y,new Color(dy,dy,dy));
      }
    }
    return result;
  }
  /**
   * gaussian blur of image
   */
  public static Picture gBlur(Picture img, double sd) {
    //make gaussian kernel
    Picture result = new Picture(img);
    double[][] g = new double[(2*r)+1][(2*r)+1];
    int xmid = r;
    int ymid = r;
    double sum = 0;
    for(int x = 0; x < g.length; x++) {
      for(int y = 0; y < g[x].length; y++) {
        g[x][y] = gaussian(x - xmid, y - ymid, sd);
        sum += g[x][y];
      }
    }
    //make elements of kernel sum to 1
    for(int x = 0; x < g.length; x++) {
      for(int y = 0; y < g[x].length; y++) {
        g[x][y] = (g[x][y])/sum;
      }
    }
    //apply kernel to image
    result = applyKernel(img, g);
    return result;
  }
  /**
   * multiplies two 2D matrices together
   * will probably not work if dimensions don't line up like they need to when you multiply matrices
   */
  public static double[][] matrixMultiply(double[][] m1, double[][] m2) {
    double[][] mresult = new double[m1.length][m2[0].length];
    for(int m1Row = 0; m1Row < m1.length; m1Row++) {
      for(int m2Col = 0; m2Col < m2[0].length; m2Col++) {
        for(int m1Col = 0; m1Col < m1[0].length; m1Col++) {
          mresult[m1Row][m2Col] += m1[m1Row][m1Col] * m2[m1Col][m2Col];
        }
      }
    }
    return mresult;
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
    for(int x = x0 - r; x < x0 + 2*r; x++) {
      for(int y = y0 - r; y < y0 + 2*r; y++) {
        System.out.println("dx2: " + ((ix.get(x,y).getRed()*2)-255));
        ixSum += (ix.get(x,y).getRed()*2)-255;
        iySum += (iy.get(x,y).getRed()*2)-255;
      }
    }
    System.out.println(x0 + "," + y0 + " " + "ixSum: " + ixSum + " iySum: " + iySum);
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
    double l1 = 0.5 * (a-d) + Math.sqrt(((a-d)*(a-d)) - 4*((a*d)-(b*c)));
    double l2 = 0.5 * (a-d) - Math.sqrt(((a-d)*(a-d)) - 4*((a*d)-(b*c)));
    if(l1 < l2) {
      return l1;
    }
    return l2;
  }
  /**
   * 
   */
  public static ArrayList<int[]> findCorners() {
    ArrayList<int[]> corners = new ArrayList<int[]>();
    //goes through image except for r sized border
    for(int x = r; x < i.width() - r; x++) {
      for(int y = r; y < i.height() - r; y++) {
        double[][] m = autoCorrelation(x,y); //finds autocorrelation matrix
        double l = eigenVal(m); //smaller eigenvalue of matrix m
        if(l > threshold) {
          corners.add(new int[] {x,y});
        }
      }
    }
    //printArray(autoCorrelation(3,3));
    return corners;
  }
  /**
   * 
   */
  public static void drawCorners() {
    ArrayList<int[]> corners = findCorners();
    //System.out.println(corners.size());
    Picture withCorners = new Picture(i);
    for(int i = 0; i < corners.size(); i++) {
      int x0 = corners.get(i)[0];
      int y0 = corners.get(i)[1];
      for(int x = x0 - 1; x < x0 + 2; x++) {
        for(int y = y0 - 1; y < y0 + 2; y++) {
          withCorners.set(x,y, new Color(255,0,0)); //sets 3x3 area centered on corner red
        }
      }
    }
    withCorners.show();
  }
  /**
   * displays contents of 2D array for testing
   */
  public static void printArray(double [][] m) {
    for(double[] row: m) {
      for(double col: row) {
        System.out.print(col + " ");
      }
      System.out.println();
    }
  }
  /**
   * returns the grayscale equivalent of image
   */
  public static Picture toGrayscale(Picture p) {
    Picture gray = new Picture(p.width(), p.height());
    for(int x = 0; x < p.width(); x++) {
      for(int y = 0; y < p.height(); y++) {
        Color c = p.get(x,y);
        double luminance = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
        Color g = new Color((int)luminance,(int)luminance,(int)luminance);
        gray.set(x,y,g);
      }
    }
    return gray;
  }
  public static void main(String[] args) {
    if(args.length == 0) {
      i = new Picture(new File("/Users/sammy/Documents/compsi/compsci12th/pictures/bicycle.bmp"));
    }
    else {
      i = new Picture(new File(args[0])); 
    }
    i = toGrayscale(i);
    i.show();
    i.setTitle("original");
    ix = gBlur(xDeriv(i),1); //image derivative in the x direction
    ix.show();
    ix.setTitle("x derivative");
    iy = gBlur(yDeriv(i),1); //image derivative in the y direction
    iy.show();
    iy.setTitle("y derivative");
    drawCorners();
  }
}