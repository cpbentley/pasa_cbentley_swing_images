package pasa.cbentley.swing.images.anim;

import java.awt.image.BufferedImage;

import pasa.cbentley.swing.images.interfaces.IFrameData;

public class FrameGUI {

   private final IFrameData    frameInfo;

   private final BufferedImage image;

   public IFrameData getFrameInfo() {
      return frameInfo;
   }

   public BufferedImage getImage() {
      return image;
   }

   public FrameGUI(IFrameData frameInfo, BufferedImage image) {
      super();
      this.frameInfo = frameInfo;
      this.image = image;
   }
}
