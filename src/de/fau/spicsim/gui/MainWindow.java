package de.fau.spicsim.gui;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollBar;


import eu.hansolo.steelseries.tools.LedColor;
import java.awt.Canvas;

public class MainWindow {

	private JFrame frame;
	
	public SevenSeg sSeg[] = new SevenSeg[2];
	public Led leds[] = new Led[8];
	public JButton btnLoad;
	public JButton btnStop;
	
	public Frame getFrame(){
		return frame;
	}
	
	
	/**
	 * Create the application.
	 */
	public MainWindow() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		SevenSeg sSeg0 = new SevenSeg(true);
		sSeg0.setBackground(Color.BLACK);
		sSeg0.setBounds(266, 158, 46, 58);
		

		SevenSeg sSeg1 = new SevenSeg(true);
		sSeg1.setBackground(Color.BLACK);
		sSeg1.setBounds(324, 158, 46, 58);
		
		
		frame.getContentPane().add(sSeg1);
		frame.getContentPane().add(sSeg0);

		sSeg[0] = sSeg0;
		sSeg[1] = sSeg1;

		Led led_0 = new Led();
		led_0.setLedColor(LedColor.BLUE);
		led_0.setBounds(51, 217, 39, 39);
		led_0.setCustomLedColor(Color.GREEN);
		frame.getContentPane().add(led_0);

		Led led_1 = new Led();
		led_1.setBounds(51, 180, 39, 39);
		frame.getContentPane().add(led_1);

		Led led_2 = new Led();
		led_2.setLedColor(LedColor.YELLOW_LED);
		led_2.setBounds(51, 151, 39, 39);
		frame.getContentPane().add(led_2);

		Led led_3 = new Led();
		led_3.setLedColor(LedColor.GREEN_LED);
		led_3.setBounds(51, 121, 39, 39);
		frame.getContentPane().add(led_3);

		Led led_4 = new Led();
		led_4.setLedColor(LedColor.BLUE_LED);
		led_4.setBounds(51, 89, 39, 39);
		frame.getContentPane().add(led_4);

		Led led_5 = new Led();
		led_5.setBounds(51, 61, 39, 39);
		frame.getContentPane().add(led_5);

		Led led_6 = new Led();
		led_6.setLedColor(LedColor.YELLOW_LED);
		led_6.setBounds(51, 33, 39, 39);
		frame.getContentPane().add(led_6);

		Led led_7 = new Led();
		led_7.setLedColor(LedColor.GREEN_LED);
		led_7.setBounds(51, 0, 39, 39);
		frame.getContentPane().add(led_7);
		
		
		leds[0] = led_0;
		leds[1] = led_1;
		leds[2] = led_2;
		leds[3] = led_3;
		leds[4] = led_4;
		leds[5] = led_5;
		leds[6] = led_6;
		leds[7] = led_7;

		JScrollBar adcPoti = new JScrollBar();
		adcPoti.setBounds(102, 12, 17, 233);
		frame.getContentPane().add(adcPoti);

		JScrollBar adcLight = new JScrollBar();
		adcLight.setBounds(142, 12, 17, 233);
		frame.getContentPane().add(adcLight);

		JButton btnLoad = new JButton("Load");
		this.btnLoad = btnLoad;
		btnLoad.setBounds(266, 14, 117, 25);
		frame.getContentPane().add(btnLoad);
		
		JButton btnStop = new JButton("Stop");
		this.btnStop = btnStop;
		btnStop.setBounds(266, 61, 117, 25);
		frame.getContentPane().add(btnStop);
		
		Canvas canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setBounds(243, 137, 164, 102);
		frame.getContentPane().add(canvas);
		
		frame.setVisible(true);
	}
}
