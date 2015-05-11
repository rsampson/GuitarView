package GuitarView;

/* Used guitar draw algorithm from OpenProcessing *@*http://www.openprocessing.org/sketch/42730*@* */
 
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PConstants;
import java.io.File;
import controlP5.*;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Sequencer;
import javax.swing.JOptionPane;

public class GuitarView extends PApplet {
	
	public static final int NUM_STRINGS = 6; // number of strings on guitar
	public static final int NUM_FRETS = 13; // number of frets shown on fret board
	public static final int NUM_CHANNELS = 16; // max number of channels in sequence

	public static int[] fretLines = new int[NUM_FRETS]; // x locations of all frets
	public static GuitarString[] strings = new GuitarString[6];;

	public static int guitarX, guitarY, guitarH; // parameters for the guitar
	public static int dX, dY; // grid to put controls on

	public final int copper = color(100, 80, 30); 
	public final int brass = color(181, 166, 66); 
	private final int ivory = color(0xFFEEEBB0);
	
	private  static PGraphics guitarImage; // for keeping the drawn image of the guitar.
	private PImage img;
    private MidiEngine me;
	private static Slider progSlide;
    public static FingerMarker[][] fm = new FingerMarker[NUM_STRINGS][NUM_FRETS];  //pre-made markers for each string  and fret
    
    public static ControlP5 cp5;
    //private static controlP5.Toggle[] tracTog;  // track filter switches
    private static controlP5.Toggle   traceTog; // enable/disable tracing
    private static controlP5.Toggle   pauseTog;  
    public static long loopTickMax, loopTickMin;     // looping end points
    public static boolean loopState = false;
    private static controlP5.RadioButton octaveRadioButton;
	// map note numbers to string and fret position. Will change for different guitar tunings
	// array is organized by string for a 13 fret section of the fret board.
    
    private final static int[] noteNumbers = { 
			40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,	51, 52, 
			45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
			50, 51,	52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 
			55, 56, 57, 58, 59, 60,	61, 62, 63, 64, 65, 66, 67, 
			59, 60, 61, 62, 63, 64, 65, 66, 67, 68,	69, 70, 71, 
			64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76 };

	private final static String[] noteNames = { "c ", "c#", "d ", "d#", "e ", "f ", "f#",
			"g ", "g#", "a ", "a#", "b " };
	
	private static String filename = " ";
	private static PFont font;
	
	// print the note produced by string s and fret f at location x y
	private static String getNoteString(int s, int f) {
		return (noteNames[noteNumbers[(s * GuitarView.NUM_FRETS) + f] % 12]);
	//	return String.valueOf(noteNumbers[(s * GuitarView.NUM_FRETS) + f]);
	}

	// print the note produced by string s and fret f at location x y
	private void printNote(int s, int f, int x, int y) {
		guitarImage.text(getNoteString(s, f), x, y);
	}
	
	// return where note may be found on fret board. There may be up to 3 matches
	public static List<Integer> noteToStringFrets(byte n) {
		List<Integer> finger = new ArrayList<Integer>();
		int match = 0;
		// scan fret board for a match on n scanned lowest to highest pitch
		for (int f = 0; f < NUM_FRETS; f++) {
			for (int s = 0; s < NUM_STRINGS; s++) {
				if (noteNumbers[(s * NUM_FRETS) + f] == n) {
					// add the marker that was found to the array
					finger.add(match, s);
					finger.add(match + 1, f);
					match = match + 2;
					break; // take first match, there is only one per string
				}
			}
		}
		return finger;
	}

	private void initFingerMarkers() {
		for (int s = 0; s < NUM_STRINGS; s++) {
			for (int f = 0; f < NUM_FRETS; f++) {
				// Initialize each object
				fm[s][f] = new FingerMarker(this, guitarImage, strings[s], f);
			}
		}
	}
	
	public static controlP5.Toggle getTraceTog() {
		return traceTog;
	}

