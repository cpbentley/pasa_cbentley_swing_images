/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.interfaces.ICallBack;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.thread.AbstractBRunnable;
import pasa.cbentley.core.src4.thread.IBProgessable;
import pasa.cbentley.swing.images.ctx.ImgCtx;
import pasa.cbentley.swing.images.utils.DoubleBuffer;
import pasa.cbentley.swing.images.utils.WritableBufferedImage;

/**
 * Runnable in its own animation thread for a {@link AnimOfImageFrames}.
 * <li>can be paused with {@link AbstractBRunnable#requestNewState(int)}.
 * <br>
 * <br>
 * Runs according to the {@link ImageAnim#nextStep()} method
 * @author Charles Bentley
 *
 */
public class AnimRunnerProducer extends AbstractBRunnable {

   private ICallBack         cb;

   private AnimOfImageFrames imageAnim;

   protected final ImgCtx    imgc;

   private float             speedModifier = 1.0f;

   private ImageFrame        frameLastSent;

   private BufferedImage     frameImageLastSent;

   /**
    * Cannot be null
    */
   private DoubleBuffer      buffer;

   public AnimRunnerProducer(ImgCtx imgc, DoubleBuffer buffer) {
      super(imgc.getUC());
      this.imgc = imgc;
      if (buffer == null) {
         throw new NullPointerException();
      }
      this.buffer = buffer;
   }

   public float getSpeedModifier() {
      return speedModifier;
   }

   /**
    * Called from GUI.. create a new user task in queue.
    * 
    * even if the worker is paused
    */
   public void forceFrameNext() {
      // TODO Auto-generated method stub
      //2 cases
      if (isStatePaused()) {

      }
      imageAnim.nextStep();
   }

   public void forceFrame(int index) {

   }

   public void forceFramePrev() {
      // TODO Auto-generated method stub
      //2 cases
      if (isStatePaused()) {

      }
      imageAnim.prevStep();
   }

   public void runAbstract() {
      if (imageAnim == null) {
         //#debug
         toDLog().pFlow("imageAnim is null. Please set anim before starting runner. Aborting...", this, AnimRunnerProducer.class, "runAbstract", ITechLvl.LVL_05_FINE, true);
         return;
      }
      getThread().setName("AnimRunner");

      //the anim may decide to stop because it has reached the last of its frame by itself
      while (isContinue()) {

         //sync on anim to modify its state
         synchronized (imageAnim) {
            boolean isContinue = imageAnim.nextStep();
            //#debug
            //toDLog().pFlow("isContinue=" + isContinue, ia, AnimRunnerProducer.class, "runAbstract", IDLog.LVL_05_FINE, true);
            if (!isContinue) {
               return;
            }
         }
         //get the first frame. preload next in another thread?
         ImageFrame frame = imageAnim.getCurrentFrame();

         //#debug
         toDLog().pFlow("CurrentFrame", frame, AnimRunnerProducer.class, "runAbstract", ITechLvl.LVL_05_FINE, true);

         if (frameImageLastSent != null) {
            //do a transition effect between the 2 frames?

         }
         //create a double buffer. this frame 
         BufferedImage image = frame.getImage();
         WritableBufferedImage backBuffer = buffer.getBackBuffer();
         backBuffer.resetTo(image);
         buffer.swapImages();
         //
         //ImageFrame frameForGui = frame.cloneForGui(buffer.getBackBuffer());
         //we have to push a copy of this frame to the GUI thread

         //send the frame for display
         if (cb != null) {
            cb.callBack(frame);
         }
         frameLastSent = frame;
         //wait the frame delay before querying the next frame
         try {
            int currentFrameDelay = (int) (frame.getDelay() * speedModifier);
            long waitTime = currentFrameDelay * 10;
            if (waitTime == 0) {
               waitTime = 2; //minimum waiting time
            }
            Thread.sleep(waitTime);
         } catch (InterruptedException e) {
            //what should we do here? it depends.
            //1: user willingly interrupted (skip action) to make it run faster
            //because a frame has a long running time 

            //#debug
            imgc.toDLog().pFlow("Sleep Interrupted", this, AnimRunnerProducer.class, "runAbstract", ITechLvl.LVL_05_FINE, true);
         }
      }
   }

   public void setCallback(ICallBack cb) {
      this.cb = cb;
   }

   /**
    * Once set, any thread that wants to modify its state must
    * synchronize on {@link ImageAnim} instance.
    * @param ia
    */
   public void setAnimation(AnimOfImageFrames ia) {
      this.imageAnim = ia;
   }

   /**
    * only set it if positive
    * @param f
    */
   public void setSpeedModifier(float f) {
      if (f >= 0.0f) {
         this.speedModifier = f;
      }
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "AnimRunnerProducer");
      toStringPrivate(dc);
      super.toString(dc.sup());
      dc.nlLvlTitleIfNull(imageAnim, "ImageAnim");
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("speedModifier", speedModifier);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "AnimRunnerProducer");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }
   //#enddebug

}
