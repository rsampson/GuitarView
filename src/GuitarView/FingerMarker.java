package GuitarView;

import processing.core.PGraphics;

public class FingerMarker {
	  private GuitarString myString;
	  private final int mySize = 20;
	  private int myColor; 
	  private int x, y;
	  private GuitarView vg;
	  private PGraphics pg;
	  private GuitarChannel gc;
	  // prevent tracers from being drawn over and over
	  private boolean inUse;
	  
	  final private static int black = 0xDF2A1F1F;
	  final private static int brown = 0xDF5A2A12; 
	  final private static int red = 0xDFDF0712;
	  final private static int orange = 0xDFE86309;
	  final private static int yellow = 0xDFFFEF00; 
	  final private static int green = 0xDF10D245;
	  final private static int blue = 0xDF1320D0;
	  final private static int violet = 0xDFBC15C2; 
	  final private static int grey = 0xDFC3C3C3;
	  final private static int white = 0xDFF1EFF7;
	  final private static int teal = 0xDF5CB0AE;

	  final private static int liteblack  = 0xDF272729;
	  final private static int litebrown  = 0xDF78521B; 
	  final private static int litered    = 0xDFE93737;
	  final private static int liteorange = 0xDFF6AF4A;
	  final private static int liteyellow = 0xDFF8D554; 
	  final private static int litegreen  = 0xDF9CF576;
	  //final public static int invisible  = 0x0;

	  // a circle must be on the fret of a string when it is created.
	  FingerMarker(GuitarView vg, PGraphics pg, GuitarString gs, int fret) {
	    myString = gs;
	    if (fret != 0)
	      this.x = GuitarView.fretLines[fret] -  mySize;
	    this.y= myString.getY();
	    this.vg = vg;
	    this.pg = pg;
	    inUse = false;
	  }
	  
 	  private void setColor(int chan) {
 		 myColor = codeColor(chan);
 	  }

 	  public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public void setChannel(GuitarChannel guitarchannel) {
		gc = guitarchannel;
		setColor(gc.getChannel());
	  }

		public static int codeColor(int chan) {
		  switch (chan & 0x0f) {
		  case 0:  
			  return black;
		  case 1:  
			  return brown; 
		  case 2:         
			  return red;
		  case 3:  
			  return orange;
		  case 4:  
			  return yellow; 
		  case 5:       
			  return green;
		  case 6:  
			  return blue;
		  case 7:  
			  return violet;
		  case 8:       
			  return grey;
		  case 9:  
			  return white;
		  case 10:  
			  return liteblack;
		  case 11:  
			  return litebrown; 
		  case 12:         
			  return litered;
		  case 13:  
			  return liteorange;
		  case 14:  
			  return liteyellow; 
		  case 15:       
			  return litegreen;
		  default:  
			  return  teal;
		}
	  }
		
	  // draw the notes as they are playing
	  public void draw() {
		// draw string
		myString.draw(vg.g, myColor);
		// draw marker shadow 
		vg.fill(0, 0, 0);
	    vg.stroke(0, 0, 0);
	    vg.ellipse(x + 3, y + 3, mySize, mySize);
		// draw marker    
		vg.fill(myColor);
	    vg.stroke(myColor);
	    vg.ellipse(x, y, mySize, mySize);
	  }
	  
	  // draw note marker that persist
	public void drawTracer() {
		// only draw once per use
		if (!inUse && GuitarView.getTraceTog().getValue() != 0) {
			int _x, _y;
			setInUse(true);
			pg.beginDraw();
			pg.fill(myColor);
			pg.stroke(myColor);
			pg.ellipse(x + vg.random(-5, 5), y + vg.random(-5, 5), mySize / 2, mySize / 2);
			// draw vector trace
//			pg.strokeWeight(1);
//			_y = GuitarView.strings[gc.getLastStringPlayed()].getY();
//			_x = GuitarView.fretLines[gc.getLastFretPlayed()] - mySize;
//			pg.line(x + vg.random(-3, 3), y + vg.random(-3, 3),
//					_x + vg.random(-3, 3), _y + vg.random(-3, 3));
			pg.endDraw();
		}
	}
}
