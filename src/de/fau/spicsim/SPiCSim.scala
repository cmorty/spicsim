package de.fau.spicsim

import avrora.sim.Simulation
import avrora.sim.clock.Synchronizer
import java.io.File
import avrora.core.LoadableProgram
import avrora.sim.mcu.ATMega32
import avrora.sim.platform.DefaultPlatform
import cck.util.Options
import avrora.sim.State
import avrora.sim.Simulator
import avrora.sim.mcu.Microcontroller
import avrora.sim.AtmelInterpreter
import cck.util.Arithmetic
import gui.Led
import avrora.sim.clock.MainClock
import de.fau.spicsim.gui.SevenSeg
import java.awt.Color
import de.fau.spicsim.dev.LedDev
import java.awt.Adjustable
import scala.collection.mutable.SynchronizedQueue
import javax.swing.JButton
import avrora.sim.mcu.ADC
import avrora.sim.mcu.AtmelMicrocontroller
import javax.swing.JSlider



class SPiCSim(file:File, leds:Array[Led], sSeg:Array[de.fau.spicsim.gui.SevenSeg], adc:Array[JSlider], pins:Array[JButton]) {

	var freq  = 1 * 1000 * 1000 // 1 MHz
	var isrunning = false

	
	
	val evq = new SynchronizedQueue[Tuple2[Simulator.Event, Int]]()
	
	val p = new LoadableProgram(file);
	p.load();
	
	var sim = avrora.Defaults.newSimulator(0, "atmega32", freq, freq, p.getProgram())
	
	val mcu = sim.getMicrocontroller.asInstanceOf[AtmelMicrocontroller]
	
	val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]
	
	val clock = sim.getClock

	val thread = new SPiCSimThread
	
	val mcuadc = mcu.getDevice("adc").asInstanceOf[ADC]
	mcuadc.setVoltageRef(5)
	
	
	///Connect
	
	
	val ledwatch = new LedDev(sim, leds)
	
	val segwatch = new dev.SevenSegDev(sim, sSeg)
	
	
	val adcdev = new dev.AdcDev(sim,adc)
	
	
	
	
	
	val pindev = new dev.PinDev(this, sim, pins)
	
	
	
	def stop {isrunning = false}
	
	def start {
		if(isrunning == false){
			isrunning = true
			thread.start
		}
	}

	
	class SPiCSimThread extends Thread{
		
		
		
		override
		def run(){
			
			
			var starttime = System.currentTimeMillis
			while(isrunning){
				
				val curtime = System.currentTimeMillis
				
				val runtime = curtime - starttime
				
				val runticks = clock.millisToCycles(runtime)
								//Get ticks
				
				var torun = runticks - clock.getCount()
				
				if(torun < clock.millisToCycles(5)){ //There is some time left 
					Thread.sleep(5);
					torun += clock.millisToCycles(5)
				} else if(torun > clock.millisToCycles(10)) {
					println("behind")
				}
				
				while(torun > 0){
					torun -= sim.step
					if(!evq.isEmpty){
						for(e <- evq.dequeueAll(_ => true)){
							sim.insertEvent(e._1,e._2)
						}
					}
				} 

			}
		}
	}

}



