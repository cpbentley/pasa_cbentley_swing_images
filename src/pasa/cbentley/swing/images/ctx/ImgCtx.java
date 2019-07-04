package pasa.cbentley.swing.images.ctx;

import java.awt.image.BufferedImage;

import pasa.cbentley.core.src4.ctx.ACtx;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src5.ctx.C5Ctx;
import pasa.cbentley.swing.ctx.SwingCtx;

/**
 * Provides image services. It requires a SwingCtx.
 * 
 * <li> Animation support for {@link BufferedImage}
 * @author Charles Bentley
 *
 */
public class ImgCtx extends ACtx {

   private final SwingCtx sc;


   public ImgCtx(SwingCtx sc) {
      super(sc.getUCtx());
      this.sc = sc;
   }

   
   public C5Ctx getC5() {
      return sc.getC5();
   }


   public SwingCtx getSwingCtx() {
      return sc;
   }

}