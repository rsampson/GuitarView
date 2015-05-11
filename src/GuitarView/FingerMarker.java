package GuitarView;

import processing.core.PGraphics;

public class FingerMarker {
	  private GuitarString myString;
	  private int mySize = 20;
	  private int myColor; 
	  private int x, y;
	  private GuitarView vg;
	  private PGraphics pg;
	  private GuitarChannel gc;
	  private boolean inUse;
	  
	  final static int black = 0xDF2A1F1F;
	  final static int brown = 0xDF5A2A12; 
	  final static int red = 0xDFDF0712;
	  final static int orange = 0xDFE86309;
	  final static int yellow = 0xDFFFEF00; 
	  final static int green = 0xDF10D245;
	  final static int blue = 0xDF1320D0;
	  final static int violet = 0xDFBC15C2; 
	  final static int grey = 0xDFC3C3C3;
	  final static int white = 0xDFF1EFF7;
	  final static int teal = 0xDF5CB0AE;

	  final static int liteblack  = 0xDF272729;
	  final static int litebrown  = 0xDF78521B; 
	  final static int litered    = 0xDFE93737;
	  final static int liteorange = 0xDFF6AF4A;
	  final static int liteyellow = 0xDFF8D554; 
	  final static int litegreen  = 0xDF9CF576;
	  final public static int invisible  = 0x0;
	  
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
	  
 	  public void setColor(int chan) {
 		 myColor = codeColor(chan);
 	  }

 	  public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public void setChannel(GuitarChannel guitarchannel) {
		gc = guitarchannel;
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
		// draw shadow first  
		vg.fill(0, 0, 0);
	    vg.stroke(0, 0, 0);
	    vg.ellipse(x + 3, y + 3, mySize, mySize);
		// draw marker    
		vg.fill(myColor);
	    vg.stroke(myColor);
	    vg.ellipse(x, y, mySize, mySize);
	  }
	  
	  // draw what notes have been played in the past
	public void drawTracer() {
		// only draw once per use
		if (!inUse) {
			int _x, _y;
			setInUse(true);
			pg.beginDraw();
			pg.fill(myColor);
			pg.stroke(myColor);
			pg.ellipse(x, y, mySize / 2, mySize / 2);
			// draw vector trace
//			pg.strokeWeight(1);
//			_y = GuitarView.strings[gc.getLastStringPlayed()].getY();
//			_x = GuitarView.fretLines[gc.getLastFretPlayed()] - mySize;
//			pg.line(x + vg.random(-5, 5), y + vg.random(-5, 5),
//					_x + vg.random(-5, 5), _y + vg.random(-5, 5));
			pg.endDraw();
		}
	}
}
