package pasa.cbentley.swing.images.anim;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.io.BAByteOS;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.swing.images.ctx.ImgCtx;
import pasa.cbentley.swing.images.utils.WritableBufferedImage;

/**
 * Read {@link ImageFrameGIF} from {@link InputStream} -> {@link ImageReader}.
 * <br>
 * The frames are kept in memory for further reading.
 * <br>
 * This class state is never accessed from the AWT thread.
 * @author Charles Bentley
 *
 */
public class GifEnginePlayOnly implements IStringable {

   private int                   frameIndex                    = 0;

   //builds a list of frame metadata as we play each frames
   private ArrayList<ImageFrameGIF>   frames                        = new ArrayList<ImageFrameGIF>(2);

   private ImgCtx                imgc;

   private boolean               isEndReachedAtLeastOnce       = false;

   /**
    * Computes the size in memory.. if over a threshold, images are dropped
    */
   private boolean               isImageReload;

   private int                   frameIndexOfLastFullFrameSeen = 0;

   private WritableBufferedImage masterImage                   = null;

   private ImageReader           reader;

   private int                   rootHeight                    = -1;

   private int                   rootWidth                     = -1;

   public GifEnginePlayOnly(ImgCtx imgc) {
      this.imgc = imgc;
   }

   public void initMetaData() throws IOException {
      IIOMetadata metadata = reader.getStreamMetadata();
      if (metadata != null) {
         IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

         NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

         if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
            IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

            if (screenDescriptor != null) {
               rootWidth = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
               rootHeight = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
            }
         }
      }
   }

   /**
    * Once a FrameGIF is built.. you only needs to either reload
    * or painted it accordingly.
    * @param frameIndex
    * @param img
    * @return
    * @throws IOException
    */
   private ImageFrameGIF buildFrameGIF(int frameIndex, BufferedImage img) throws IOException {
      if (frameIndex < frames.size()) {
         return frames.get(frameIndex);
      }
      IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
      IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
      int delay = Integer.valueOf(gce.getAttribute("delayTime"));
      //System.out.println("#GifEngine#delay = " + delay);
      String disposal = gce.getAttribute("disposalMethod");

      int x = 0;
      int y = 0;
      NodeList children = root.getChildNodes();
      for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
         Node nodeItem = children.item(nodeIndex);
         if (nodeItem.getNodeName().equals("ImageDescriptor")) {
            NamedNodeMap map = nodeItem.getAttributes();
            x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
            y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
         }
      }

      ImageFrameGIF fg = new ImageFrameGIF(imgc, this, delay, disposal, frameIndex);
      fg.setX(x);
      fg.setY(y);
      fg.setH(img.getHeight());
      fg.setW(img.getWidth());

      fg.setLastFullFrame(frameIndexOfLastFullFrameSeen);
      frames.ensureCapacity(frameIndex);
      frames.add(frameIndex, fg);

      //what do we do with the previous frame?
      if (frameIndex != 0 && masterImage != null) {
         int prevIndex = frameIndex - 1;
         if (prevIndex < 0) {
            prevIndex = frames.size() - 1;
         }
         ImageFrameGIF fg1 = frames.get(prevIndex);

         manageDisposal(fg1, masterImage);
      }

      if (masterImage == null) {
         //we at the first frame.. image with
         masterImage = new WritableBufferedImage(img.getWidth(), img.getHeight());
      }

      //if we draw on master. we cannot do restorToPrevious.
      masterImage.drawImage(img, x, y);

      fg.setFrameImage(masterImage.getImage());
      //we have to take the image and draw it

      //in the first pass.. if we are in fullFrame doNotDispose. we save the new image as root
      //its the equivalent in memory
      fg.setRootImage(img);

      return fg;
   }

   private ImageFrameGIF buildFrameGIFAgain(int frameIndex) throws IOException {

      ImageFrameGIF fg = frames.get(frameIndex);
      if (frameIndex == 0) {
         masterImage.clear();
      } else {
         int prevIndex = frameIndex - 1;
         if (prevIndex < 0) {
            prevIndex = frames.size() - 1;
         }
         ImageFrameGIF fg1 = frames.get(prevIndex);

         //if we have a full frame.. no need for doing anything
         //what do we do of the previsou frame?
         manageDisposal(fg1, masterImage);
      }

      //if we draw on master. we cannot do restorToPrevious.
      masterImage.drawImage(fg.getImageRoot(), fg.getX(), fg.getY());
      fg.setFrameImage(masterImage.getImage());

      //but if we are in do not dispose. we have to draw over! no choice
      //      if (fg.isLastFullFrame()) {
      //         fg.setFrameImage(fg.getImageRoot());
      //      } else {
      //         //we have to build it fr
      //         masterImage.drawImage(fg.getImageRoot(), fg.getX(), fg.getY());
      //         fg.setFrameImage(masterImage.getImage());
      //      }

      return fg;
   }

   public void closePlayer() {
      reader.dispose();
   }

   /**
    * Tries to reach the index.
    * If index cannot be 
    * @param index
    * @return
    */
   public ImageFrameGIF getFrame(int index) {
      ImageFrameGIF fg = frames.get(index);
      if (fg.isLastFullFrame()) {
         //ok we can just set the home as the current
         masterImage.drawImage(fg.getImageRoot(), 0, 0);
         fg.setFrameImage(masterImage.getImage());
      } else {
         masterImage.drawImage(fg.getImageRoot(), fg.getX(), fg.getY());
         fg.setFrameImage(masterImage.getImage());
      }
      return fg;
   }

   public boolean isFirstPassDone() {
      return isEndReachedAtLeastOnce;
   }

   public ImageFrameGIF getFrameImage() throws IOException {
      BufferedImage image;
      try {
         if (isFirstPassDone()) {
            ImageFrameGIF frame = buildFrameGIFAgain(frameIndex);
            frameIndex++;
            return frame;
         } else {
            image = reader.read(frameIndex);
            if (image.getWidth() == this.rootWidth && image.getHeight() == this.rootHeight) {
               frameIndexOfLastFullFrameSeen = frameIndex;
            }
            ImageFrameGIF frame = buildFrameGIF(frameIndex, image);
            frameIndex++;
            return frame;
         }
      } catch (IndexOutOfBoundsException io) {
         //we reached the end. close the reader and use the root Image from now one
         return lastFrameReached();
      } catch (IOException e) {
         //there might an issue with metadata in some GIFs or malformed data
         //assume we reached the end
         e.printStackTrace();
         //#debug
         toDLog().pTest("But its ok. Available GIF frames can run without problem", this, GifEnginePlayOnly.class, "getFrameImage", ITechLvl.LVL_05_FINE, false);
         return lastFrameReached();
      } catch (IllegalArgumentException e) {
         //there might an issue with metadata in some GIFs or malformed data
         //assume we reached the end
         e.printStackTrace();
         //#debug
         toDLog().pTest("But its ok. Available GIF frames can run without problem", this, GifEnginePlayOnly.class, "getFrameImage", ITechLvl.LVL_05_FINE, false);
         return lastFrameReached();
      }
   }

   private ImageFrameGIF lastFrameReached() {
      isEndReachedAtLeastOnce = true; //this will getNumFrames to work
      if (reader != null) {
         reader.dispose();
         reader = null;
      }
      frameIndex = 0;
      //we now know the number of frames
      return null; //use maydecide to run again but that's not our call
   }

   /**
    * Create Frame without drawing them
    * @throws IOException
    */
   public void loadUntilEOF() throws IOException {
      try {
         while (true) {
            BufferedImage image = reader.read(frameIndex);
            if (image.getWidth() == this.rootWidth && image.getHeight() == this.rootHeight) {
               frameIndexOfLastFullFrameSeen = frameIndex;
            }
            buildFrameGIF(frameIndex, image);
            frameIndex++;
         }
      } catch (IndexOutOfBoundsException io) {
         //we reached the end. close the reader and use the root Image from now one
         reader.dispose();
         frameIndex = 0;
         //we now know the number of frames
         isEndReachedAtLeastOnce = true; //this will getNumFrames to work
      }
   }

   public boolean isSeekable() {
      if (isEndReachedAtLeastOnce) {
         return isOnlyFullFrames();
      } else {
         return false;
      }
   }

   private boolean isOnlyFullFrames() {
      //true if all frames are full
      for (ImageFrameGIF frame : frames) {
         if (!frame.isLastFullFrame()) {
            return false;
         }
      }
      return true;
   }

   /**
    * Most of the time, equivalent to seekable
    * @return
    */
   public boolean isReverseSupported() {
      if (isEndReachedAtLeastOnce) {
         return isOnlyFullFrames();
      } else {
         return false;
      }
   }

   public int getNumFrames() {
      if (isEndReachedAtLeastOnce) {
         return frames.size();
      }
      return -1;
   }

   /**
    * What is disposal method? It is simply the answer to the question: <b>What do you do with the previous frame?</b>
    * <br>
    * <br>
    * <li> 0 - No disposal specified. The decoder is not required to take any action.
    * <li> 1 - Do not dispose. The graphic is to be left in place. You draw over it
    * <li> 2 - Restore to background color. The area used by the graphic must be restored to the background color.
    * <li> 3 - Restore to previous. The decoder is required to restore the area overwritten by the graphic with what was there prior to rendering the graphic.
    * The thing to remember about Restore to Previous is that it's not necessarily the first frame of the animation that will be restored but the last frame set to Unspecified or Do Not Dispose.
    * @param disposal
    */
   private void manageDisposal(ImageFrameGIF fg, WritableBufferedImage master) {
      String disposal = fg.getDisposal();
      if (disposal.equals("restoreToPrevious")) {
         BufferedImage from = null;
         //read the last non restoreToPrevious frame
         for (int i = frameIndex - 1; i >= 0; i--) {
            if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
               //what do we have here?
               from = frames.get(i).getImageRoot();
               break;
            }
         }
         if (from != null) {
            master.resetTo(from);
         } else {
            //we do a restoreToBackgroundColor
            master.clearRect(fg.getX(), fg.getY(), fg.getW(), fg.getH());
         }
      } else if (disposal.equals("restoreToBackgroundColor")) {
         master.clearRect(fg.getX(), fg.getY(), fg.getW(), fg.getH());
      } else if (disposal.equals("doNotDispose")) {
         //when disposable is none
         //all images are down over each other.
         //so potentially you need all images to visually (for the user) draw the last one
      } else {
         //none case
      }
   }

   /**
    * Reads the meta data to know the width and height of the "screen"
    * @throws IOException
    */
   public void readMetaData() throws IOException {
      IIOMetadata metadata = reader.getStreamMetadata();
      if (metadata != null) {
         IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

         NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

         if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
            IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

            if (screenDescriptor != null) {
               rootWidth = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
               rootHeight = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
            }
         }
      }
   }

   /**
    * Builds the image back to the given frame.
    * <br>
    * <br>
    * Most of the time, this requires to build the since the last full frame
    * @param frameSeek
    */
   public void seekToFrame(int frameSeek) {
      if (isSeekable()) {
         frameIndex = frameSeek;
      } else {
         throw new IllegalStateException();
      }
   }

   public void setSource(ByteArrayInputStream is) throws IOException {
      //make the stream seakable
      Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("gif");
      ImageInputStream imageInputStream = ImageIO.createImageInputStream(is);
      reader = readers.next();
      reader.setInput(imageInputStream, false);
      initMetaData();
   }

   private void reset() {
      masterImage = null;
      frameIndex = 0;
      frames.clear();
      reader = null;
      frameIndexOfLastFullFrameSeen = 0;
      rootHeight = -1;
      rootWidth = -1;
      isEndReachedAtLeastOnce = false;
   }

   public void setSourceToArray(InputStream is) throws IOException {
      reset();
      BAByteOS bos = imgc.getUCtx().getIOU().convert(is);
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.getArrayRef(), 0, bos.size());
      setSource(bis);
   }

   public void setSource(InputStream is) throws IOException {
      reset();
      //make the stream seakable
      Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("gif");
      ImageInputStream imageInputStream = ImageIO.createImageInputStream(is);
      reader = readers.next();
      reader.setInput(imageInputStream, false);
      initMetaData();
   }

   //#mdebug
   private IDLog toDLog() {
      return imgc.toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "GifEnginePlayOnly");
      toStringPrivate(dc);

      imgc.getC5().toStringListStringable(dc, frames, "GIF Frames");

      if (masterImage == null) {
         dc.nl();
         dc.append("MasterImage is null");
      } else {
         BufferedImage bi = masterImage.getImage();
         dc.nlLvlO(bi, "MasterImage", imgc);
      }

   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("frameIndex", frameIndex);
      dc.appendVarWithSpace("rootWidth", rootWidth);
      dc.appendVarWithSpace("rootHeight", rootHeight);
      dc.appendVarWithSpace("isEndReachedAtLeastOnce", isEndReachedAtLeastOnce);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "GifEnginePlayOnly");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return imgc.getUCtx();
   }
   //#enddebug

   public int getRootWidth() {
      return rootWidth;
   }

   public int getRootHeight() {
      return rootHeight;
   }

}
