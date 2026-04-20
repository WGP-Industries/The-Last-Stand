
import java.io.*;		// for playing sound clips
import java.util.HashMap;
import javax.sound.sampled.*;

public class SoundManager {				// a Singleton class
	HashMap<String, Clip> clips;

	private static SoundManager instance = null;	// keeps track of Singleton instance

	private float volume;

	private SoundManager () {
		clips = new HashMap<String, Clip>();

		// played from start of the game, was gonna use the song 
		// but it was a bit long and can't remember if that was allowed
		Clip clip = loadClip("sounds/background.wav");
			
		clips.put("background", clip);

		clip = loadClip("sounds/hitSound.wav");	// played when the player hits alien or the alien hits treasure
		clips.put("hit", clip);

		clip = loadClip("sounds/hitSound2.wav");	// played when an alien is regenerated at the left or right of the JPanel
		clips.put("appear", clip);


		clip = loadClip("sounds/ghost_die.wav");	// played when the ghost monster dies		
		clips.put("die", clip);
        
		clip = loadClip("sounds/snake_die.wav");	// played when the snake monster dies      
		clips.put("die2", clip);

		clip = loadClip("sounds/player_shoot.wav");	// played when the player shoots a bullet
		clips.put("shoot", clip);


		
		volume = 1.0f;
	}


	public static SoundManager getInstance() {	// class method to retrieve instance of Singleton
		if (instance == null)
			instance = new SoundManager();
		
		return instance;
	}		


    	public Clip loadClip (String fileName) {	// gets clip from the specified file
 		AudioInputStream audioIn;
		Clip clip = null;

		try {
    			File file = new File(fileName);
    			audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL()); 
    			clip = AudioSystem.getClip();
    			clip.open(audioIn);
		}
		catch (Exception e) {
 			System.out.println ("Error opening sound files: " + e);
		}
    		return clip;
    	}


	public Clip getClip (String title) {

		return clips.get(title);
	}


    	public void playClip(String title, boolean looping) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.setFramePosition(0);
			if (looping)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				clip.start();
		}
    	}


    	public void stopClip(String title) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.stop();
		}
    	}

}