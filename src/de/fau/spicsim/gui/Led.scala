package de.fau.spicsim.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

class Led(val color: java.awt.Color) extends JPanel {

	private var circle: Ellipse2D = null
	setOpaque(false)
	setForeground(color)

	override def paintComponent(g: Graphics) {
		super.paintComponent(g);
		val b = g.getClipBounds()
		val sz = (b.getWidth min b.getHeight).toFloat
		val g2 = g.asInstanceOf[Graphics2D]
		g2.setColor(getForeground());
		circle = new Ellipse2D.Float(0, 0, sz, sz);
		g2.fill(circle);
	}

}
