package GuitarView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

// corresponds to a single midi channel played on a guitar
public class GuitarChannel {
	private static List<Integer> channels = new ArrayList<Integer>();
	private static int chans;  // total number of channels found
    private int lastFretPlayed;
	private int lastStringPlayed;
	private int channel;
	private List<FingerMarker> stringStates;
    private int[][] noteFingerings ;

	GuitarChannel(List<controlP5.Toggle> togl, int chan){
		channels.add(chan);
		channel = chan;
		lastFretPlayed = 7; // initialize fret and string near enter of neck
		lastStringPlayed = 3;
		stringStates = new CopyOnWriteArrayList<FingerMarker>();
		noteFingerings = new int[GuitarView.NUM_STRINGS * GuitarView.NUM_STRINGS][2];
		
		for (int s = 0; s < GuitarView.NUM_STRINGS; s++) {
		   	  stringStates.add(null);
		    }
        
        togl.add( GuitarView.cp5.addToggle("chan" + chan)
				.setPosition((5 + chans) * GuitarView.dX, 3 * GuitarView.dY)
				.setSize(20, 20)
				.setValue(true)
				.setColorActive(FingerMarker.codeColor(chan)));
        chans++;
	}

	public void clearStringStates() {
		for (int s = 0; s < GuitarView.NUM_STRINGS; s++) {
		   	  stringStates.set(s, null);
		    }
	}
	
	public int[] getNoteFingerings(int note) {
		return noteFingerings[note - 40];
	}

	public void setNoteFingerings(int[] noteFingerings, int note) {
		this.noteFingerings[note - 40] = noteFingerings;
	}
    
	// channel destructor
	public static void killChannels(List<GuitarChannel> gcl, List<controlP5.Toggle> togl){
		channels.clear();
	    chans  = 0;
		for (controlP5.Toggle t : togl) {
			t.remove();
		}
        togl.clear();
        gcl.clear();
 	}
	
	public List<FingerMarker> getStringStates() {
		return stringStates;
	}
    public void setStringState(int s, FingerMarker fm) {
	   getStringStates().set(s, fm);
    }
    
	public static boolean isChannelEnabled(List<controlP5.Toggle> parent, int channel) {
	  return parent.get(channels.indexOf(channel)).getState() == true;
	}
	
	public static boolean isNewChannel(int chan){
	  return !channels.contains(chan);
	}
	
    public static int getChans() {
		return chans;
	}
    
	public int getChannel() {
		return channel;
	}

	public int getLastFretPlayed() {
		return lastFretPlayed;
	}
	
	public void setLastFretPlayed(int lastFretPlayed) {
		this.lastFretPlayed = lastFretPlayed;
	}

	public int getLastStringPlayed() {
		return lastStringPlayed;
	}

	public void setLastStringPlayed(int lastStringPlayed) {
		this.lastStringPlayed = lastStringPlayed;
	}

	public static int getIndexOfChannel(int channel) {
	  return channels.indexOf(channel);
	}
	
	public int[] findClosestFingering(List<Integer> sf) {
		int[] result = {0,0};
		float length = 0;
		float shortestLength = 9999999;
		//int lastFret = 0;
		//GuitarView.myTextarea.append(String.format(" lst fret %d \n", lastFretPlayed), 12);

		// for each fingering pair in sf
		for (int i = 0; i < sf.size() / 2; i = i + 2) {
			// "length" is not a measure in cartesian space, but a measure of difficulty in
			// reaching a note from another position, try the following formula.
			//length = 2 * (sf.get(i + 1) - lastFretPlayed)  + sf.get(i) - lastStringPlayed;
			length = PApplet.abs(sf.get(i + 1) - (6 + lastFretPlayed) / 2);
			if (length < shortestLength || sf.get(i + 1) == 0) {
				shortestLength = length;
				result[0] = sf.get(i);
				result[1] = sf.get(i + 1);
				//lastFret = sf.get(i + 1);
			}
			// you can't do better than an open string, quit if you find one
			//if (lastFret == 0) break;
		}
		// the last string and fret have some "memory" as to where they were in the past
		// this will mess up vectoring. Don't remember open strings
		//if (lastFret != 0) {
//			lastStringPlayed = (result[0] + lastStringPlayed) / 2;
//			lastFretPlayed = (result[1] + lastFretPlayed) / 2;
		lastStringPlayed = result[0];
		lastFretPlayed = result[1];
		//}
		return result;
	}
	
	public static GuitarChannel get(List<GuitarChannel> channels, int channel) {
		return channels.get(getIndexOfChannel(channel));
	}
} 
	

