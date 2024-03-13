/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.ctx;

import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.ctx.ACtx;
import pasa.cbentley.core.src5.ctx.C5Ctx;
import pasa.cbentley.swing.ctx.SwingCtx;

/**
 * Provides image services. Logically it requires a {@link SwingCtx} .
 * 
 * <li> Animation support for {@link BufferedImage}
 * @author Charles Bentley
 *
 */
public class ImgCtx extends ACtx {

   public static final int CTX_ID = 4002;

   private final SwingCtx  sc;

   public ImgCtx(SwingCtx sc) {
      super(sc.getUC());
      this.sc = sc;
   }

   /**
    * 
    * @return
    */
   public C5Ctx getC5() {
      return sc.getC5();
   }

   public int getCtxID() {
      return CTX_ID;
   }

   public SwingCtx getSwingCtx() {
      return sc;
   }

}
