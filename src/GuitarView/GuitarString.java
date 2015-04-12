package GuitarView;

import processing.core.PGraphics;

public class GuitarString {
	private int y;
	private PGraphics pg;
	private GuitarView vg;

	GuitarString(GuitarView vg, PGraphics pg, int y) {
		this.y = y;
		this.pg = pg;
		this.vg = vg;
	}

	public int getY() {
		return y;
	}

	public void draw() {
		pg.strokeWeight(3);
		pg.fill(vg.copper);
		pg.stroke(vg.copper);
		pg.line(vg.guitarX, y, vg.width, y);
		// draw shadow
		pg.fill(10, 10, 3);
		pg.stroke(10, 10, 3);
		pg.line(vg.guitarX, y + 3, vg.width, y + 3);

	}
}
