/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.swing.images.ctx.ImgCtx;

/**
 * Runs according to an index.
 * 
 * If 
 * @author Charles Bentley
 *
 */
public abstract class ImageFrameProducer implements IStringable {

   protected ImgCtx sc;

   protected int      index;

   public ImageFrameProducer(ImgCtx sc) {
      this.sc = sc;
   }

   /**
    * Call when the animations if disposed
    */
   public abstract void close();

   /**
    * 
    * @return
    */
   public int getIndex() {
      return index;
   }

   /**
    * 
    * @return
    */
   public abstract int getMaxHeightFrames();

   /**
    * 
    * @return
    */
   public abstract int getMaxWidthFrames();

   /**
    * Returns the next Frame relative to the current frame index.
    * <br>
    * The first call returns the first frame.
    * <br>
    * <br>
    * Returns null if we have reached the end.
    * <br>
    * @return
    */
   public abstract ImageFrame getNext();

   /**
    * -1 if current not known
    * @return
    */
   public abstract int getNumFrames();

   /**
    * 
    * @return null if could not get the frame
    */
   public abstract ImageFrame getPrev();

   /**
    * True if Producer is still able to honor either
    * <li> {@link ImageFrameProducer#getNext()}
    * <li> {@link ImageFrameProducer#getPrev()}
    * <br>
    * <br>
    * This means some producers may run in loop 
    * @return
    */
   public abstract boolean hasMore();

   /**
    * Return true, but getNext
    * @return
    */
   public abstract boolean hasReachEnd();

   /**
    * True if the number of frames may change over replays.
    * @return
    */
   public abstract boolean isDynamicNumFrames();

   /**
    * 
    * @return true if engine is able to produce frames in reverse order
    */
   public abstract boolean isReverseSupported();

   /**
    * Seek to the end. Used before a {@link ImageFrameProducer#getPrev()}
    */
   public abstract void resetToEnd();

   /**
    * Called on a Producer, it may throw an IllegalArgumentException
    * @param index
    */
   public void resetTo(int index) {
      this.index = index;
   }

   /**
    * Set the index back the the begining
    */
   public void resetToZero() {
      index = 0;
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "ImageFrameProducer");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("index", index);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ImageFrameProducer");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return sc.getUC();
   }
   //#enddebug

}
