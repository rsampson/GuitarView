package GuitarView;

/* Used some of the guitar drawing algorithm from :
"Play Guitar" by Karl Hiner, licensed under Creative Commons Attribution-Share Alike 3.0 and GNU GPL license.
Work: http://openprocessing.org/visuals/?visualID= 42730
License:
http://creativecommons.org/licenses/by-sa/3.0/
http://creativecommons.org/licenses/GPL/2.0/
*/

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PConstants;
import java.io.File;
import controlP5.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.Sequencer;

public class GuitarView extends PApplet {

	private static final long serialVersionUID = 3036968534111084926L;
	public static final int NUM_STRINGS = 6; // number of strings on guitar
	public static final int NUM_FRETS = 13; // number of frets shown on fret board
	public static final int NUM_CHANNELS = 16; // max number of channels in sequence

	public static int[] fretLines = new int[NUM_FRETS]; // x locations of all frets
	public static GuitarString[] strings = new GuitarString[6];;

	public static int guitarX, guitarY, guitarH; // parameters for the guitar
	public static int dX, dY; // grid to put controls on

	public final int copper = color(100, 80, 30); 
	public final int brass = color(220, 200, 150); 
	private final int darkbrass = color(100, 90, 60); 
	private final int wood = color(83, 31, 1);
	
	private  static PGraphics guitarImage; // for keeping the drawn image of the guitar.
    private MidiEngine me;
	private static Slider progSlide;
    public static FingerMarker[][] fm = new FingerMarker[NUM_STRINGS][NUM_FRETS];  //pre-made markers for each string  and fret
    
    private PFont pfont; // use true/false for smooth/no-smooth
    private ControlFont font;

    public static ControlP5 cp5;
     private static Toggle   traceTog; // enable/disable tracing
    private static Toggle   pauseTog;  
    private static Textlabel tuningLabel;
    public static Textarea myTextarea;
    public static long loopTickMax, loopTickMin;     // looping end points
    public static boolean loopState = false;
    //private static controlP5.RadioButton octaveRadioButton;
    private static MultiList tuneList;  // selects various guitar tunings
    private static MultiListButton regular;
    private static MultiListButton instrumental;
    private static MultiListButton open;

    // String tunings in Midi note numbers
    // taken from: http://sethares.engr.wisc.edu/alternatetunings/alternatetunings.html
    // regular tunings
    private final static int[] standard =          { 40, 45, 50, 55, 59, 64};  // e a d g b e 
    private final static int[] majorThird =        { 48, 52, 56, 60, 64, 68};  // c e g# c e g#
    private final static int[] allFourths =        { 40, 45, 50, 55, 60, 65};  // e a d g c f 
    private final static int[] augmentedFourths =  { 36, 42, 48, 54, 60, 66};  // c f# c f# c f# 
    private final static int[] mandoGuitar =       { 26, 43, 50, 57, 64, 71};  // c g d a e b
    // instrumental tunings
    private final static int[] dobro =             { 43, 47, 50, 55, 59, 62};  // g b d g b d
    private final static int[] overtone =          { 48, 52, 55, 58, 60, 62};  // c e g a# c d
    private final static int[] pentatonic =        { 45, 48, 50, 52, 55, 57};  // a c d e g a
    // open tunings
    private final static int[] openC =             { 36, 43, 48, 55, 60, 64};  // c g c g c e
    private final static int[] openD =             { 38, 45, 50, 54, 57, 62};  // d a d f# a d
    private final static int[] openG =             { 38, 43, 50, 55, 59, 62};  // d g d g b d
    private final static int[] openDMinor =        { 38, 45, 50, 53, 57, 62};  // d a d f a d 
    private final static int[] openA =             { 40, 45, 49, 52, 57, 64};  // e a c# e a e

    private static int[] noteNumbers;  // fret board mapping
    
	private final static String[] noteNames = { "C ", "C#", "D ", "D#", "E ", "F ", "F#",
		"G ", "G#", "A ", "A#", "B " };
	
	private static String filename = " ";
	
	private static boolean loadFlag = false;
	

	// set up fret board note mapping
	private void initNotes(int[] t) {
		noteNumbers = new int[NUM_STRINGS * NUM_FRETS];
        int i = 0;
        int n = 0;
		for (int s = 0; s < NUM_STRINGS; s++) {
            n = t[s];
			for (int f = 0; f < NUM_FRETS; f++) {
                noteNumbers[i++] = n++;
			}
		}
	}
	
	// return the note name produced by string s and fret f
	public static String getNoteName(int note) {
		return noteNames[note % 12];
	}
	
	// return the note name produced by string s and fret f
	public static String getNoteNameWithOctave(int note) {
		return noteNames[note % 12] + "  " + ((note / 12) - 1);
	}

