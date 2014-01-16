package de.fau.spicsim.dev

import avrora.sim.Simulation
import avrora.sim.clock.Synchronizer
import java.io.File
import avrora.core.LoadableProgram
import avrora.sim.mcu.ATMega32
import avrora.sim.platform.DefaultPlatform
import cck.util.Options
import scala.actors.threadpool.TimeUnit
import avrora.sim.State
import avrora.sim.Simulator
import avrora.sim.mcu.Microcontroller
import avrora.sim.AtmelInterpreter
import cck.util.Arithmetic
import de.fau.spicsim.gui.Led
import avrora.sim.clock.MainClock
import de.fau.spicsim.gui.SevenSeg
import java.awt.Color
import de.fau.spicsim.PwmLowPass

class SevenSegDev(sim: Simulator, sSegs: Array[de.fau.spicsim.gui.SevenSeg]) extends Simulator.Watch.Empty {
	//Config
	val onCol = Color.red

	val mcu = sim.getMicrocontroller

	val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]

	val clock = sim.getClock

	private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)
	
	
	val segMap = Map(
			0 -> 3, 
			1 -> 4, 
			2 -> 6, 
			3 -> 5,
			4 -> 0,
			5 -> 1,
			6 -> 2
			)

	class SegControl(sSeg: SevenSeg) extends Simulator.Event {
		val res = 5

		class SegState() extends PwmLowPass(clock) {
			var lastlevelCol = Color.darkGray
		}

		val segStates = Array.fill(7)(new SegState)
		
		
		update
		


		/**
		 * Update colors
		 * @return Whether there elements in flux
		 */
		private def update() = {
			var rv = false
			for (seg <- 0 to 6) yield {
				val segS = segStates(seg)
				val newl = segS.getlevel
				
				var fac = newl * 2;
				if(fac > 1) fac = 1
				if(fac < 0.2) fac = 0;
				val nCol = new Color((onCol.getRed * fac).toInt, (onCol.getGreen * fac).toInt, (onCol.getBlue * fac).toInt, onCol.getAlpha)
				
				if (!nCol.equals(segS.lastlevelCol)) {
					segS.lastlevelCol = nCol
					sSeg.setSegment(segMap(seg), nCol)
					if (newl != 1 && newl != 0) rv = true
				}
			}
			rv
		}

		var queued = false

		def set(seg: Int, state: Boolean) {
			
			if (segStates(seg).setLevel(state) && !queued) {
				clock.insertEvent(this, clock.millisToCycles(res))
				queued = true
			}
		}

		def fire {
			if (update) { //Stuff in flux - need to reschedule
				clock.insertEvent(this, clock.millisToCycles(res))
			} else {
				queued = false
			}
		}

	}

	val segc = sSegs.map(new SegControl(_))
	

	def getport(port: String) = interp.getRegisterByte(mcu.getProperties.getIORegAddr(port))
	def regWatch(port: String) = sim.insertWatch(this, mcu.getProperties.getIORegAddr(port))

	regWatch("PORTD")
	regWatch("PORTB")
	regWatch("DDRD")
	regWatch("DDRB")

	override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {
		val portd = getport("PORTD")
		val ddrd = getport("DDRD")

		val portb = getport("PORTB")
		val ddrb = getport("DDRB")

		for (sSegId <- 0 to 1) {
			if (bs(portd, sSegId) && bs(ddrd, sSegId)) { //Output & High
				for (seg <- 0 to 6) {
					setSeg(sSegId, seg, bs(ddrb, seg) && !bs(portb, seg))
				}
			} else {
				setSegOff(sSegId)
			}
		}
	}

	def setSeg(sSegId: Int, seg: Int, on: Boolean) {
		segc(sSegId).set(seg, on)
	}

	def setSegOff(sSeg: Int) {
		for (i <- 0 to 6) {
			setSeg(sSeg, i, false);
		}
	}

}