	public static controlP5.RadioButton getOctaveRadioButton() {
		return octaveRadioButton;
	}

	private void drawGuitar() {
		// figure out where the frets should go
		float d = (float) (1.47 * width / fretLines.length);
		for (int i = 0; i < fretLines.length; ++i) {
			fretLines[i] = (int) (i * d + guitarX);
			d -= (width * .003); // the frets get closer together as we move up the board
		}
		guitarImage.beginDraw();
		//guitarImage.background(0xFFF2E781);
		// draw fret board
		guitarImage.noStroke();
		guitarImage.beginShape();
		guitarImage.textureMode(PConstants.NORMAL);
		guitarImage.texture(img);
		guitarImage.vertex(guitarX, guitarY, 0, 0);
		guitarImage.vertex(width, guitarY, 1, 0);
		guitarImage.vertex(width, guitarY + guitarH, 1, 1);
		guitarImage.vertex(guitarX, guitarY + guitarH, 0, 1);
		guitarImage.endShape(PConstants.CLOSE);
        // erase background left of neck
		guitarImage.fill(ivory);
		guitarImage.beginShape();
		guitarImage.vertex(0, guitarY);
		guitarImage.vertex(guitarX, guitarY);
		guitarImage.vertex(guitarX, guitarY + guitarH);
		guitarImage.vertex(0, guitarY + guitarH);
		guitarImage.endShape(PConstants.CLOSE);
		
		// draw frets, and fret markers
		for (int i = 0; i < fretLines.length; ++i) {
			guitarImage.strokeWeight(6);

			// draw fret shadows
			guitarImage.stroke(18, 16, 6);
			guitarImage.fill(18, 16, 6);
			guitarImage.line(fretLines[i] + 4, guitarY, fretLines[i] + 4, guitarY
					+ guitarH);
			// then draw frets
			guitarImage.stroke(brass);
			guitarImage.fill(brass);
			guitarImage.line(fretLines[i], guitarY, fretLines[i], guitarY
					+ guitarH);

			// draw note names
			guitarImage.fill(255, 255, 127, 100);
			for (int s = 0; s < NUM_STRINGS; s++) {
				printNote(s, i, fretLines[i] - 15, strings[s].getY() - 5); 
			}

			// draw markers
			guitarImage.fill(0, 0, 0);
			guitarImage.noStroke();
			if (i == 3 || i == 5 || i == 7 || i == 9)
				guitarImage.ellipse((fretLines[i] + fretLines[i - 1]) / 2,
						guitarY + guitarH / 2, 18, 18);
			else if (i == 12) {
				guitarImage.ellipse((fretLines[i] + fretLines[i - 1]) / 2,
						guitarY + guitarH / 6, 18, 18);
				guitarImage.ellipse((fretLines[i] + fretLines[i - 1]) / 2,
						guitarY + 5 * guitarH / 6, 18, 18);
			}
		}
		// draw strings
		for (GuitarString s : strings) {
			s.draw();
		}
		image(guitarImage, 0, 0); // render guitar image from buffer
		guitarImage.endDraw();
	}

