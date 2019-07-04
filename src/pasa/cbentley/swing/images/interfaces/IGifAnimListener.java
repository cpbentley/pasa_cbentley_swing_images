package pasa.cbentley.swing.images.interfaces;

import pasa.cbentley.swing.images.anim.AnimOfImageFrames;

/**
 * Runs in the anim thread.
 * @author Charles Bentley
 *
 */
public interface IGifAnimListener {

   public void gifAnimDidStart(AnimOfImageFrames anim);

   /**
    * The last frame has been display and the new frame will be displayed 
    * as soon as this method returns
    */
   public void gifAnimWillRepeat(AnimOfImageFrames anim);

   public void gifAnimDidStop(AnimOfImageFrames anim);

}
