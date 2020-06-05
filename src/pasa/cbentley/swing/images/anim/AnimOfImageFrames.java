/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.swing.images.ctx.ImgCtx;

/**
 * An {@link ImageFrame} with instructions on how to read next frame from the {@link ImageFrameProducer}.
 * 
 * <br>
 * <br>
 * The view displaying {@link AnimOfImageFrames} uses {@link AnimOfImageFrames#getAnimWidth()} for
 * sizing itself.
 * @author Charles Bentley
 *
 */
public class AnimOfImageFrames implements IStringable {

   private ImageFrame         currentFrame;

   private boolean            isInverse;

   private boolean            isUpAndDown;

   private ImageFrameProducer producer;

   private ImgCtx           sc;

   private String             source;

   public AnimOfImageFrames(ImgCtx sc, ImageFrameProducer producer) {
      this.sc = sc;
      this.producer = producer;
   }

   public int getAnimHeight() {
      return producer.getMaxHeightFrames();
   }

   public int getAnimWidth() {
      return producer.getMaxWidthFrames();
   }

   /**
    * Null if nextStep
    * @return
    */
   public ImageFrame getCurrentFrame() {
      return currentFrame;
   }

   public int getNumFrames() {
      return producer.getNumFrames();
   }

   public String getSource() {
      return source;
   }

   public boolean isGoingDown() {
      return isInverse;
   }

   public boolean isUpAndDown() {
      return isUpAndDown;
   }

   /**
    * Read animation for next frame.
    * <br>
    * Returns false if its not possible. And current frame returns null
    */
   public boolean nextStep() {
      if (isInverse && producer.isReverseSupported()) {
         currentFrame = producer.getPrev();
         if (currentFrame == null) {
            if (isUpAndDown) {
               isInverse = false;
               producer.resetToZero();
               currentFrame = producer.getNext();
            } else {
               //reverse case
               producer.resetToEnd();
               currentFrame = producer.getPrev();
            }
         }
         //if still false 
         return currentFrame != null;
      } else {
         //advance next frame
         currentFrame = producer.getNext();
         if (currentFrame == null) {
            if (isUpAndDown && producer.isReverseSupported()) {
               isInverse = true;
               producer.resetToEnd();
               currentFrame = producer.getPrev();
            } else {
               producer.resetToZero();
               currentFrame = producer.getNext();
            }
         }
         return currentFrame != null;
      }
   }

   /**
    * Called manually
    */
   public boolean prevStep() {
      if (!isInverse && producer.isReverseSupported()) {
         currentFrame = producer.getPrev();
         if (currentFrame == null) {
            if (isUpAndDown) {
               isInverse = false;
               producer.resetToZero();
               currentFrame = producer.getNext();
            } else {
               //reverse case
               producer.resetToEnd();
               currentFrame = producer.getPrev();
            }
         }
         //if still false 
         return currentFrame != null;
      } else {
         //advance next frame
         currentFrame = producer.getNext();
         if (currentFrame == null) {
            if (isUpAndDown && producer.isReverseSupported()) {
               isInverse = true;
               producer.resetToEnd();
               currentFrame = producer.getPrev();
            } else {
               producer.resetToZero();
               currentFrame = producer.getNext();
            }
         }
         return currentFrame != null;
      }
   }

   /**
    * Only feasible
    */
   public void randomStep() {
      int numFrames = producer.getNumFrames();
      if (numFrames != -1) {
         int index = sc.getUCtx().getRandom().nextInt(numFrames);
         producer.resetTo(index);
      }
   }

   public void reset() {
      producer.resetToZero();
   }

   /**
    * Starts at the end of the frames
    * @param isGoingDown
    */
   public void setGoingDown(boolean isGoingDown) {
      this.isInverse = isGoingDown;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public void setUpAndDown(boolean isUpAndDown) {
      this.isUpAndDown = isUpAndDown;
   }

   //#mdebug
   public IDLog toDLog() {
      return sc.toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "ImageAnimWithProducer");
      toStringPrivate(dc);
      dc.nlLvl(currentFrame, "CurrentFrame");
      dc.nlLvl(producer, "Producer");
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ImageAnimWithProducer");
      toStringPrivate(dc);
      //
      dc.nlLvl(currentFrame, "CurrentFrame");
      dc.nlLvl(producer, "Producer");
   }

   public UCtx toStringGetUCtx() {
      return sc.getUCtx();
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("isInverse", isInverse);
      dc.appendVarWithSpace("isUpAndDown", isUpAndDown);
      dc.appendVarWithSpace("source", source);
   }
   //#enddebug

}
