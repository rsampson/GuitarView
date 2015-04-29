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
	public Sequence sequence;
	public static List<controlP5.Toggle> chanTog = new ArrayList<controlP5.Toggle>();  // channel filter switches
	public static List<GuitarChannel> gchannels = new ArrayList<GuitarChannel>(); 
	private static int noteCount;

	MidiEngine()
	{
	    try {
	      sequencer = MidiSystem.getSequencer();
	      sequencer.open();

		  MetaEventListener mel = new MetaEventListener() {
				@Override
				public void meta(MetaMessage meta) {
					addNoteAsFingerMarker(meta.getData());
					// handle looping
                    if (GuitarView.loopState == true) {
                    	if (sequencer.getTickPosition() > GuitarView.loopTickMax) {
                    		sequencer.setTickPosition(GuitarView.loopTickMin);
                    		System.out.print("loop endpoint hit");
                    	}
                    }
				} 
			};
	      sequencer.addMetaEventListener(mel);
	      sequencer.setTempoFactor((float).75); // slow down a bit
	    } 
	    catch(Exception e) 
	    {
	      System.err.println("Exception opening sequencer " + e);
	      e.printStackTrace();
	    }
	 }
	
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
					// create available channels and make them visible
					chan = b[0] & 0x0f;					
					if ((l >= 1) && GuitarChannel.isNewChannel(chan)) {
                        gchannels.add(new GuitarChannel(chanTog, chan, trk_num));
					}
					// add note message to meta event track
					try {
						MetaMessage metaMessage = new MetaMessage(com, b, l);
						MidiEvent me2 = new MidiEvent(metaMessage, me.getTick());
						trk.add(me2);
					} catch (Exception e) {
						System.err.println("Can't add Midi events to track " + e);
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void printMessage(byte[] dat) {
		System.out.print(String.format(" cmd %x", dat[0] & 0xf0)
				+ String.format(" ch %x", dat[0] & 0x0f));
		System.out.print(" nt " + dat[1] + " vel " + dat[2]);
		System.out.println();
	}	
	
	private void addNoteAsFingerMarker(byte[] dat) {
		final int command = dat[0] & 0xf0;
		final int channel = dat[0] & 0x0f;
		final int velocity = dat[2];
		int note = dat[1];
		// if (type == NOTE_ON || type == NOTE_OFF) and the note fall on the guitar fret board
		if ((command & 0x80) == 0x80 && note < 77 && note > 39) {
			// only show if channel switch is on
	    if (GuitarChannel.isChannelEnabled(chanTog, channel)) {
	    	    // offset note by number in octave number box
	    	    note = note + (OCTAVE * ((int)GuitarView.getOctaveRadioButton().getValue()) - 2);
				// hacked to fit note on fret board
//				if (note > 76)
//					note = note - OCTAVE;
//				if (note < 40)
//					note = note + OCTAVE;
                
				List<Integer> sf = GuitarView.noteToStringFrets((byte) note);
                FingerMarker fm;
				GuitarChannel guitarchannel = GuitarChannel.get(gchannels, channel);
				if (command == NOTEON && velocity != 0) {
					// pick best possible fingering position
					int[] bestSf = guitarchannel.findClosestFingering(sf);
					fm = GuitarView.fm[bestSf[0]][bestSf[1]];
					fm.setColor(channel); // color marker by channel #
					fm.setChannel(guitarchannel);   // !!!!!!!! may be able to internalize above
					// set finger marker in its channel string position
					markers.add(fm);
					//guitarchannel.getStringStates().set(bestSf[0], fm);
					guitarchannel.getStringStates().add( fm);
					System.out.print("add ");
				} else /* if (command == NOTEOFF || velocity == 0) */ {
					// This is somewhat sketchy, we have lost track of the fingering selected,
					// so erase all possibilities.
					for (int n = 0; n < sf.size(); n = n + 2) {
						fm = GuitarView.fm[sf.get(n)][sf.get(n + 1)];
						fm.setInUse(false);
						//fm.setColor(channel); // color marker by channel #
						if (markers.remove(fm)) {
							System.out.print("rem ");
						} else {
							System.out.print("err ");
						}
						guitarchannel.getStringStates().remove(fm);
				    }
				}
			}
			printMessage(dat);
			// update progress slider periodically
			if (noteCount % 30 == 0){
				GuitarView.getProgSlide().setValue(sequencer.getTickPosition());
			}
			noteCount++;
		}
	}
	
	
	public void loadSequenceFromFile(File selFile)
	{
	    try {
	      sequence = MidiSystem.getSequence(selFile);
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
	      GuitarView.getProgSlide().setRange(0,sequencer.getTickLength());
	      sequencer.start();
	    } 
	    catch(Exception e) 
	    {
	      System.err.println("Exception opening midi file " + e);
	      e.printStackTrace();
	    }
	 }	
}
