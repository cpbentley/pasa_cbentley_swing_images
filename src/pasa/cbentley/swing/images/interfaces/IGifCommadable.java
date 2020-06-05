/*
 * (c) 2018-2020 Charles-Philip Bentley
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */
package pasa.cbentley.swing.images.interfaces;

import pasa.cbentley.swing.images.anim.AnimOfImageFrames;

/**
 * Call methods called in the UI thread
 * @author Charles Bentley
 *
 */
public interface IGifCommadable {

   public void cmdFrameNext();
   
   public void cmdFramePrev();
   
   public void cmdFrameTo(int val);

   public void cmdFaster();

   public void cmdSlower();

   public void cmdPlay();

   public void cmdPause();

   public void cmdStop();

   public void cmdForward();

   public void setImage(AnimOfImageFrames ia);
   /**
    * 
    * @return
    */
   public int getState();

   public void cmdTogglePlayPause();

   public void willStartLoadingNewGif(String name);

   //#debug
   public void cmdDebugAnim();

   public void cmdShowDataToggle();

}
