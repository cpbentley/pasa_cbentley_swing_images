/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.swing.images.ctx.ImgCtx;

/**
 * 
 * @author Charles Bentley
 *
 */
public class ImageFrameProducerArray extends ImageFrameProducer {

   private ImageFrame[] frames;

   public ImageFrameProducerArray(ImgCtx sc, ImageFrame[] frames) {
      super(sc);
      this.frames = frames;
   }

   /**
    * Null if reached the end
    */
   public ImageFrame getNext() {
      index++;
      if (index < frames.length) {
         return frames[index];
      } else {
         return null;
      }
   }

   public ImageFrame getPrev() {
      index--;
      if (index >= 0) {
         return frames[index];
      } else {
         return null;
      }
   }

   public int getMaxWidthFrames() {
      int max = 0;
      for (ImageFrame frame : frames) {
         max = Math.max(max, frame.getImage().getWidth());
      }
      return max;
   }

   public int getMaxHeightFrames() {
      int max = 0;
      for (ImageFrame frame : frames) {
         max = Math.max(max, frame.getImage().getHeight());
      }
      return max;
   }

   public void resetTo(int index) {
      if (index < 0 || index >= frames.length) {
         throw new IllegalArgumentException("" + index);
      }
      this.index = index;
   }

   /**
    * -1 if current not known
    * @return
    */
   public int getNumFrames() {
      return frames.length;
   }

   /**
    * True if the number of frames may change over replays.
    * @return
    */
   public boolean isDynamicNumFrames() {
      return false;
   }

   public void resetToEnd() {
      index = frames.length - 1;
   }

   public boolean hasReachEnd() {
      return true;
   }

   public boolean hasMore() {
      return true;
   }

   public void close() {

   }

   public boolean isReverseSupported() {
      return true;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "ImageFrameProducerArray");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ImageFrameProducerArray");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }
   //#enddebug

}
