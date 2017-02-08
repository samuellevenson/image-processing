import java.awt.Color;
import java.io.File;
/**
 * resources: http://cs.brown.edu/courses/cs143/2013/proj2/ (project page)
 *            http://mccormickml.com/2013/02/26/image-derivative/ (image derivatives)
 *            http://fourier.eng.hmc.edu/e161/lectures/gradient/node8.html (laplacian operator + gaussian things)
 *            https://en.wikipedia.org/wiki/Kronecker_product (how to do outer product of matrices)
 * 
 * 2/8/17
 * 
 * started interest point detection part of program
 */
public class LocalFeatureMatching {
  /**
   * 2D gaussian function (turns out the 1D equivalent expresses the normal distribution from statistics class!)
   */
  public static double gaussian(int x, int y, double sd) {
    return ((1/(2*Math.PI*sd*sd)) * Math.exp(-1*(x*x + y*y)/(2*sd*sd))); 
  }
  /**
   * laplacian of gaussian combines laplacian operator with gaussian function
   */
  public static double laplacianOfGaussian(int x, int y, double sd) {
    return Math.exp(-1*(x*x+y*y)/2*sd*sd) * (x*x + y*y - 2*sd*sd)/(sd*sd*sd*sd);
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
  public static Picture xDeriv(Picture img) {
    Picture result = new Picture(img.width(),img.height());
    for(int y = 0; y < img.height(); y++) {
      for(int x = 1; x < img.width(); x++) {
        int dx = img.get(x,y).getRed() - img.get(x-1,y).getRed();
        dx = (dx+255)/2; //scale from range of -255,255 to range of 0,255
        result.set(x,y,new Color(dx,dx,dx));
      }
    }
    return result;
  }
  /**
   * image derivative in y direction of red channel
   */
  public static Picture yDeriv(Picture img) {
    Picture result = new Picture(img.width(),img.height());
    for(int x = 0; x < img.width(); x++) {
      for(int y = 1; y < img.height(); y++) {
        int dy = img.get(x,y).getRed() - img.get(x,y-1).getRed();
        dy = (dy+255)/2; //scale from range of -255,255 to range of 0,255
        result.set(x,y,new Color(dy,dy,dy));
      }
    }
    return result;
  }
  /**
   * gaussian blur of image
   */
  public static Picture gBlur(Picture img, int r, double sd) {
    //make gaussian kernel
    Picture result = new Picture(img);
    double[][] g = new double[(2*r)+1][(2*r)+1];
    int xmid = r;
    int ymid = r; //square
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
   * applies laplacian operator + gaussian blur
   */
  public static Picture lapLogBlur(Picture img, int r, double sd) {
    //make laplacian of gaussian kernel (LoG)
    Picture result = new Picture(img);
    double[][] lapLog = new double[(2*r)+1][(2*r)+1];
    int xmid = r;
    int ymid = r; //square
    double sum = 0;
    for(int x = 0; x < lapLog.length; x++) {
      for(int y = 0; y < lapLog[x].length; y++) {
        lapLog[x][y] = laplacianOfGaussian(x - xmid, y - ymid, sd);
        sum += lapLog[x][y];
      }
    }
    //make elements of kernel sum to 1
    for(int x = 0; x < lapLog.length; x++) {
      for(int y = 0; y < lapLog[x].length; y++) {
        lapLog[x][y] = (lapLog[x][y])/sum;
      }
    }
    //apply kernel
    result = applyKernel(img,lapLog);
    return result;
  }
  /**
   * unfinished attempt to find outer product of two matrices
   */
  public static Picture outerProduct(Picture p1, Picture p2) {
    Picture outer = new Picture(p1.width()*p2.width(), p1.height()*p2.height());
    for(int x1 = 0; x1 < p1.width(); x1++) {
      for(int y1 = 0; y1 < p1.height(); y1++) {
        int red = p1.get(x1,y1).getRed();
        int green = p1.get(x1,y1).getGreen();
        int blue = p1.get(x1,y1).getBlue();
        for(int x2 = 0; x2 < p2.width(); x2++) {
          for(int y2 = 0; y2 < p2.height(); y2++) {
            
          }
        }
      }
    }
    return null;
  }
  public static void main(String[] args) {
    Picture img = new Picture(new File("/Users/sammy/Documents/compsi/compsci12th/pictures/bicycle.bmp"));
    if(args.length > 0) {
      img = new Picture(new File(args[0]));
    }
    img.show();
    img.setTitle("original");
    Picture imgx = xDeriv(gBlur(img,1,1)); //image derivative in the x direction
    imgx.show();
    imgx.setTitle("x derivative");
    Picture imgy = yDeriv(gBlur(img,1,1)); //image derivative in the y direction
    imgy.show();
    imgy.setTitle("y derivative");
    Picture laplacianOfGaussian = lapLogBlur(img,1,1);
    laplacianOfGaussian.show();
    laplacianOfGaussian.setTitle("laplacian of gaussian");
  }
}