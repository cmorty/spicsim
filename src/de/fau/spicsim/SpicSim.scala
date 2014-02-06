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
import de.fau.spicsim.gui.StackPanel
import de.fau.spicsim.dev.StackMon
import de.fau.spicsim.dev.SevenSegDev
import de.fau.spicsim.dev.AdcDev
import de.fau.spicsim.dev.PinDev
import de.fau.spicsim.interfaces.SpicSimDevUpdater
import de.fau.spicsim.interfaces.SpicSimDev

class SpicSimInstance(val file: File, freq: Int) {

	val p = new LoadableProgram(file);
	p.load();

	val sim = avrora.Defaults.newSimulator(0, "atmega32", freq, freq, p.getProgram())

	val mcu = sim.getMicrocontroller.asInstanceOf[AtmelMicrocontroller]

	val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]

	val clock = sim.getClock

	val mcuadc = mcu.getDevice("adc").asInstanceOf[ADC]
	mcuadc.setVoltageRef(5)

	///Connect

}

class SpicSim() extends SpicSimDevUpdater {

	val ssim = this

	private var observers = List[SpicSimDev]()

	val evq = new SynchronizedQueue[Unit => Unit]()

	val ledwatch = new LedDev(this)
	val segwatch = new SevenSegDev(this)
	val adcdev = new AdcDev(this)
	val pindev = new PinDev(this)

	val devs = List(ledwatch, segwatch, adcdev, pindev)

	val freq = 1 * 1000 * 1000 // 1 MHz

	var sim: SpicSimInstance = null

	var isrunning = false

	var simThread: SPiCSimThread = null

	def load(file: File) {
		stop
		sim = new SpicSimInstance(file, freq)
		evq.clear
		observers.foreach(_.registerSim(sim.sim))
		SpicSimDev.test
		//Remove old stuff if possible 
		System.gc()
	}

	def stop {
		isrunning = false
		if (simThread != null) {
			if (simThread.isAlive()) simThread.join
		}
	}

	def start {
		if (isrunning == false) {
			simThread = new SPiCSimThread
			isrunning = true
			simThread.start
		}
	}

	def addStackmon(pan: StackPanel) {
		val smon = new StackMon(sim.sim, pan)
	}

	def insertEvent(e: Simulator.Event, cycles: Long) {
		if (Thread.currentThread == simThread) {
			sim.sim.insertEvent(e, cycles)
		} else {
			evq += { x => sim.sim.insertEvent(e, cycles) }
		}
	}

	def removeEvent(e: Simulator.Event) {
		if (Thread.currentThread == simThread) {
			sim.sim.removeEvent(e)
		} else {
			evq += { x => sim.sim.removeEvent(e) }
		}
	}

	def registerSimUpdate(observer: SpicSimDev) {
		if (observers.contains(observer)) {
			//This object is already registered.
			return
		}

		observers = observer :: observers
		if (sim != null) observer.registerSim(sim.sim)
	}

	class SPiCSimThread extends Thread {

		override def run() {

			var starttime = System.currentTimeMillis
			while (isrunning) {

				val curtime = System.currentTimeMillis

				val runtime = curtime - starttime

				val runticks = sim.clock.millisToCycles(runtime)
				//Get ticks

				var torun = runticks - sim.clock.getCount()

				if (torun < sim.clock.millisToCycles(5)) { //There is some time left 
					Thread.sleep(5);
					torun += sim.clock.millisToCycles(5)
				} else if (torun > sim.clock.millisToCycles(10)) {
					println("behind")
				}

				while (torun > 0) {
					torun -= sim.sim.step
					if (!evq.isEmpty) {
						for (e <- evq.dequeueAll(_ => true)) {
							e()
						}
					}
				}
			}
		}
	}

}

