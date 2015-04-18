package GuitarView;

/* OpenProcessing Tweak of *@*http://www.openprocessing.org/sketch/42730*@* */
/* !do not delete the line above, required for linking your tweak if you upload again */
 
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PConstants;
import java.io.File;
import controlP5.*;
import java.util.ArrayList;
import java.util.List;

public class GuitarView extends PApplet {

	public int[] fretLines = new int[NUM_FRETS]; // x locations of all frets
	public int guitarX, guitarY, guitarH; // parameters for the guitar
	public static final int NUM_STRINGS = 6; // number of strings on guitar
	public static final int NUM_FRETS = 13; // number of frets shown on fret board
	public static final int NUM_CHANNELS = 16; // max number of channels in sequence
	public final int copper = color(100, 80, 30); 
	public final int brass = color(181, 166, 66); 
	private final int ivory = color(0xFFEEEBB0);
	private GuitarString[] strings = new GuitarString[6];;
	private PGraphics guitarImage; // for keeping the drawn image of the guitar.
	private PImage img;
    private MidiEngine me;
    public static FingerMarker[][] fm = new FingerMarker[NUM_STRINGS][NUM_FRETS];  //pre-made markers for each string  and fret
    public static ControlP5 cp5;
    //private static controlP5.Toggle[] tracTog;  // track filter switches
    private static controlP5.Toggle   traceTog; // enable/disable tracing
    
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

	// print the note produced by string s and fret f at location x y
	private static String getNoteString(int s, int f) {
		return (noteNames[noteNumbers[(s * GuitarView.NUM_FRETS) + f] % 12]);
	}

	// print the note produced by string s and fret f at location x y
	private void printNote(int s, int f, int x, int y) {
		guitarImage.text(getNoteString(s, f), x, y);
	}
	
//	public static int[] noteToStringFret(byte n) {
//		boolean found = false;
//		int[] finger = new int[2];
//		// scan fret board for a match on n
//		for (int f = 0; f < NUM_FRETS; f++) {
//			for (int s = 0; s < NUM_STRINGS; s++) {
//				if (noteNumbers[(s * NUM_FRETS) + f] == n) {
//					// add the marker that was found to the array
//					finger[0] = s;
//					finger[1] = f;
//					found = true;
//					break; // take first match
//				}
//			}
//			if (found == true) break;
//		}
//		return finger;
//	}
	
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
//		guitarImage.fill(ivory);
//		guitarImage.beginShape();
//		guitarImage.vertex(0, guitarY);
//		guitarImage.vertex(guitarX, guitarY);
//		guitarImage.vertex(guitarX, guitarY + guitarH);
//		guitarImage.vertex(0, guitarY + guitarH);
//		guitarImage.endShape(PConstants.CLOSE);
		
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
//		 if (frame != null) {
//		    frame.setResizable(true);
//		  }	
		guitarImage = createGraphics(width, height, P2D);
		img = loadImage("woodgrain3.jpg");
		
		guitarX = height / 20;
		guitarY = height / 2 + 20;
		guitarH = height / 3 + 60;
//		guitarX = 0;
//		guitarY = height / 2 ;
//		guitarH = height / 3 ;
		
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
	    
		traceTog = cp5.addToggle("trace").setPosition(70, 140)
				.setSize(20, 20).setValue(true);
		cp5.addToggle("pause").setPosition(70, 180).setSize(20, 20)
				.setValue(true);
		cp5.addButton("reset").setPosition(70, 220).setSize(35, 20)
		         .setColorCaptionLabel(0xffffffff);
		cp5.addButton("load_file").setPosition(70, 30).setSize(55, 20)
                 .setColorCaptionLabel(0xffffffff);
		 
	    cp5.addSlider("tempo")
        .setRange((float).25,(float)1.5)
        .setValue((float).75)
        .setPosition(70, 100)
        .setSize(150, 20)
	    .setNumberOfTickMarks(15);
	    
	    cp5.addFrameRate().setInterval(10).setPosition(0,height - 10);

	    //cp5.getTooltip().register("tempo","Slide with mouse to change tempo");
	    
		drawGuitar(); // setup guitar image in buffer
		initFingerMarkers();
	}
	
	public void fileSelected(File selection) {
		  if (selection == null) {
		    println("Window was closed or the user hit cancel.");
		  } 
		  else {
		    println("User selected " + selection.getAbsolutePath());
		    me.loadSequenceFromFile( selection);
		  }	
	}

	private void tempo(float theTempo) { // change tempo with slider
		if (me.sequencer != null) {
			me.sequencer.setTempoFactor(theTempo);
		}
	}
	
	private void pause(boolean theValue) {  // start and stop sequencer with  toggle
      if (me.sequencer != null && me.sequence != null) {
		if (theValue == true) {
			me.sequencer.start();
		} else {
			me.sequencer.stop();
		}
	  }
	    cp5.saveProperties(("properties"));
	    //cp5.loadProperties(("properties"));
	}
	
	private void reset() { // reset sequencer button
		if (me.sequencer != null) {
			me.sequencer.setTickPosition(0);
			me.markers.clear();
			if (guitarImage != null) {
				guitarImage.beginDraw();
				drawGuitar();
				image(guitarImage, 0, 0);
				guitarImage.endDraw();
			}
		}
	}
	
	private void load_file() { // load midi file button
		if (me.sequencer != null) {
			me.sequencer.stop();
			trace();
		}
		selectInput("Select a Midi file to play:", "fileSelected");
	}

	private void trace() { // clear guitar image
    // hack for null pointer
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
	
	public void draw() {
		guitarImage.beginDraw();
		image(guitarImage, 0, 0); // render image of guitar from buffer
		guitarImage.endDraw();

		// draw finger markers
		if (me != null && me.markers != null) {
			for (FingerMarker m : me.markers) {
				m.draw();
				if (getTraceTog().getValue() != 0) m.drawTracer();
			}
		}
		//cp5.draw();  // we need to manually draw controlP5 when using P2D
	}

	public static void main(String _args[]) {
		PApplet.main(new String[] { GuitarView.class.getName() });
	}
}
