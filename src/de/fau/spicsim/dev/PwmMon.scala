package de.fau.spicsim.dev

import avrora.sim.clock.MainClock
import scala.collection.mutable.MutableList
import scala.collection.mutable.Queue
import de.fau.spicsim.interfaces.SpicSimDev
import avrora.sim.Simulator
import de.fau.spicsim.interfaces.SpicSimDevUpdater
import de.fau.spicsim.interfaces.DevObserver

class PwmMon(ssdu: SpicSimDevUpdater) extends SpicSimDev(ssdu) {

	abstract class PWMState(val time: Long)
	case class On(_time: Long) extends PWMState(_time)
	case class Off(_time: Long) extends PWMState(_time)

	val state = Queue[PWMState]()
	var curlevel: Boolean = false

	private val done = true;

	private var observers = List[Tuple2[DevObserver, Long]]()
	private var montime: Long = 0
	private var monticks: Long = 0

	/**
	 * @param observer Observer object
	 * @param time Time in ms
	 */
	def addObserver(observer: DevObserver, time: Long) {
		observers = observer -> time :: observers
		montime = montime.max(time)
	}

	private var cleanctr = 0;
	def setLevel(level: Boolean): Boolean = {
		this.synchronized {
			if (curlevel == level) {
				false
			} else {
				val tm = clock.getCount
				//Clean up
				if (cleanctr > 9) {
					cleanctr = 0
					var startt = tm - monticks;
					while (!state.isEmpty && state.head.time < startt) state.dequeue()
				} else {
					cleanctr += 1
				}
				//Add
				curlevel = level;
				if (monticks > 0) state += { if (curlevel) On(tm) else Off(tm) }
				observers.foreach(_._1.notify(this, null))
				true

			}

		}
	}

	def flux(): Boolean = !state.isEmpty

	def flux(time: Long): Boolean = {
		val tm = clock.getCount
		state.exists(_.time > (tm - clock.millisToCycles(time)))
	}

	def state(time: Long): Queue[PWMState] = state.dropWhile(_.time < clock.getCount - time)

	override def registerSim(sim: Simulator) {
		if (done) {
			super.registerSim(sim)
			state.clear()
			monticks = clock.millisToCycles(montime)

		}
	}
}

