/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.interfaces.ITechTransform;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.thread.ITechRunnable;
import pasa.cbentley.swing.image.DrawUtils;
import pasa.cbentley.swing.images.ctx.ImgCtx;
import pasa.cbentley.swing.images.interfaces.IFrameData;
import pasa.cbentley.swing.images.interfaces.IGifCommadable;
import pasa.cbentley.swing.images.interfaces.IGifRepaintable;

/**
 * Create by a {@link IGifRepaintable} UI component (e.g. JComponentAnim) 
 * Draws on a {@link Graphics2D}.
 * 
 * Each {@link AnimationCoordinator} all method work in the UI thread.
 * 
 * <br>
 * @author Charles Bentley
 *
 */
public class AnimationCoordinator implements IGifCommadable, IStringable {

   /**
    * 
    */
   private static final long  serialVersionUID = 2845905827158978898L;

   private AnimRunnerProducer animRunner;

   /**
    * Where the frame is drawn
    */
   private IGifRepaintable       component;

   /**
    * Possibly null
    */
   private AnimOfImageFrames  ia;

   private ImgCtx             imgc;

   private boolean            isShowData;

   private String             loadingMessage;

   private Dimension          preferredSize;

   protected ThreadAnimToUI   threadAnimToUI;

   private int                transform;

   public AnimationCoordinator(ImgCtx imgc, IGifRepaintable component) {
      this.imgc = imgc;
      if(component == null) {
         throw new NullPointerException();
      }
      this.component = component;
      preferredSize = new Dimension(200, 200); //sets a default size 
      threadAnimToUI = new ThreadAnimToUI(imgc, this);
   }

   public void cmdDebugAnim() {
      //#debug
      toDLog().pAlways("", animRunner, AnimationCoordinator.class, "cmdDebugAnim", ITechLvl.LVL_05_FINE, false);
   }

   public void cmdFaster() {
      if (animRunner != null) {
         float f = animRunner.getSpeedModifier();
         f -= 0.15f;
         animRunner.setSpeedModifier(f);
         //skip current frame. user wants it faster
         cmdForward();
      }
   }

   /**
    * Interrupts current anim frame and force the next one
    */
   public void cmdForward() {
      if (animRunner != null) {
         if (animRunner.getThread() != null) {
            //anim.requestNewState(IBRunnable.STATE_5_INTERRUPTED);
            animRunner.getThread().interrupt();
         }
      }
   }

   public void cmdFrameNext() {
      if (ia == null) {
         return;
      }
      //ask the anim to process the FrameNext task
      synchronized (ia) {
         ia.nextStep();
         //update front buffer since we are in GUI thread
         animRunner.forceFrameNext();
         this.repaint();
      }
   }

   public void cmdFramePrev() {
      if (ia == null) {
         return;
      }
      synchronized (ia) {
         //this call may be done while the anim runner is paused or running
         animRunner.forceFramePrev();
         this.repaint();
      }
   }

   public void cmdFrameTo(int val) {
      // TODO Auto-generated method stub

   }

   public void cmdIsInverse(boolean b) {
      if (ia == null) {
         return;
      }
      synchronized (ia) {
         ia.setGoingDown(b);
      }
   }

   public void cmdIsUpAndDown(boolean b) {
      synchronized (ia) {
         ia.setUpAndDown(b);
      }
   }

   public void cmdLoop() {

   }

   public void cmdPause() {
      if (animRunner != null) {
         animRunner.requestNewState(ITechRunnable.STATE_1_PAUSED);
      } else {

      }
   }

   public void cmdPlay() {
      if (ia == null) {
         return;
      }
      if (animRunner != null) {
         //if anim was stopped
         if (animRunner.getState() == ITechRunnable.STATE_1_PAUSED) {
            animRunner.requestNewState(ITechRunnable.STATE_0_RUNNING);
         } else {
            if (animRunner.getState() == ITechRunnable.STATE_6_FINISHED) {
               setImage(ia);
            }
            //start 
         }
      }
   }

