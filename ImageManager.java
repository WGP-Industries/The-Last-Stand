// ID: 816040879
// ASSIGNMENT: 1
// COURSE: COMP 3609 - Game Programming

import java.awt.Image;
import javax.swing.ImageIcon;

/**
   The ImageManager class manages the loading and processing of images.
*/

public class ImageManager {
      
   	public ImageManager () {

	}

	public static Image loadImage (String fileName) {
		return new ImageIcon(fileName).getImage();
	}

}
