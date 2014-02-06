package de.fau.spicsim.interfaces

import avrora.sim.AtmelInterpreter
import avrora.sim.Simulator
import avrora.sim.clock.MainClock
import avrora.sim.mcu.ATMegaFamily
import avrora.sim.clock.Clock

abstract class SpicSimDevUpdater {
	def registerSimUpdate(observer: SpicSimDev)
	def insertEvent(e: Simulator.Event, cycles: Long)
	def removeEvent(e: Simulator.Event)
}

class SpicSimDev(ssdu: SpicSimDevUpdater) extends SpicSimDevUpdater with DelayedInit {
	var sim: Simulator = null
	var mcu: ATMegaFamily = null
	var interp: AtmelInterpreter = null
	private val ssd = this

	class MainClockWrapper(self: MainClock) {

		def insertEvent(e: Simulator.Event, cycles: Long) = ssdu.insertEvent(e, cycles)

		def removeEvent(e: Simulator.Event) = ssdu.removeEvent(e)

		def getCount = self.getCount

		def millisToCycles(ms: Double) = self.millisToCycles(ms)

		def cyclesToMillis(cycles: Long) = self.cyclesToMillis(cycles)
	}

	def removeEvent(e: Simulator.Event) {
		ssdu.removeEvent(e)
	}

	def insertEvent(e: Simulator.Event, cycles: Long) {
		ssdu.insertEvent(e, cycles)
	}

	var clock: MainClockWrapper = null

	SpicSimDev.alld = this :: SpicSimDev.alld

	private var simupdateobservers = List[SpicSimDev]()

	def delayedInit(body: => Unit) = {
		body
		ssdu.registerSimUpdate(this)
	}

	def registerSimUpdate(observer: SpicSimDev) {
		if (simupdateobservers.contains(observer)) {
			return
		}

		simupdateobservers = observer :: simupdateobservers
		if (sim != null) {
			observer.registerSim(sim)
		}
	}

	def registerSim(sim: Simulator) {
		if (this.sim == sim) {
			throw new Exception("Double Registration")
		}
		this.sim = sim
		mcu = sim.getMicrocontroller.asInstanceOf[ATMegaFamily]
		interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]
		clock = new MainClockWrapper(sim.getClock)
		simupdateobservers.foreach(_.registerSim(sim))
	}
}

object SpicSimDev {
	var alld = List[SpicSimDev]()

	def test {
		for (d <- alld) {
			if (d.sim == null) throw new Exception("Nosim")
		}

	}
}