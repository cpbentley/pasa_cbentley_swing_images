/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.swing.images.ctx.ImgCtx;
import pasa.cbentley.swing.images.interfaces.IFrameData;
import pasa.cbentley.swing.images.utils.WritableBufferedImage;

/**
 * 
 * Store image data of a frame.
 * <br>
 * <br>
 * A frame has an index and a delay.
 * <br>
 * A full frame provide an animation an image to be drawn
 * <br>
 * @author Charles Bentley
 *
 */
public class ImageFrame implements IStringable, IFrameData {

   protected int           delay;

   protected String        disposal;

   protected BufferedImage image;

   protected int           index;

   protected ImgCtx      sc;

   /**
    * Cache of the scaled image
    */
   protected BufferedImage scaled;

   public ImageFrame(ImgCtx sc, BufferedImage image, int delay, String disposal) {
      this.sc = sc;
      this.image = image;
      this.delay = delay;
      this.disposal = disposal;
   }

   /**
    * Used by sub class that will dynamically provide the BufferedImage of the frame
    * @param sc
    * @param image
    * @param delay
    * @param disposal
    */
   protected ImageFrame(ImgCtx sc, int delay, String disposal, int index) {
      this.sc = sc;
      this.delay = delay;
      this.disposal = disposal;
      this.index = index;
   }

   public int getDelay() {
      return delay;
   }

   public String getDisposal() {
      return disposal;
   }

   public BufferedImage getImage() {
      if (scaled != null) {
         return scaled;
      }
      return image;
   }

   /**
    * might be sublclassed
    * @return
    */
   public BufferedImage getImageRoot() {
      return image;
   }

   public int getIndex() {
      return index;
   }

   /**
    * Called by worker thread when it has scaled the image
    * @param bi
    */
   public void setImageScaled(BufferedImage bi) {
      scaled = bi;
   }

   //#mdebug
   public IDLog toDLog() {
      return sc.toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "ImageFrame");
      toStringPrivate(dc);
      dc.nlLvlO(image, "frameImage", sc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ImageFrame");
      toStringPrivate(dc);
      dc.nlLvlO(image, "frameImage", sc);
   }

   public UCtx toStringGetUCtx() {
      return sc.getUCtx();
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("index", index);
      dc.appendVarWithSpace("delay", delay);
      dc.appendVarWithSpace("disposal", disposal);
   }
   //#enddebug

}