/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
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
