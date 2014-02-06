package de.fau.spicsim.gui

import javax.swing.JFrame
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Color
import java.awt.Insets
import javax.swing.JSlider
import javax.swing.SwingConstants
import javax.swing.JButton

class MainWindow {
	val ledcol = List(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)

	val frame = new JFrame();
	frame.setBounds(100, 100, 450, 300);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	val cp = frame.getContentPane()
	cp.setLayout(new GridBagLayout());

	val c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 0.5;
	c.weighty = 0.5;

	/*c.ipady = 20;
	c.ipadx = 20;*/
	c.insets = new Insets(5, 5, 5, 5)

	//Add leds
	val leds = for (i <- 0 to 7) yield {
		c.gridx = 0;
		c.gridy = i;

		val led = new Led(ledcol(i))
		cp.add(led, c)
		led
	}

	c.gridy = 0;
	c.gridheight = 8

	val adc = for (i <- 0 to 1) yield {
		c.gridx += 1
		val adc = new JSlider();
		//adc.setBounds(102, 12, 17, 233);
		adc.setOrientation(1)
		adc.setMinimum(0)
		adc.setMaximum(1024)
		cp.add(adc, c);
		adc

	}

	c.gridheight = 2
	c.gridy = 1
	c.gridx += 1

	val btnLoad = new JButton("Load");
	cp.add(btnLoad, c);

	c.gridy = 3
	c.gridheight = 3
	val sSeg = for (i <- 0 to 1) yield {
		val seg = new SevenSeg
		c.gridx += 1
		cp.add(seg, c)
		seg
	}

	c.gridy += 3
	c.gridx -= 2
	c.gridheight = 2
	val btn = for (i <- 1 to 2) yield {
		val seg = new JButton("T" + i);
		c.gridx += 1
		cp.add(seg, c)
		seg
	}

	frame.setVisible(true);

}

object MainWindow {

	def main(args: Array[String]): Unit = {
		val mw = new MainWindow
		println("Hallo")
	}

}