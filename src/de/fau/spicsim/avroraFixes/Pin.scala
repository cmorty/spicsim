package de.fau.spicsim.avroraFixes

import avrora.sim.Simulator
import avrora.sim.State
import avrora.sim.mcu.Microcontroller
import cck.util.Arithmetic
import de.fau.spicsim.interfaces.PinInterface
import de.fau.spicsim.interfaces.PinTristate
import de.fau.spicsim.interfaces.SpicSimDev
import de.fau.spicsim.interfaces.SpicSimDevUpdater

/* This fixes the incorrect bad I/O implementation of AVRora. Unfortunately the
 * original simulator is so fucked up it's a pain to fix it. 
 */

class Pin(ssdu: SpicSimDevUpdater, _port: Char, pin: Int, iscoff: Int, gicroff: Int, int: Int) extends SpicSimDev(ssdu)
		with Simulator.Event
		with Microcontroller.Pin.Input
		with PinInterface {

	private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)
	private def getport(port: String) = interp.getRegisterByte(mcu.getProperties.getIORegAddr(port))

	private val watch = new Simulator.Watch.Empty {
		override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {
			shedule
		}
	}
	private def regWatch(port: String) = sim.insertWatch(watch, mcu.getProperties.getIORegAddr(port))

	var extState = PinTristate.HighZ
	private var curlevel = false

	override def registerSim(sim: Simulator) {
		super.registerSim(sim)

		regWatch("MCUCR")
		regWatch("GICR")
		regWatch("DDR" + _port)
		regWatch("PORT" + _port)

		//Register self
		mcu.getPin("P" + _port + pin).connectInput(this);
		shedule
	}

	override def fire() {

		val rPort = getport("PORT" + _port)
		val rDdr = getport("DDR" + _port)
		val mcucr = getport("MCUCR")
		val gicr = getport("GICR")

		val dir = bs(rDdr, pin)
		val port = bs(rPort, pin)

		val level: Boolean = {
			if (extState != PinTristate.HighZ) { //Input is not HighZ
				extState == PinTristate.High
			} else { //Input - HighZ
				if (!dir && !port) { // Output is HighZ, too
					curlevel //HighZ -> No change
				} else {
					port //Otherwise output or Pullup
				}
			}
		}

		if (bs(gicr, gicroff)) { //Interupt activated
			//Calculate level at output
			val sInt: Boolean = (bs(mcucr, iscoff + 1), bs(mcucr, iscoff + 0)) match {
				case (false, false) => //Low
					!level
				case (false, true) => //Both
					level != curlevel
				case (true, false) => //Faling
					curlevel && !level
				case (true, true) => //Rising
					!curlevel && level
			}

			if (sInt) {
				val flag = mcu.getEIFR_reg();
				flag.flagBit(int);
			}
		}
		curlevel = level
	}

	private def shedule {
		if (clock != null) clock.insertEvent(this, 0)
	}

	def setInput(in: PinTristate) {
		if (extState != in) {
			extState = in
			shedule
		}
	}

	def read: Boolean = curlevel

}