	// return the note name produced by string s and fret f
	private static String getNoteString(int s, int f) {
		return  getNoteName(noteNumbers[(s * NUM_FRETS) + f]);
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

	// create a cache of pre made finger markers
	private void initFingerMarkers() {
		for (int s = 0; s < NUM_STRINGS; s++) {
			for (int f = 0; f < NUM_FRETS; f++) {
				// Initialize each object
				fm[s][f] = new FingerMarker(this, guitarImage, strings[s], f);
			}
		}
	}

	private void clearTracers() {
		for (int s = 0; s < NUM_STRINGS; s++) {
			for (int f = 0; f < NUM_FRETS; f++) {
				fm[s][f].setInUse(false);
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
		// draw fret board
		guitarImage.noStroke();
		guitarImage.fill(wood);
		guitarImage.beginShape();
		
		guitarImage.vertex(guitarX, guitarY);
		guitarImage.vertex(width, guitarY);
		guitarImage.vertex(width, guitarY + guitarH);
		guitarImage.vertex(guitarX, guitarY + guitarH);
		guitarImage.endShape(PConstants.CLOSE);
        // erase background left of neck
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
			guitarImage.stroke(darkbrass);
			guitarImage.fill(darkbrass);
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
				printNote(s, i, fretLines[i] - 21, strings[s].getY() - 5); 
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
			s.draw(guitarImage, copper);
		}
		guitarImage.endDraw();
	}


	public void setup() {
		size(1200, 600, PConstants.JAVA2D);
//		 size(displayWidth, displayHeight, P2D);
		 if (frame != null) {
		    frame.setResizable(true);
		  }	
		background(200,50);
		smooth();
		initNotes(standard);
		
		guitarImage = createGraphics(width, height);
		
		guitarX = height / 20;
		guitarY = height / 2 + 20;
		guitarH = height / 3 + 60;
        dX = width / 30;
        dY = height / 18;
		
		int inc = guitarH / 22; // for string y-positions

		// initialize strings
		strings[0] = new GuitarString(this,  guitarY + inc * 21);
		strings[1] = new GuitarString(this,  guitarY + inc * 17);
		strings[2] = new GuitarString(this,  guitarY + inc * 13);
		strings[3] = new GuitarString(this,  guitarY + inc * 9);
		strings[4] = new GuitarString(this,  guitarY + inc * 5);
		strings[5] = new GuitarString(this,  guitarY + inc);
		
	    me = new MidiEngine();
		drawGuitar(); // setup guitar image in buffer
		initFingerMarkers();
		configureUI();
		frameRate(20);
	}

	private void configureUI() {
		cp5 = new ControlP5(this);
		
		cp5.setAutoDraw(false);    // control drawing manually to avoid concurrency issues 

	    cp5.setColorCaptionLabel(0xFF000000);
	    
	    pfont = createFont("Arial",13,true); 
	    font = new ControlFont(pfont);
	    
	    ControlFont.sharp();
	    
	    configFont(cp5.addButton("load_file")
		.setPosition(dX, dY)
		.setSize(55, 20))
        ;
		
		
 		traceTog = cp5.addToggle("trace")
 				.setPosition(dX, 3 * dY)
				.setSize(35, 20).setValue(false)
			    ;
 		configFont(traceTog);
 		
 		configFont(cp5.addButton("all")
		   .setPosition(2 * dX, 3 * dY)
		   .setSize(35, 20))
          ;
		 
 		configFont(cp5.addButton("none")
           .setPosition(3 * dX, 3 * dY)
           .setSize(35, 20))
           ;
 		
		cp5.addTextlabel("labelvisual")
        .setText("visual display controls")
        .setPosition(dX,2 * dY + 8)
        .setColorValueLabel(copper)
        .setFont(createFont("arial",18))
        ;
		
 		cp5.addTextlabel("labelplay")
        .setText("play controls")
        .setPosition(dX, 4 * dY + 8)
        .setColorValueLabel(copper)
        .setFont(createFont("arial",18))
        ;
 		
 		tuningLabel = cp5.addTextlabel("labeltuning")
        .setText("tuning controls")
        .setPosition(width / 2 + 8 * dX, dY + 8)
        .setColorValueLabel(copper)
        .setFont(createFont("arial",18))
        ;
 		
 		cp5.addTextlabel("labelauthor")
        .setText("v0.01 R. Sampson 2015")
        .setPosition(26 * dX + 1, 10)
        .setColorValueLabel(copper)
        .setFont(createFont("arial",11))
        ;
		
		pauseTog = cp5.addToggle("pause")
				.setPosition(6 * dX, 5 * dY)
				.setSize(35, 20)
				.setValue(false)
				;
		
		configFont(pauseTog);
		
 		configFont(cp5.addButton("reset")
 		  .setPosition(7 * dX, 5 * dY)
		  .setSize(35, 20))
		  ;
		
          configFont(cp5.addButton("Loop")
		  .setPosition(8 * dX, 5 * dY)
		  .setSize(35, 20))
          ;
		 
	    cp5.addSlider("tempo")
          .setRange((float).25,(float)1.5)
          .setValue((float).75)
          .setPosition(dX, 5 * dY)
          .setSize(150, 20)
          .getCaptionLabel()
          .setFont(font)
          .toUpperCase(false)
          .setSize(13)
          ;

	    progSlide = cp5.addSlider("progress")
          .setRange((float)0,(float)0)
          .setValue((float)0)
          .setPosition(9 * dX, 5 * dY)
          .setSize(150, 20)
          .setDecimalPrecision(0)
          ;
	    progSlide.getCaptionLabel()
        .setFont(font)
        .toUpperCase(false)
        .setSize(13);

	      
        myTextarea = cp5.addTextarea("txt")
                      .setPosition(width / 2 + 2 * dX,2 * dY)
                      .setSize(200,200)
                      .setFont(createFont("arial",20))
                      .setLineHeight(16)
                      .setColor(color(128))
                      .setColorBackground(color(255,100))
                      .setColorForeground(color(255,100))
                      ;


          // add list of tuning selections
          tuneList = cp5.addMultiList("tunings",width / 2 + 8 * dX,2 * dY,130, 25);
          
          regular = tuneList.add("regular_tunings",1);
          configFont(regular);

          instrumental = tuneList.add("instrumental_tunings",2);
          configFont(instrumental);

          open = tuneList.add("open_tunings",3);
          configFont(open);  
          
          configFont(regular.add("Standard",11).setCaptionLabel("Standard Tuning"));
          configFont(regular.add("MajorThird",12).setCaptionLabel("Major Third"));
          configFont(regular.add("AllFourths",13).setCaptionLabel("All Fourths"));
          configFont(regular.add("AugmentedFourths",14).setCaptionLabel("Augmented Fourths"));
          configFont(regular.add("MandoGuitar",15).setCaptionLabel("Mando Guitar"));

          configFont(instrumental.add("Dobro",21).setCaptionLabel("Dobro Tuning"));
          configFont(instrumental.add("Overtone",22).setCaptionLabel("Overtone Tuning"));
          configFont(instrumental.add("Pentatonic",23).setCaptionLabel("Pentatonic Tuning"));
          
          configFont(open.add("OpenA",35).setCaptionLabel("Open A"));
          configFont(open.add("OpenC",31).setCaptionLabel("Open C"));
          configFont(open.add("OpenD",32).setCaptionLabel("Open D"));
          configFont(open.add("OpenDMinor",33).setCaptionLabel("Open D Minor"));
          configFont(open.add("OpenG",34).setCaptionLabel("Open G"));

          
	    cp5.getTooltip().register("load_file","Browse midi files to play");
	    cp5.getTooltip().register("all","Enable display of all channels (voices)");
	    cp5.getTooltip().register("none","Supress display off all channels");
	    cp5.getTooltip().register("pause","Pause music play");
	    cp5.getTooltip().register("Loop","loop play between the last two pause points");
	    cp5.getTooltip().register("progress","Slide with mouse to change play position");
	    cp5.getTooltip().register("tempo","Slide with mouse to change tempo");
	    cp5.getTooltip().register("reset","Play music from te begining");
	    cp5.getTooltip().register("trace","Turn on visual indication of which notes have been played");
	    cp5.getTooltip().register("tunings","Configure special guitar tunings");
	    myTextarea.clear();
	}

	@SuppressWarnings("rawtypes")
	private void configFont(controlP5.Controller cc) {
		cc.getCaptionLabel()
          .setFont(font)
          .toUpperCase(false)
          .setSize(13).setColor(0xffffffff);
	}
	
	// handle gui events
	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isController()) {

			print("control event from : " + theEvent.getName() + " \n");
			
			switch (theEvent.getName()) {

			case "Standard":
				initNotes(standard);
				tuningLabel.setText("Standard");
				clearFretboard();
				break;
			case "MajorThird":
				initNotes(majorThird);
				tuningLabel.setText("MajorThird");
				clearFretboard();
				break;
			case "AllFourths":
				initNotes(allFourths);
				tuningLabel.setText("AllFourths");
				clearFretboard();
				break;
			case "AugmentedFourths":
				initNotes(augmentedFourths);
				tuningLabel.setText("AugmentedFourths");
				clearFretboard();
				break;
			case "MandoGuitar":
				initNotes(mandoGuitar);
				tuningLabel.setText("MandoGuitar");
				clearFretboard();
				break;
				
			case "Dobro":
				initNotes(dobro);
				tuningLabel.setText("Dobro");
				clearFretboard();
				break;
			case "Overtone":
				initNotes(overtone);
				tuningLabel.setText("Overtone");
				clearFretboard();
				break;
			case "Pentatonic":
				initNotes(pentatonic);
				tuningLabel.setText("Pentatonic");
				clearFretboard();
				break;
				
			case "OpenC":
				initNotes(openC);
				tuningLabel.setText("OpenC");
				clearFretboard();
				break;
			case "OpenD":
				initNotes(openD);
				tuningLabel.setText("OpenD");
				clearFretboard();
				break;
			case "OpenG":
				initNotes(openG);
				tuningLabel.setText("OpenG");
				clearFretboard();
				break;
			case "OpenDMinor":
				initNotes(openDMinor);
				tuningLabel.setText("OpenDMinor");
				clearFretboard();
				break;
			case "OpenA":
				initNotes(openA);
				tuningLabel.setText("OpenA");
				clearFretboard();
				break;
			}
		}
	}
	