	public void setup() {
		size(1200, 600, P2D);
//		 size(displayWidth, displayHeight, P2D);
		 if (frame != null) {
		    frame.setResizable(true);
		  }	
		background(255);  
		guitarImage = createGraphics(width, height, P2D);
		img = loadImage("woodgrain3.jpg");
		
		guitarX = height / 20;
		guitarY = height / 2 + 20;
		guitarH = height / 3 + 60;
        dX = width / 30;
        dY = height / 15;
		
		int inc = guitarH / 22; // for string y-positions

		// initialize strings
		strings[0] = new GuitarString(this, guitarImage, guitarY + inc * 21);
		strings[1] = new GuitarString(this, guitarImage, guitarY + inc * 17);
		strings[2] = new GuitarString(this, guitarImage, guitarY + inc * 13);
		strings[3] = new GuitarString(this, guitarImage, guitarY + inc * 9);
		strings[4] = new GuitarString(this, guitarImage, guitarY + inc * 5);
		strings[5] = new GuitarString(this, guitarImage, guitarY + inc);
		
	    me = new MidiEngine();

		cp5 = new ControlP5(this);
		 
	    cp5.setColorLabel(0xFF000000);
	    
		cp5.addButton("load_file").setPosition(dX, dY).setSize(55, 20)
        .setColorCaptionLabel(0xffffffff);
 		traceTog = cp5.addToggle("trace").setPosition(dX, 2 * dY)
				.setSize(35, 20).setValue(false);
		cp5.addButton("all").setPosition(2 * dX, 2 * dY).setSize(35, 20)
        .setColorCaptionLabel(0xffffffff);
         cp5.addButton("none").setPosition(3 * dX, 2 * dY).setSize(35, 20)
         .setColorCaptionLabel(0xffffffff);
		
		pauseTog = cp5.addToggle("pause").setPosition(6 * dX, 3 * dY).setSize(35, 20)
				.setValue(false);
		cp5.addButton("reset").setPosition(7 * dX, 3 * dY).setSize(35, 20)
		         .setColorCaptionLabel(0xffffffff);
		cp5.addButton("fwd").setPosition(8 * dX, 3 * dY).setSize(35, 20)
        .setColorCaptionLabel(0xffffffff);
		cp5.addButton("rev").setPosition(9 * dX, 3 * dY).setSize(35, 20)
        .setColorCaptionLabel(0xffffffff);
		cp5.addButton("Loop").setPosition(10 * dX, 3 * dY).setSize(35, 20)
        .setColorCaptionLabel(0xffffffff);
		 
	    cp5.addSlider("tempo")
        .setRange((float).25,(float)1.5)
        .setValue((float).75)
        .setPosition(dX, 3 * dY)
        .setSize(150, 20);

	    progSlide = cp5.addSlider("progress")
        .setRange((float)0,(float)1)
        .setValue((float)0)
        .setPosition(11 * dX, 3 * dY)
        .setSize(150, 20).setDecimalPrecision(0);
   
//	    octaveRadioButton = cp5.addRadioButton("octave")
//	            .setPosition(dX, 4 * dY)
//	            .setSize(35,20)
//	            .setItemsPerRow(3)
//	            .setSpacingColumn(20)
//	            .addItem("-",-1)
//	            .addItem("0",0)
//	            .addItem("+",1)
//	            .activate(1)
//	            ;

	    //cp5.getTooltip().register("tempo","Slide with mouse to change tempo");
		cp5.loadProperties(("properties"));

		drawGuitar(); // setup guitar image in buffer
		initFingerMarkers();
		
		// The font must be located in the sketch's 
		// "data" directory to load successfully
//		font = loadFont("ArialMT-16.vlw");
//		textFont(font, 16);
//		textSize(25);
//		fill(0xFF000000);
//		text("Guitar View", dX + 1, 26);
//		fill(brass);
//		text("Guitar View", dX, 25);
//		textSize(12);
//		fill(0xFF000000);
//		text("v0.01 R. Sampson 2015", (25 * dX) + 1, 26);
//		fill(brass);
//		text("v0.01 R. Sampson 2015", 25 * dX, 25);
		frameRate(20);
	}
	
	public static Slider getProgSlide() {
		return progSlide;
	}

	public void fileSelected(File selection) {
		  if (selection == null) {
		    println("Window was closed or the user hit cancel.");
		  } 
		  else {
			filename = selection.getAbsolutePath();  
		    println("User selected " + filename);
		    this.reset();
		    me.loadSequenceFromFile( selection);
		  }	
	}

	private void tempo(float theTempo) { // change tempo with slider
		if (me.sequencer != null) {
			me.sequencer.setTempoFactor(theTempo);
		}
	}
	
	private void progress(float thePosition) { // change position in sequence
		Sequencer seq = me.sequencer;
		if (seq != null && seq.getSequence() != null) {
			seq.stop();
			seq.setTickPosition((long) thePosition);
			seq.start();
		}
	}
	
