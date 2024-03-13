/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.utils;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.swing.images.ctx.ImgCtx;

/**
 * Class with 2 {@link WritableBufferedImage}.
 * 
 * When one (front) is being painted by the UI thread, the other is being written by the anim thread (back). 
 * 
 * @author Charles Bentley
 *
 */
public class DoubleBuffer implements IStringable {

   private WritableBufferedImage image1;

   private WritableBufferedImage image2;

   private volatile boolean      isSwitched;

   protected final ImgCtx        imgc;

   public DoubleBuffer(ImgCtx sc, int w, int h) {
      this.imgc = sc;
      image1 = new WritableBufferedImage(w, h);
      image2 = new WritableBufferedImage(w, h);
   }

   public WritableBufferedImage getBackBuffer() {
      if (isSwitched) {
         return image1;
      } else {
         return image2;
      }
   }

   public WritableBufferedImage getFrontBuffer() {
      if (isSwitched) {
         return image2;
      } else {
         return image1;
      }
   }

   /**
    * Its only called by the worker thread when it has finished updating the buffer.
    * So no requirement that it should be atomic
    */
   public void swapImages() {
      isSwitched = !isSwitched;
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "DoubleBuffer");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "DoubleBuffer");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return imgc.getUC();
   }

   //#enddebug

}
