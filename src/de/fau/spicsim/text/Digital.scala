package de.fau.spicsim.text

import de.fau.spicsim.dev.DevObservable
import de.fau.spicsim.interfaces.DevObserver
import de.fau.spicsim.dev.PwmMon
import de.fau.spicsim.dev.DiscreteLowPass
import de.fau.spicsim.dev.PWMLowPass
import de.fau.spicsim.dev.SevenSegDev
import de.fau.spicsim.dev.PwmMon
import de.fau.spicsim.interfaces.SpicSimDev
import avrora.sim.Simulator

object LedState extends Enumeration {
	type LedState = Value
	val On = Value(1)
	val Off = Value(0)
	val Trans = Value(-1)

}

class DigitalLedMon(input: PwmMon) extends DevObserver with DevObservable {

	import LedState._

	val dlp = new DiscreteLowPass(new PWMLowPass(input, 30), 3, 1, 50)
	dlp.addObserver(this)
	notify(dlp, dlp.level)

	var value = Trans
	notify(dlp, dlp.level)

	def notify(subject: Any, data: Any) {
		subject match {
			case dlp: DiscreteLowPass =>
				value = data match {
					case 0 => Off
					case 1 => Trans
					case 2 => On
					case _ => throw new Exception("Did not get expected data")
				}
				//We are only notifyed if there is a change
				updateAndNotify(value)
			case _ => throw new Exception("Did not get a DLP")
		}
	}
}

//input must be val https://issues.scala-lang.org/browse/SI-4396
//This class allows other LEDs to settle before fireing an event.
class DigitalLedsMon(val input: List[PwmMon]) extends SpicSimDev(input.head) with DevObserver with DevObservable {

	val dlms = for (x <- input) yield {
		val dlm = new DigitalLedMon(x)
		dlm.addObserver(this)
		dlm
	}

	private var queued = false

	val sMon = new Simulator.Event() {
		def fire {
			queued = false
			updateAndNotify(dlms.map(_.value))
		}
	}

	def notify(subject: Any, data: Any) {
		subject match {
			case dlp: DigitalLedMon =>
				if (!queued) {
					queued = true
					clock.insertEvent(sMon, clock.millisToCycles(20))
				}
			case _ => throw new Exception("Did not get a DLP")
		}
	}
}

class Digital7SegElMon(input: List[PwmMon]) extends DevObserver with DevObservable {
	if (input.length != 7) throw new Exception("Wrong number of elements")

	val dlps = input.map(x => new DiscreteLowPass(new PWMLowPass(x), 8, 1, 20))
	dlps.foreach(_.addObserver(this))

	import LedState._
	val state = scala.collection.mutable.Map(dlps.map(_ -> Off): _*)

	def values = dlps.map(state(_))

	def lOn(dat: Int*) = {
		dat.fold(0)((a, b) => a + (1 << b))
	}

	private var curVal = 'K'
	def value = curVal

	val decode = Map(
		/*
			          0
			        5   1
			          6
			        4   2
			          3
			*/
		lOn(0, 1, 2, 3, 4, 5) -> '0',
		lOn(1, 2) -> '1',
		lOn(0, 1, 6, 4, 3) -> '2',
		lOn(0, 1, 6, 2, 3) -> '3',
		lOn(5, 1, 6, 2) -> '4',
		lOn(0, 5, 6, 2, 3) -> '5',
		lOn(0, 5, 4, 3, 2, 6) -> '6',
		lOn(0, 1, 2) -> '7',
		lOn(0, 1, 2, 3, 4, 5, 6) -> '8',
		lOn(0, 5, 1, 6, 2, 3) -> '9',
		lOn(4, 5, 0, 1, 6, 2) -> 'A',
		lOn(5, 4, 3, 2, 6) -> 'b',
		lOn(0, 5, 4, 3) -> 'C',
		lOn(6, 4, 3, 2, 1) -> 'd',
		lOn(0, 5, 6, 4, 3) -> 'E',
		lOn(0, 5, 6, 4) -> 'F',
		lOn(5, 4, 6, 1, 2) -> 'H',
		lOn(0, 5, 1, 6, 2) -> 'g',
		lOn(5, 4) -> 'I',
		lOn(5, 4, 3) -> 'L',
		lOn(2, 3, 4, 6) -> 'o',
		lOn(6, 1, 0, 5, 4) -> 'P',
		lOn(5, 4, 3, 2, 1) -> 'U',
		lOn(5) -> '-',
		lOn(3) -> '_',
		lOn() -> ' '

	)

	private def calValue = {
		if (!state.values.forall(_ != Trans)) {
			'#'
		} else {
			val st = values.foldRight(0)((a, b) => (b << 1) + a.id)
			decode.getOrElse(st, { /* println("Could not find " + st); */ '?' })

		}
	}

	def notify(subject: Any, data: Any) {
		subject match {
			case dlp: DiscreteLowPass =>
				data match {
					case x: Int => state(dlp) = {
						if (x < 1) Off
						else if (x < 3) Trans
						else On
					}
					case _ => throw new Exception("Did not get expected data")
				}
			case _ => throw new Exception("Did not get a DLP")
		}
		val cVal = calValue

		if (curVal != cVal) {
			curVal = cVal
			update()
		} else {
			//val st = values.foldRight(0)((a, b) => (b << 1) + a.id)
			//println("NF: " + st + ": " + values.mkString(","))
		}
		notifyObservers(curVal)
	}

}

//input must be val https://issues.scala-lang.org/browse/SI-4396
//This class allows other LEDs to settle before fireing an event.
class Digital7SegElMons(val input: List[List[PwmMon]]) extends SpicSimDev(input.head.head) with DevObserver with DevObservable {

	val ssegs = for (x <- input) yield {
		val dlm = new Digital7SegElMon(x)
		dlm.addObserver(this)
		dlm
	}

	private var queued = false

	val sMon = new Simulator.Event() {
		def fire {
			queued = false
			updateAndNotify(ssegs.map(_.value))
		}
	}

	def notify(subject: Any, data: Any) {
		subject match {
			case c: Digital7SegElMon =>
				if (!queued) {
					queued = true
					clock.insertEvent(sMon, clock.millisToCycles(25))
				}
			case _ => throw new Exception("Did not get a Digital7SegElMon")
		}
	}
}