	public static Slider getProgSlide() {
		return progSlide;
	}

	public void fileSelected(File selection) {
		if (selection == null) {
			println("Window was closed or the user hit cancel.");
		} else {
			filename = selection.getAbsolutePath();
			println("User selected " + filename);
			this.reset();
			fill(copper);
			textSize(16);
			text("loading, please wait...", dX, 7 * dY);
			loadFlag = true;
			me.loadSequenceFromFile(selection);
			loadFlag = false;
		}
		clearFretboard();
	}

	@SuppressWarnings("unused")
	private void tempo(float theTempo) { // change tempo with slider
		if (me.sequencer != null) {
			me.sequencer.setTempoFactor(theTempo);
		}
	}
	
	@SuppressWarnings("unused")
	private void progress(float thePosition) { // change position in sequence
		Sequencer seq = me.sequencer;
		// update the progress slider when under human control
		if (seq != null && seq.getSequence() != null  && mouseButton == LEFT ) {
			seq.stop();
			seq.setTickPosition((long)thePosition);
			seq.start();
		}
	}
	
	@SuppressWarnings("unused")
	private void pause(boolean theValue) { // start and stop sequencer with
		// save pause points for looping
		if (me.sequence != null) {
			if (theValue == false) {
				me.sequencer.start();
			} else {
				me.sequencer.stop();
				if (loopState != true) {
					loopTickMin = loopTickMax;
					loopTickMax = me.sequencer.getTickPosition();
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

	@SuppressWarnings("unused")
	private void Loop() { // reset sequencer button
		if (me.sequencer != null) {
			me.sequencer.setTickPosition(loopTickMin);
		}
		if (loopTickMax != 0) {
			loopState = true;
		    pauseTog.setValue(false);
		}
		clearFretboard();
	}

	
	@SuppressWarnings("unused")
	private void load_file() { // load midi file button
		if (me.sequencer != null) {
			me.sequencer.stop();
		}
		// enable all channels
		all();
		selectInput("Select a Midi file to play:", "fileSelected");
	}

	private void all() { // turn all channel toggles on
		for (controlP5.Toggle t : MidiEngine.chanTog) {
			t.setValue(true);
		}
		clearFretboard();
	}	

	@SuppressWarnings("unused")
	private void none() { // turn all channel toggles on
		for (controlP5.Toggle t : MidiEngine.chanTog) {
			t.setValue(false);
		}
		clearFretboard();
	}	
	
	private void clearFretboard() {
		if (me.sequencer != null) {
			me.clearFingerMarkers();
			if (guitarImage != null) {
				drawGuitar();
			}
		}
		clearTracers();
	}
	
	@SuppressWarnings("unused")
	private void trace() {
		clearFretboard();
	}
	
	public void draw() {
		background(200,50);
		image(guitarImage, 0, 0); // render image of guitar from buffer

		textSize(25);
		fill(copper);
		text("Guitar View", dX, 25);
		textSize(16);
	    text(filename, 3 * dX, height / 11);
	    
	    if (/*!me.busy() ||*/ !loadFlag){
	    	cp5.draw();
	    }
	    
		// draw finger markers on fret board	    
		for (GuitarChannel c : MidiEngine.gchannels) {
			for (int s = 0; s < NUM_STRINGS; s++) {
				FingerMarker fm = c.getStringStates().get(s);
				if (fm != null) {
					fm.draw();
					fm.drawTracer();
				}
			}
		}
	}
	

	public static void main(String _args[]) {
		PApplet.main(new String[] { GuitarView.class.getName() });
	}
}