	private void pause(boolean theValue) { // start and stop sequencer with
		// save pause points for looping
		Sequencer seq = me.sequencer;

		if (seq != null && me.sequence != null) {
			if (theValue == false) {
				seq.start();
			} else {
				seq.stop();
				if (loopState != true) {
					loopTickMin = loopTickMax;
					loopTickMax = seq.getTickPosition();
					loopTickMin = (long) min(loopTickMax, loopTickMin);
					loopTickMax = (long) max(loopTickMax, loopTickMin);
				} 
				loopState = false;
			}
		}
	}
	
	private void reset() { // reset sequencer button
		if (me.sequencer != null) {
			me.sequencer.setTickPosition(0);
		}
		getProgSlide().setValue(0);
	    loopTickMax = 0;
	    loopTickMin = 0;     // looping end points
	    loopState = false;
	    pauseTog.setValue(false);
	    clearFretboard();
	}

	private void Loop() { // reset sequencer button
		if (me.sequencer != null) {
			me.sequencer.setTickPosition(loopTickMin);
		}
		if (loopTickMax != 0) {
			loopState = true;
		    pauseTog.setValue(false);
		}
	}

	
	private void fwd() { // reset sequencer button
		Sequencer seq = me.sequencer;
		if (seq != null) {
			seq.setTickPosition(seq.getTickPosition() + 5000);
		}
	}
	
	private void rev() { // reset sequencer button
		Sequencer seq = me.sequencer;
		if (seq != null) {
			seq.setTickPosition(seq.getTickPosition() - 5000);
		}
	}
	
	private void load_file() { // load midi file button
		if (me.sequencer != null) {
			me.sequencer.stop();
			// enable all channels
			for (controlP5.Toggle t : MidiEngine.chanTog) {
               t.setValue(true);
			}
		}
		clearFretboard();
		selectInput("Select a Midi file to play:", "fileSelected");
	}

	private void all() { // turn all channel toggles on
		for (controlP5.Toggle t : MidiEngine.chanTog) {
			t.setValue(true);
		}
		clearFretboard();
	}	

	private void none() { // turn all channel toggles on
		for (controlP5.Toggle t : MidiEngine.chanTog) {
			t.setValue(false);
		}
		clearFretboard();
	}	
	
	private void clearFretboard() {
		if (me.sequencer != null) {
			me.markers.clear();
			if (guitarImage != null) {
				guitarImage.beginDraw();
				drawGuitar();
				image(guitarImage, 0, 0);
				guitarImage.endDraw();
			}
		}
	}
	
	private void trace() {
		clearFretboard();
	}
	
	public void draw() {
		background(255);
		guitarImage.beginDraw();
		image(guitarImage, 0, 0); // render image of guitar from buffer
		guitarImage.endDraw();

		textSize(25);
		fill(0xFF000000);
		text("Guitar View", dX + 1, 26);
		fill(brass);
		text("Guitar View", dX, 25);
		textSize(12);
		fill(0xFF000000);
		text("v0.01 R. Sampson 2015", (25 * dX) + 1, 26);
		fill(brass);
		text("v0.01 R. Sampson 2015", 25 * dX, 25);
		
		
		textSize(16);
	    fill(0xFF000000);
	    text(filename, 3 * dX, height / 11);
//		for (GuitarChannel c : MidiEngine.gchannels) {
//			for ( int s = 0; s < NUM_STRINGS; s++) {
//		      c.getStringStates().get(s).draw();
//			}
//		}
			
		// draw finger markers
		if (me != null && me.markers != null) {
			for (FingerMarker m : me.markers) {
				m.draw();
				if (getTraceTog().getValue() != 0) m.drawTracer();
			}
		}
	}

	public static void main(String _args[]) {
		PApplet.main(new String[] { GuitarView.class.getName() });
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				cp5.saveProperties(("properties"));
			}
		}, "Shutdown-thread"));
	}
}
