package de.fau.spicsim.text

import java.io.OutputStream
import java.io.PrintStream

import de.fau.spicsim.SpicSim
import de.fau.spicsim.dev.Digital7SegElMons
import de.fau.spicsim.dev.DigitalLedMon
import de.fau.spicsim.dev.DigitalLedsMon
import de.fau.spicsim.dev.LedState.LedState
import de.fau.spicsim.dev.LedState.Off
import de.fau.spicsim.dev.LedState.On
import de.fau.spicsim.dev.LedState.Trans
import de.fau.spicsim.interfaces.DevObserver

class TextOutput(ssim: SpicSim, os: OutputStream) {

	private val ps = new PrintStream(os)

	private def ledS(ls: LedState) = ls match {
		case On => "On "
		case Off => "Off"
		case Trans => "Tra"
	}

	private def ledHex(leds: List[LedState]) = {
		var rv = 0;
		if (leds.forall(_ != Trans)) {
			for (i <- 0 to 7) {
				rv += { if (leds(i) == On) 1 else 0 } << i
			}
			"%02X".format(rv)
		} else {
			"??"
		}

	}

	val ledLP = ssim.ledwatch.leds.map(new DigitalLedMon(_))

	val ledObs = for (l <- 0 to 7) yield {
		val ob = new DevObserver {
			def notify(subject: Any, data: Any) {
				data match {
					case s: LedState => ps.println("LED " + l + ": " + ledS(s))
					case _ => throw new Exception("Did not get expected data")
				}
			}
		}
		ledLP(l).addObserver(ob);
	}

	val ledsMon = new DigitalLedsMon(ssim.ledwatch.leds)

	val ledsO = new DevObserver {
		def notify(subject: Any, data: Any) {
			data match {
				case s: List[LedState] => ps.println("LEDs: " + ledHex(s))
				case _ => throw new Exception("Did not get expected data")
			}
		}
	}
	ledsMon.addObserver(ledsO)

	val sSegMon = new Digital7SegElMons(ssim.segwatch.segc)
	val segO = new DevObserver {
		def notify(subject: Any, data: Any) {
			data match {
				case s: List[Char] =>
					if (s.contains('?')) {
						ps.println("7Seg: " + s.mkString(""))
					} else {
						ps.println("7Seg: " + s.mkString(""))
					}
				//ps.println(SsegMon.ssegs(0).values.mkString(",") + " - " + SsegMon.ssegs(1).values.mkString(","))
				case _ => throw new Exception("Did not get expected data")
			}
		}
	}
	sSegMon.addObserver(segO)

	val btns = for (b <- 0 to 1) yield {
		val btn = ssim.buttondev.btns(b)
		val ob = new DevObserver {
			def notify(subject: Any, data: Any) {
				ps.println("Button" + b + ": " + { if (btn.pressed) "pressed" else "released" })
			}
		}
		btn.addObserver(ob)
		ob
	}

	val adcs = for (a <- 0 to 1) yield {
		val adc = ssim.adcdev.adc(a)
		val ob = new DevObserver {
			def notify(subject: Any, data: Any) {
				ps.println("Adc: " + "%4d".format(adc.level.min(1023)) + " / " + "%01.3f".format(adc.voltage) + "V")
			}
		}
		adc.addObserver(ob)
		ob
	}

}

