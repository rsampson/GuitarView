package GuitarView;

// some parts taken from: http://stackoverflow.com/questions/27987400/how-to-get-note-on-off-messages-from-a-midi-sequence
import java.io.File;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import controlP5.ControlP5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import processing.core.PApplet;

public class MidiEngine {
	private static final int OTHER = -1;
	private static final int NOTE_ON = 1;
	private static final int NOTE_OFF = 2;
	private static final int NOTEON = 0x90;
	private static final int NOTEOFF = 0x80;
	private static final int OCTAVE = 12;
	public File selectedfile;
	public List<FingerMarker> markers = new CopyOnWriteArrayList<FingerMarker>();
	public Sequencer sequencer;
	private List<Integer> channels = new ArrayList<Integer>();
	private int chans = 0;  // total number of channels found
	private controlP5.Toggle  tog;
	private static List<controlP5.Toggle> chanTog = new ArrayList<controlP5.Toggle>();  // channel filter switches

// Iterates the MIDI events of the first track and if they are a NOTE_ON or
// NOTE_OFF message, adds them to the second track as a Meta event.
	private void addNotesToTrack(Track track, Track trk, int trk_num) // throws InvalidMidiDataException
	{ 
		int chan;
		for (int ii = 0; ii < track.size(); ii++) {
			MidiEvent me = track.get(ii);
			MidiMessage mm = me.getMessage();
			if (mm instanceof ShortMessage) {
				ShortMessage sm = (ShortMessage) mm;
				int command = sm.getCommand();
				int com = OTHER;
				if (command == ShortMessage.NOTE_ON) {
					com = NOTE_ON;
				} else if (command == ShortMessage.NOTE_OFF) {
					com = NOTE_OFF;
				}
				if (com != OTHER) {
					byte[] b = sm.getMessage();
					int l = (b == null ? 0 : b.length);
					if (l > 3) System.out.println(" Running status");
					// make available channels visible
					chan = b[0] & 0x0f;					
//					chan = chan | ((trk_num & 0x0f) << 4);  // make a hybrid of track/channel
					if ((l >= 1) && !channels.contains(chan)) {
                        channels.add(chan);
//                        System.out.println("Found channels");
//            			for (Integer itgr : channels) {
//            				System.out.println(" " + itgr + " ");
//            			}
//            			System.out.println();
                        chanTog.add( GuitarView.cp5.addToggle("chan" + chan)
								.setPosition(70 + (chans * 40), 60)
								.setSize(20, 20)
								.setValue(true)
								.setColorActive(FingerMarker.codeColor(chan))
						);
						chans++; // this is a new channel
					}
					
					try {
						MetaMessage metaMessage = new MetaMessage(com, b, l);
						MidiEvent me2 = new MidiEvent(metaMessage, me.getTick());
						trk.add(me2);
					} catch (Exception e) {
						System.err.println("Can't add Midi events to track");
					}
				}
			}
		}
	}
	
	private void printMessage(byte[] dat) {
		System.out.print(String.format(" command: %x", dat[0] & 0xf0)
				+ String.format(" channel: %x", dat[0] & 0x0f));
		System.out.print(" note: " + dat[1] + " velocity: " + dat[2]);
		System.out.println();
	}	
	
	
	private void addNoteAsFingerMarker(byte[] dat) {
		final int command = dat[0] & 0xf0;
		final int channel = dat[0] & 0x0f;
		final int velocity = dat[2];
		int note = dat[1];

		// if (type == NOTE_ON || type == NOTE_OFF)
		if ((command & 0x80) == 0x80) {
			// only show if channel switch is on
		if (chanTog.get(channels.indexOf(channel)).getState() == true) {
				// hacked to fit note on fret board
//				if (note < 76)
//					note = note - OCTAVE;
//				if (note > 40)
//					note = note + OCTAVE;

				int[] sf = GuitarView.noteToStringFrets((byte) note);
				FingerMarker fm;
				fm = GuitarView.fm[sf[0]][sf[1]];
				fm.setColor(channel); // color marker by channel #

				if (command == NOTEON && velocity != 0) {
					markers.add(fm);
					System.out.print(" add ");
				} else if (command == NOTEOFF || velocity == 0) {
					if (markers.remove(fm)) {
						System.out.print(" remove ");
					} else {
						System.out.print(" note remove fail ");
					}
				}
			}
			printMessage(dat);
		}
	}
	
	MidiEngine()
	{
	    try {
	      sequencer = MidiSystem.getSequencer();
	      sequencer.open();

		  MetaEventListener mel = new MetaEventListener() {

				@Override
				public void meta(MetaMessage meta) {
//					final byte[] dat = meta.getData();
					addNoteAsFingerMarker(meta.getData());
				} 
			};

	      sequencer.addMetaEventListener(mel);
	      sequencer.setTempoFactor((float).75); // slow down a bit
	    } 
	    catch(Exception e) 
	    {
	      System.err.println("Exception opening sequence");
	    }
	 }
	
	public void loadSequenceFromFile(File selFile)
	{
	    try {
	      Sequence sequence = MidiSystem.getSequence(selFile);
	      // scan all tracks and add notes
	      Track[] tracks = sequence.getTracks();
	      System.out.println("There are " + tracks.length + " tracks");
	      Track trk = sequence.createTrack();
	      int t = 0;
	      for (Track track : tracks) {
	        addNotesToTrack(track, trk, t);
	        t++;
	      }
	      sequencer.setSequence(sequence);
	      sequencer.start();
	    } 
	    catch(Exception e) 
	    {
	      System.err.println("Exception opening midi file");
	    }
	 }	
}
