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
	private int track;
	private int channel;
	private List<FingerMarker> stringStates;
    

	GuitarChannel(List<controlP5.Toggle> togl, int chan, int track){
		channels.add(chan);
		this.track = track;
		channel = chan;
		stringStates = new CopyOnWriteArrayList<FingerMarker>();
		
//        for (int s = 0; s < GuitarView.NUM_STRINGS; s++) {
//        	FingerMarker fm = new FingerMarker(null, GuitarView.guitarImage, GuitarView.strings[s], 0);
//        	fm.setColor(FingerMarker.invisible);
//        	stringStates.add(fm);
//        }
        
        togl.add( GuitarView.cp5.addToggle("chan" + chan)
				.setPosition((5 + chans) * GuitarView.dX, 2 * GuitarView.dY)
				.setSize(20, 20)
				.setValue(true)
				.setColorActive(FingerMarker.codeColor(chan)));
        chans++;
	}
	
	public List<FingerMarker> getStringStates() {
		return stringStates;
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
    
	public int getTrackhan() {
		return channel | ((track & 0x0f) << 4);  // make a hybrid of track/channel
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
		// for each fingering pair in sf
		for (int i = 0; i < sf.size() / 2; i = i + 2) {
			// "length" is not a measure in cartesian space, but a measure of difficulty in
			// reaching a note from another position.
			length = /* PApplet.sqrt */ ((float)1.5 * (PApplet.sq(sf.get(i) - lastStringPlayed))
					+ PApplet.sq(sf.get(i + 1) - lastFretPlayed));
			if (length < shortestLength) {
				shortestLength = length;
				result[0] = sf.get(i);
				result[1] = sf.get(i + 1);
			}
		}
		lastStringPlayed = result[0];
//		System.out.print(" last string ch "+ channel +" is "+ lastStringPlayed +" ");
		lastFretPlayed = result[1];
//		System.out.print(" last fret "+" is "+ lastFretPlayed +" ");
		return result;
	}
	
	public static GuitarChannel get(List<GuitarChannel> channels, int channel) {
		return channels.get(getIndexOfChannel(channel));
	}
} 
	

