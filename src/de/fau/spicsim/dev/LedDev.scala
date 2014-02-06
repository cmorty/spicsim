package de.fau.spicsim.dev

import scala.collection.mutable.Buffer
import avrora.sim.Simulator
import avrora.sim.State
import avrora.sim.clock.MainClock
import cck.util.Arithmetic
import de.fau.spicsim.interfaces.SpicSimDev
import de.fau.spicsim.interfaces.LedInterface
import de.fau.spicsim.interfaces.SpicSimDevUpdater

case class LedDev(ssdu: SpicSimDevUpdater) extends SpicSimDev(ssdu) {

	class LED(ssdu: SpicSimDevUpdater, pin: String) extends PwmMon(ssdu) with DevObservable {
		var pinOffset: Byte = -1

		var ddrAddr = -1
		var portAddr = -1

		val sMon = new Simulator.Watch.Empty {
			private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)

			override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {
				val ddr = interp.getRegisterByte(ddrAddr)
				val port = interp.getRegisterByte(portAddr)
				val ddrl = bs(ddr, pinOffset)
				val portl = bs(port, pinOffset)

				if (setLevel(ddrl && !portl)) {
					updateAndNotify()
				}
			}
		}

		override def registerSim(sim: Simulator) {
			super.registerSim(sim)
			portAddr = mcu.getProperties.getIORegAddr("PORT" + pin(0))
			ddrAddr = mcu.getProperties.getIORegAddr("DDR" + pin(0))
			sim.insertWatch(sMon, portAddr);
			sim.insertWatch(sMon, ddrAddr);
			pinOffset = pin(1).toString.toByte
		}

	}

	private val ledlist = List("D7", "C0", "C1", "C6", "C7", "A7", "A6", "A5")

	val leds = ledlist.map(x => new LED(this, x))

}
