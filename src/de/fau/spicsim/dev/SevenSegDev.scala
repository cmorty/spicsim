package de.fau.spicsim.dev

import avrora.sim.Simulator
import avrora.sim.State
import cck.util.Arithmetic
import de.fau.spicsim.interfaces.DevObserver
import de.fau.spicsim.interfaces.SevenSegInterface
import de.fau.spicsim.interfaces.SpicSimDev
import avrora.sim.clock.MainClock
import de.fau.spicsim.interfaces.SpicSimDevUpdater

class SevenSegDev(ssdu: SpicSimDevUpdater) extends SpicSimDev(ssdu) with DevObservable with DevObserver {
	//Config

	private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)

	val segMap = List(
		3, // 0
		4, // 1
		6, // 2
		5, // 3
		0, // 4
		1, // 5
		2 // 6
	)

	val segc = List.fill(2)(List.fill(7)(new PwmMon(this)))

	def notify(subject: Any, data: Any) {
		updateAndNotify(data)
	}
	import scala.language.reflectiveCalls
	val sMon = new Simulator.Watch.Empty {

		def getport(port: String) = interp.getRegisterByte(mcu.getProperties.getIORegAddr(port))
		def regWatch(port: String) = sim.insertWatch(this, mcu.getProperties.getIORegAddr(port))

		override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {
			val portd = getport("PORTD")
			val ddrd = getport("DDRD")

			val portb = getport("PORTB")
			val ddrb = getport("DDRB")

			for (sSegId <- 0 to 1) {
				if (bs(ddrd, sSegId) && !bs(portd, sSegId)) { //Output & High
					for (seg <- 0 to 6) {
						setSeg(sSegId, segMap(seg), bs(ddrb, seg) && !bs(portb, seg)) //Output & Low
					}
				} else {
					setSegOff(sSegId)
				}
			}
		}
	}

	override def registerSim(sim: Simulator) {
		super.registerSim(sim)
		sMon.regWatch("PORTD")
		sMon.regWatch("PORTB")
		sMon.regWatch("DDRD")
		sMon.regWatch("DDRB")
	}

	def setSeg(sSegId: Int, seg: Int, on: Boolean) {
		segc(sSegId)(seg).setLevel(on)
	}

	def setSegOff(sSegId: Int) {
		segc(sSegId).foreach(_.setLevel(false))
	}

}