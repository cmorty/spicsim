package de.fau.spicsim

import java.awt.EventQueue
import gui.MainWindow
import javax.swing.JFileChooser
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.JFrame
import de.fau.spicsim.gui.StackPanel
import de.fau.spicsim.gui.StackPanel
import de.fau.spicsim.gui.StackPanel



object Main {
	
	var gui:MainWindow = null;
	
	var lastpath = "."
	lastpath = "/home/inf4/morty/Lehre/V_SPIC/uebungen/spicboard/boardtest"
	lastpath = "testcode"
		lastpath = "."
	
	var sim:SPiCSim = null;
	
	var stackF:JFrame = null;
	
	var sp:StackPanel = null
	
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
		lastpath = chooser.getSelectedFile.getParentFile.toString
		
		if(sim != null) sim.stop
		if(sp != null) sp.dispose
		if(stackF != null) stackF.dispose
		
		
		
		sim = new SPiCSim(chooser.getSelectedFile,gui.leds.reverse, gui.sSeg, Array(gui.adcLight, gui.adcPoti), Array(gui.btnT1, gui.btnT2))

		sp = new StackPanel
		sim.addStackmon(sp)
		
		
		sim.start
	}

}