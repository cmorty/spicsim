package de.fau.spicsim

import java.awt.EventQueue
import gui.MainWindow
import javax.swing.JFileChooser
import java.awt.event.ActionListener
import java.awt.event.ActionEvent


object Main {
	
	var gui:MainWindow = null;
	
	var lastpath = "."
	lastpath = "/home/inf4/morty/Lehre/V_SPIC/uebungen/spicboard/boardtest"
	lastpath = "testcode"
	
	var sim:SPiCSim = null;
	
	def main(args: Array[String]): Unit = {
		initGui
		
		
		
	}
	
	
	def initGui = {
		EventQueue.invokeLater(new Runnable() {
			def run() {
				gui = new MainWindow();
				gui.btnLoad.addActionListener(new ActionListener{
					def actionPerformed(e:ActionEvent){
						openFile
					} 
					
				})
				
				
			}
		});
		
	}
	
	def openFile(){
		val chooser = new JFileChooser(lastpath);
		if(chooser.showOpenDialog(gui.getFrame) !=  JFileChooser.APPROVE_OPTION) return;
		sim = new SPiCSim(chooser.getSelectedFile,gui.leds, gui.sSeg.reverse, Array(gui.adcLight, gui.adcPoti), Array(gui.btnT1, gui.btnT2))
		sim.start
	}

}