   public void cmdRandom() {
      synchronized (ia) {
         int num = ia.getNumFrames();
         int frameIndex = imgc.getUCtx().getRandom().nextInt(num);
         animRunner.forceFrame(frameIndex);
         this.repaint();
      }
   }

   public void cmdShowDataToggle() {
      isShowData = !isShowData;
   }

   public void cmdSlower() {
      if (ia == null) {
         return;
      }
      if (animRunner != null) {
         float f = animRunner.getSpeedModifier();
         f += 0.15f;
         animRunner.setSpeedModifier(f);
      }
   }

   public void cmdStop() {
      if (animRunner != null) {
         animRunner.requestNewState(ITechRunnable.STATE_3_STOPPED);
         if (ia == null) {
            return;
         }
         synchronized (ia) {
            ia.reset();
         }
      } else {

      }
   }

   public void cmdTogglePlayPause() {
      if (animRunner != null) {
         int state = animRunner.getState();
         if (state == ITechRunnable.STATE_1_PAUSED) {
            cmdPlay();
         } else {
            cmdPause();
         }
      }
   }

   public AnimRunnerProducer createAnimRunner() {
      AnimRunnerProducer anim = new AnimRunnerProducer(imgc, threadAnimToUI.getBuffer());
      anim.addListener(threadAnimToUI);
      anim.setCallback(threadAnimToUI);
      return anim;
   }

   /**
    * Could be null
    * @return
    */
   public AnimOfImageFrames getAnim() {
      return ia;
   }

   public DrawUtils getDU() {
      return imgc.getSwingCtx().getDU();
   }

   /**
    * Return the preferred dimension required to display the animation.
    * <br>
    * Possible impacts
    * <li> the image is scaled up/down
    * <li> the image is mirrored, double the scaled width
    * <li> the image is double mirrored double the width
    * @return
    */
   public Dimension getPreferredSize() {
      return preferredSize;
   }

   public int getState() {
      if (animRunner != null) {
         return animRunner.getState();
      }
      return ITechRunnable.STATE_3_STOPPED;
   }

   public int getTransform() {
      return transform;
   }

   public void killAnim() {
      if (animRunner != null) {
         animRunner.requestNewState(ITechRunnable.STATE_3_STOPPED);
      }
   }

   /**
    * Transform may change preferred size
    */
   public void paintAnimFrame(Graphics g, int x, int y) {
      if (ia == null) {
         if (loadingMessage != null) {
            g.drawString(loadingMessage, 40, 40);
         } else {
            g.drawString("No GIF animation", 40, 40);
         }
      } else {
         FrameGUI frameGUI = threadAnimToUI.getFrameToDraw();
         if (frameGUI == null) {
            //#debug
            toDLog().pDraw("", ia, AnimationCoordinator.class, "paintComponent", ITechLvl.LVL_05_FINE, true);
            g.drawString("frameGUI is null", 40, 40);
         } else {
            //load our image from the buffer on which the animator writes frame data
            BufferedImage currentImg = frameGUI.getImage();
            if (currentImg != null) {
               imgc.getSwingCtx().getDU().drawRegion((Graphics2D) g, currentImg, 0, 0, currentImg.getWidth(), currentImg.getHeight(), transform, x, y);
            } else {
               g.drawString("BufferedImage of FrameGUI is Null", 40, 40);
            }
            paintDebugData(g, x, y, frameGUI.getFrameInfo());
         }

      }

   }

