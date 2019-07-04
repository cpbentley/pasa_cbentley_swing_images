package pasa.cbentley.swing.images.anim;

import java.io.IOException;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.swing.images.ctx.ImgCtx;

/**
 * 
 * @author Charles Bentley
 *
 */
public class ImageFrameProducerGifOptimized extends ImageFrameProducer {

   private GifEnginePlayOnly engine;

   public ImageFrameProducerGifOptimized(ImgCtx sc, GifEnginePlayOnly engine) {
      super(sc);
      this.engine = engine;
   }

   /**
    * Null if reached the end
    */
   public ImageFrame getNext() {
      try {
         //engine decide of the index
         ImageFrameGIF frame = engine.getFrameImage();
         if (frame != null) {
            index = frame.getIndex();
         }
         return frame;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * When resetting to zero.. 2 strategies
    * <li>fast read all remaining frames and reset to zero
    * <li>realod the whole reader.. might not be feasible
    */
   public void resetToZero() {
      if (!engine.isFirstPassDone()) {
         try {
            engine.loadUntilEOF();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      index = 0;
   }

   /**
    * 
    */
   public ImageFrame getPrev() {
      try {
         if (!engine.isFirstPassDone()) {
            engine.loadUntilEOF();
         }
         if (isReverseSupported()) {
            index--;
            if (index < 0) {
               index = engine.getNumFrames() - 1;
            }
            //requires engine to keep frames in memory or it has to be rebuild
            ImageFrameGIF frame = engine.getFrame(index);
            return frame;
         } else {
            return null;
         }
      } catch (IOException e) {
         return null;
      }
   }

   /**
    * Same problem if not seekable right now.... fast forward to make
    * it seekable. 
    * <br>
    * User wants. User gets
    */
   public void resetTo(int index) {
      if (!engine.isFirstPassDone()) {
         try {
            engine.loadUntilEOF();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      this.index = index;
   }

   /**
    * -1 if current not known
    * @return
    */
   public int getNumFrames() {
      if (!engine.isFirstPassDone()) {
         try {
            engine.loadUntilEOF();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return engine.getNumFrames();
   }

   /**
    * True if the number of frames may change over replays.
    * @return
    */
   public boolean isDynamicNumFrames() {
      return false;
   }

   public void resetToEnd() {
      if (getNumFrames() != -1) {
         index = getNumFrames();
      }
   }

   public int getMaxWidthFrames() {
      return engine.getRootWidth();
   }

   public int getMaxHeightFrames() {
      return engine.getRootHeight();
   }

   public boolean hasReachEnd() {
      return true;
   }

   public boolean hasMore() {
      return true;
   }

   public void close() {
      engine.closePlayer();
   }

   /**
    * We can always rebuild a frame from scratch if needed
    */
   public boolean isReverseSupported() {
      //depends on the gif config.. if each frame is its own ok
      //otherwise engine might not support it
      return true;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "ImageFrameProducerGifOptimized");
      toStringPrivate(dc);
      super.toString(dc.sup());
      dc.nlLvlTitleIfNull(engine, "GifEngine");
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ImageFrameProducerGifOptimized");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }
   //#enddebug

}
