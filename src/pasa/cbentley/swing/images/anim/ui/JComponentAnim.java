/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.anim.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import pasa.cbentley.core.src4.ctx.ToStringStaticUc;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.interfaces.ICallBack;
import pasa.cbentley.core.src4.interfaces.ITechTransform;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.thread.IBRunnable;
import pasa.cbentley.core.src4.thread.IBRunnableListener;
import pasa.cbentley.core.src4.thread.ITechRunnable;
import pasa.cbentley.swing.ctx.SwingCtx;
import pasa.cbentley.swing.images.anim.AnimOfImageFrames;
import pasa.cbentley.swing.images.anim.AnimRunnerProducer;
import pasa.cbentley.swing.images.anim.AnimationCoordinator;
import pasa.cbentley.swing.images.anim.ImageFrame;
import pasa.cbentley.swing.images.anim.ImageFrameGIF;
import pasa.cbentley.swing.images.ctx.ImgCtx;
import pasa.cbentley.swing.images.interfaces.IGifCommadable;
import pasa.cbentley.swing.images.utils.DoubleBuffer;

/**
 * {@link JComponent} wrapper for {@link AnimationCoordinator}.
 * 
 * Each {@link JComponentAnim} has its own thread?
 * 
 * TODO option to use a single thread that control several
 * 
 * <br>
 * TODO Transition effects between an old Animation and a new Animation?
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class JComponentAnim extends JComponent implements IGifCommadable, MouseListener, MouseWheelListener, IStringable, KeyListener, IBRunnableListener, ICallBack {

   /**
    * 
    */
   private static final long  serialVersionUID = 2845905827158978898L;

   private AnimRunnerProducer animRunner;

   private DoubleBuffer       buffer;

   private ImageFrame         frameToDraw;

   /**
    * Possibly null
    */
   private AnimOfImageFrames  ia;

   protected final ImgCtx     imgc;

   private boolean            isShowData;

   private String             loadingMessage;

   private Dimension          preferredSize;

   protected final SwingCtx   sc;

   private int                transform;

   public JComponentAnim(ImgCtx imgc) {
      this.imgc = imgc;
      this.sc = imgc.getSwingCtx();
      preferredSize = new Dimension(200, 200);
      this.addMouseListener(this);
      this.addMouseWheelListener(this);
      this.addKeyListener(this);
   }

   /**
    * Call back in the {@link AnimRunnerProducer}s thread with the current frame
    */
   public void callBack(final Object o) {
      sc.execute(new Runnable() {
         public void run() {
            if (o instanceof ImageFrame) {
               frameToDraw = (ImageFrame) o;
            }
            JComponentAnim.this.repaint();
         }
      });
   }

   public void cmdDebugAnim() {
      //#debug
      toDLog().pAlways("", animRunner, JComponentAnim.class, "cmdDebugAnim", ITechLvl.LVL_05_FINE, false);
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
         imageFrameUpdateFromGUI();
         this.repaint();
      }
   }

   public void cmdFramePrev() {
      if (ia == null) {
         return;
      }
      synchronized (ia) {
         ia.prevStep();
         imageFrameUpdateFromGUI();
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
         animRunner.requestNewState(STATE_1_PAUSED);
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
         ia.randomStep();
         imageFrameUpdateFromGUI();
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
         animRunner.requestNewState(STATE_3_STOPPED);
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
      buffer = new DoubleBuffer(imgc, preferredSize.width, preferredSize.height);
      AnimRunnerProducer anim = new AnimRunnerProducer(imgc, buffer);
      anim.addListener(this);
      anim.setCallback(this);
      return anim;
   }

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

   private void imageFrameUpdateFromGUI() {
      ImageFrame frame = ia.getCurrentFrame();
      buffer.getFrontBuffer().resetTo(frame.getImage());
      frameToDraw = frame;
   }

   public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_SPACE) {
         cmdTogglePlayPause();
      }
   }

   public void keyReleased(KeyEvent e) {

   }

   public void keyTyped(KeyEvent e) {

   }

   public void killAnim() {
      if (animRunner != null) {
         animRunner.requestNewState(STATE_3_STOPPED);
      }
   }

   public void mouseClicked(MouseEvent e) {
      //#debug
      toDLog().pFlow(sc.toSD().d1(e), this, JComponentAnim.class, "mouseClicked", ITechLvl.LVL_04_FINER, true);
   }

   public void mouseEntered(MouseEvent e) {
      //#debug
      toDLog().pFlow(sc.toSD().d1(e), this, JComponentAnim.class, "mouseEntered", ITechLvl.LVL_04_FINER, true);
   }

   public void mouseExited(MouseEvent e) {
      //#debug
      toDLog().pFlow(sc.toSD().d1(e), this, JComponentAnim.class, "mouseExited", ITechLvl.LVL_04_FINER, true);
   }

   public void mousePressed(MouseEvent e) {
      //#debug
      toDLog().pFlow(sc.toSD().d1(e), this, JComponentAnim.class, "mousePressed", ITechLvl.LVL_04_FINER, true);

      if (e.getButton() == MouseEvent.BUTTON1) {
         //pause un pause
      }
   }

   public void mouseReleased(MouseEvent e) {
      //#debug
      toDLog().pFlow(sc.toSD().d1(e), this, JComponentAnim.class, "mouseReleased", ITechLvl.LVL_04_FINER, true);
   }

   public void mouseWheelMoved(MouseWheelEvent e) {
      //#debug
      toDLog().pFlow(sc.toSD().d1(e), this, JComponentAnim.class, "mouseWheelMoved", ITechLvl.LVL_04_FINER, true);

      if (e.getWheelRotation() > 0) {
         cmdFrameNext();
      } else {
         cmdFramePrev();
      }
      this.repaint();
   }

   /**
    * Transform may change preferred size
    */
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Rectangle clipBounds = g.getClipBounds();
      if (ia == null) {
         if (loadingMessage != null) {
            g.drawString(loadingMessage, 40, 40);
         } else {
            g.drawString("No Animated Gif loaded. Drag and drop one here.", 40, 40);
         }
      } else {
         ImageFrame frame = frameToDraw;
         if (frame == null) {
            //#debug
            toDLog().pDraw("", ia, JComponentAnim.class, "paintComponent", ITechLvl.LVL_05_FINE, true);

            g.drawString("Frame is null", 40, 40);
         } else {
            //load our image from the buffer on which the animator writes frame data
            BufferedImage currentImg = buffer.getFrontBuffer().getImage();
            if (currentImg != null) {
               int w = currentImg.getWidth();
               int x = 0;
               if (this.getWidth() > w) {
                  x = (this.getWidth() - w) / 2;
               }
               int h = currentImg.getWidth();
               int y = 0;
               if (this.getHeight() > h) {
                  y = (this.getHeight() - h) / 2;
               }
               sc.getDU().drawRegion((Graphics2D) g, currentImg, 0, 0, currentImg.getWidth(), currentImg.getHeight(), transform, clipBounds.x + x, clipBounds.y + y);
            } else {
               g.drawString("Null Image", 40, 40);
            }

            if (isShowData) {
               int x = 5;

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
               int y = 10;
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

      }

   }

   public void runnerException(IBRunnable runner, Throwable e) {
      //be careful. we are in the runner thread

   }

   public void runnerNewState(IBRunnable runner, int newState) {
      //be careful. we are in the runner thread

      //#debug
      toDLog().pFlow("newState=" + ToStringStaticUc.toStringState(newState), this, JComponentAnim.class, "runnerNewState", ITechLvl.LVL_05_FINE, true);

      sc.execute(new Runnable() {
         public void run() {
            JComponentAnim.this.repaint();
         }
      });
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

         //#debug
         toDLog().pFlow("", this, JComponentAnim.class, "setImage", ITechLvl.LVL_05_FINE, false);

         AnimRunnerProducer animRunner = createAnimRunner();
         animRunner.setAnimation(ia);
         animRunner.setCallback(this);
         this.animRunner = animRunner;
         sc.getExecutorService().execute(animRunner);
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
      return sc.toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "ImageComponent");
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
      dc.root1Line(this, "ImageComponent");
      sc.toSD().d1((JComponent) this, dc);
   }

   public UCtx toStringGetUCtx() {
      return sc.getUC();
   }
   //#enddebug

   public void willStartLoadingNewGif(String name) {
      // TODO Auto-generated method stub

   }

}