   private void paintDebugData(Graphics g, int x, int y, IFrameData frame) {
      if (isShowData) {
         int h = g.getFontMetrics().getHeight();
         String str1 = "#" + frame.getIndex() + " of " + ia.getNumFrames();
         String str2 = "" + frame.getDisposal();
         String str3 = "delay:" + frame.getDelay();
         String str4 = "";
         if (frame instanceof ImageFrameGIF) {
            ImageFrameGIF frameGIF = (ImageFrameGIF) frame;
            str4 = "[" + frameGIF.getX() + "," + frameGIF.getY() + " - " + frameGIF.getW() + "," + frameGIF.getH();
            g.setColor(Color.WHITE);
            g.drawRect(frameGIF.getX(), frameGIF.getY(), frameGIF.getW(), frameGIF.getH());
         }

         int width1 = g.getFontMetrics().stringWidth(str1);
         int width2 = g.getFontMetrics().stringWidth(str2);
         int width3 = g.getFontMetrics().stringWidth(str3);
         int width4 = g.getFontMetrics().stringWidth(str4);

         g.setColor(Color.WHITE);
         g.fillRect(x, y, width1, h);
         y += h;
         g.fillRect(x, y, width2, h);
         y += h;
         g.fillRect(x, y, width3, h);
         y += h;
         g.fillRect(x, y, width4, h);

         y = 10 + h;
         g.setColor(Color.BLACK);
         g.drawString(str1, x, y);
         y += h;
         g.drawString(str2, x, y);
         y += h;
         g.drawString(str3, x, y);
         y += h;
         g.drawString(str4, x, y);
      }
   }

   /**
    * Request the component to be repainted
    */
   private void repaint() {
      component.requestRepaintPlease();
   }

   public void repaintView() {
      component.requestRepaintPlease();
   }

   /**
    * Modify where to draw the debug data
    * @param xDebug
    * @param yDebug
    */
   public void setDebugXY(int xDebug, int yDebug) {

   }

   /**
    * Sets the ImageAnim and start playing it
    * @param ia
    */
   public void setImage(AnimOfImageFrames ia) {
      this.ia = ia;
      if (ia != null) {
         //we need to know some data
         preferredSize = new Dimension(ia.getAnimWidth(), ia.getAnimHeight());

         threadAnimToUI.bufferUpdate();
         //#debug
         toDLog().pFlow("", this, AnimationCoordinator.class, "setImage", ITechLvl.LVL_05_FINE, false);

         AnimRunnerProducer animRunner = createAnimRunner();
         animRunner.setAnimation(ia);
         animRunner.setCallback(threadAnimToUI);
         this.animRunner = animRunner;
         imgc.getSwingCtx().getExecutorService().execute(animRunner);
      }
   }

   public void setLoadingMessage(String message) {
      ia = null;
      this.loadingMessage = message;
   }

   /**
    * Update preferred size
    * @param transform
    */
   public void setTransform(int transform) {
      if (ia != null) {
         int w = ia.getAnimWidth();
         int h = ia.getAnimHeight();
         switch (transform) {
            case ITechTransform.TRANSFORM_4_MIRROR_ROT270:
            case ITechTransform.TRANSFORM_5_ROT_90:
            case ITechTransform.TRANSFORM_6_ROT_270:
            case ITechTransform.TRANSFORM_7_MIRROR_ROT90:
               preferredSize = new Dimension(h, w);
               break;
            default:
               preferredSize = new Dimension(w, h);
               break;
         }
      }
      this.transform = transform;
   }

   /**
    * Each frame may have a transition effect applied
    */
   public void setTransitionEffects() {

   }

   //#mdebug
   public IDLog toDLog() {
      return imgc.toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "AnimDrawerWithProducer");
      dc.appendVarWithSpace("loadingMessage", loadingMessage);
      dc.appendVarWithSpace("transform", transform);
      dc.appendVarWithSpace("pw", preferredSize.width);
      dc.appendVarWithSpace("ph", preferredSize.height);
      dc.nlLvlTitleIfNull(ia, "ImageAnim");
      dc.nlLvlTitleIfNull(animRunner, "AnimRunnable");

   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "AnimDrawerWithProducer");
   }

   public UCtx toStringGetUCtx() {
      return imgc.getUCtx();
   }
   //#enddebug

   public void willStartLoadingNewGif(String name) {
      //if nothing. display a loading message
      loadingMessage = "Loading " + name;
   }

}
