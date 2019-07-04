package pasa.cbentley.swing.images.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class WritableBufferedImage {

   private BufferedImage masterBufferedImage = null;

   private Graphics2D    masterGraphics      = null;

   public WritableBufferedImage(int width, int height) {
      init(width, height);
   }

   /**
    * Init with w and h and 0 background color
    * @param width
    * @param height
    */
   public void init(int width, int height) {
      masterBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      masterGraphics = masterBufferedImage.createGraphics();
      masterGraphics.setBackground(new Color(0, 0, 0, 0));
   }

   public void clear() {
      masterGraphics.clearRect(0, 0, masterBufferedImage.getWidth(), masterBufferedImage.getHeight());
   }

   public BufferedImage getImage() {
      return masterBufferedImage;
   }

   public void drawImage(BufferedImage img, int x, int y) {
      masterGraphics.drawImage(img, x, y, null);

   }

   public void resetTo(BufferedImage from) {
      masterGraphics.clearRect(0, 0, masterBufferedImage.getWidth(), masterBufferedImage.getHeight());
      drawImage(from, 0, 0);
   }

   public void clearRect(int x, int y, int w, int h) {
      masterGraphics.clearRect(x, y, w, h);
   }
}
