package de.fau.spicsim.dev

import avrora.sim.State
import avrora.sim.Simulator
import avrora.sim.AtmelInterpreter
import avrora.sim.mcu.AtmelMicrocontroller
import avrora.sim.mcu.ADC
import avrora.sim.mcu.ADC.ADCInput
import java.awt.Adjustable
import avrora.sim.mcu.Microcontroller.Pin
import cck.util.Arithmetic
import avrora.sim.mcu.ATMegaFamily
import scala.concurrent.SyncVar
import avrora.sim.mcu.Microcontroller
import java.util.Observer
import java.util.Observable
import javax.swing.JButton
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import de.fau.spicsim.SpicSim
import de.fau.spicsim.interfaces.PinInterface
import de.fau.spicsim.interfaces.SpicSimDev
import de.fau.spicsim.interfaces.PinTristate
import de.fau.spicsim.interfaces.SpicSimDevUpdater

class PinDev(ssdu: SpicSimDevUpdater) extends SpicSimDev(ssdu) {

	class PinControl(_port: Char, pin: Int, iscoff: Int, gicroff: Int, int: Int) extends Simulator.Watch.Empty with Simulator.Event with Microcontroller.Pin.Input with PinInterface {

		private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)
		private def getport(port: String) = interp.getRegisterByte(mcu.getProperties.getIORegAddr(port))
		private def regWatch(port: String) = sim.insertWatch(this, mcu.getProperties.getIORegAddr(port))

		var extState = PinTristate.HighZ
		private var curlevel = false

		def updateSim() {
			regWatch("MCUCR")
			regWatch("GICR")
			regWatch("DDR" + _port)
			regWatch("PORT" + _port)

			//Register self
			mcu.getPin("P" + _port + pin).connectInput(this);
			shedule
		}

		override def fire() {

			val rPort = getport("PORTD")
			val rDdr = getport("DDRD")
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

		override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {
			shedule
		}

		def read: Boolean = curlevel

	}

	private val pinc = List(
		new PinControl('D', 2, 0, 6, 6),
		new PinControl('D', 3, 2, 7, 7)
	)

	pinc.foreach(_.setInput(PinTristate.High))

	override def registerSim(sim: Simulator) {
		super.registerSim(sim)
		pinc.foreach(_.updateSim)
	}

	def pin(id: Int) = pinc(id).asInstanceOf[PinInterface]

}