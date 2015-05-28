package GuitarView;

import processing.core.PGraphics;

public class GuitarString {
	private int y;
	private GuitarView vg;

	GuitarString(GuitarView vg, int y) {
		this.y = y;
		this.vg = vg;
	}

	public int getY() {
		return y;
	}

	public void draw(PGraphics pg, int color) {
		pg.strokeWeight(3);
		pg.fill(color);
		pg.stroke(color);
		pg.line(GuitarView.guitarX, y, vg.width, y);
		// draw shadow
		pg.fill(10, 10, 3);
		pg.stroke(10, 10, 3);
		pg.line(GuitarView.guitarX, y + 3, vg.width, y + 3);
	}
}
