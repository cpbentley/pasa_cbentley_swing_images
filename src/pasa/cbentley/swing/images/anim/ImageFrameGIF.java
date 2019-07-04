package pasa.cbentley.swing.images.anim;

import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.swing.images.ctx.ImgCtx;

/**
 * Frame with specific Gif information
 * 
 * @author Charles Bentley
 *
 */
public class ImageFrameGIF extends ImageFrame {

   private GifEnginePlayOnly engine;

   /**
    * In some cases, every single frame is similar
    */
   private int               frameToBuildOne;

   private int               h;

   /**
    * Last full frame to build o
    */
   private int               lastFullFrame;

   private int               w;

   private int               x;

   private int               y;

   private BufferedImage     rootImage;

   /**
    * 
    * @param sc
    * @param engine class that created this {@link ImageFrameGIF}
    * @param delay
    * @param disposal
    * @param index
    */
   protected ImageFrameGIF(ImgCtx sc, GifEnginePlayOnly engine, int delay, String disposal, int index) {
      super(sc, delay, disposal, index);
      this.engine = engine;
   }

   public int getDelay() {
      return delay;
   }

   public int getFrameToBuildOne() {
      return frameToBuildOne;
   }

   public int getH() {
      return h;
   }

   public BufferedImage getImage() {
      return image;
   }

   public ImageFrame cloneForGui() {
      BufferedImage image = getImage();
      BufferedImage copy = new BufferedImage(image.getColorModel(), image.copyData(null), image.isAlphaPremultiplied(), null);
      ImageFrameGIF frame = new ImageFrameGIF(sc,engine, delay, disposal, index);
      frame.setRootImage(rootImage);
      frame.setFrameImage(copy);
      frame.x = x;
      frame.y = y;
      frame.w = w;
      frame.h = h;
      return frame;
   }

   public int getLastFullFrame() {
      return lastFullFrame;
   }

   public int getW() {
      return w;
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public void setFrameToBuildOne(int frameToBuildOne) {
      this.frameToBuildOne = frameToBuildOne;
   }

   public void setH(int h) {
      this.h = h;
   }

   public void setFrameImage(BufferedImage image) {
      this.image = image;
   }

   /**
    * override
    * @return
    */
   public BufferedImage getImageRoot() {
      return rootImage;
   }

   public boolean isLastFullFrame() {
      return lastFullFrame == index;
   }

   public void setLastFullFrame(int lastFullFrame) {
      this.lastFullFrame = lastFullFrame;
   }

   /**
    * The image that is read from.
    * <br>
    * it might be a lot smaller
    * @param img
    */
   public void setRootImage(BufferedImage img) {
      rootImage = img;
   }

   public void setW(int w) {
      this.w = w;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "FrameGIF");
      toStringPrivate(dc);
      super.toString(dc.sup());

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "FrameGIF");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("x", x);
      dc.appendVarWithSpace("y", y);
      dc.appendVarWithSpace("w", w);
      dc.appendVarWithSpace("h", h);
      dc.appendVarWithSpace("lastFullFrame", lastFullFrame);
      dc.appendVarWithSpace("frameToBuildOne", frameToBuildOne);
   }
   //#enddebug

}
