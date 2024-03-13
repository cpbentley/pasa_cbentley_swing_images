/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.ctx.ToStringStaticUc;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.interfaces.ICallBack;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.thread.IBRunnable;
import pasa.cbentley.core.src4.thread.IBRunnableListener;
import pasa.cbentley.swing.ctx.SwingCtx;
import pasa.cbentley.swing.images.ctx.ImgCtx;
import pasa.cbentley.swing.images.utils.DoubleBuffer;

/**
 * Bridges the {@link AnimationCoordinator} in the UI thread with the
 * @author Charles Bentley
 *
 */
public class ThreadAnimToUI implements IBRunnableListener, ICallBack {

   private AnimationCoordinator animator;

   private DoubleBuffer         buffer;

   private ImageFrame           frameToDraw;

   private ImgCtx               imgc;

   public ThreadAnimToUI(ImgCtx imgc, AnimationCoordinator animator) {
      this.imgc = imgc;
      this.animator = animator;
      bufferUpdate();
   }

   /**
    * Called by the ui when the size has changed
    */
   public void bufferUpdate() {
      Dimension preferredSize = animator.getPreferredSize();
      buffer = new DoubleBuffer(imgc, preferredSize.width, preferredSize.height);
   }

   /**
    * 
    */
   public void callBack(Object o) {
      if (o instanceof ImageFrame) {
         frameToDraw = (ImageFrame) o;
      }
      requestPaintInUIThread();
   }

   public DoubleBuffer getBuffer() {
      return buffer;
   }

   public FrameGUI getFrameToDraw() {
      ImageFrame frame = frameToDraw;
      //load our image from the buffer on which the animator writes frame data
      BufferedImage currentImg = buffer.getFrontBuffer().getImage();

      return new FrameGUI(frame, currentImg);
   }

   public SwingCtx getSC() {
      return imgc.getSwingCtx();
   }

   private void requestPaintInUIThread() {
      getSC().execute(new Runnable() {
         public void run() {
            animator.repaintView();
         }
      });
   }

   public void runnerException(IBRunnable runner, Throwable e) {

      e.printStackTrace();
   }

   public void runnerNewState(IBRunnable runner, int newState) {
      //#debug
      toDLog().pFlow("newState=" + ToStringStaticUc.toStringState(newState), this, AnimationCoordinator.class, "runnerNewState", ITechLvl.LVL_05_FINE, true);

      requestPaintInUIThread();
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "ThreadAnimToUI");
      toStringPrivate(dc);
      dc.nlLvl(frameToDraw, "frameToDraw");
      dc.nlLvl(buffer, "buffer");
      dc.nlLvl(animator, "animator");
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "ThreadAnimToUI");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return imgc.getUC();
   }

   private void toStringPrivate(Dctx dc) {

   }

   //#enddebug

}
