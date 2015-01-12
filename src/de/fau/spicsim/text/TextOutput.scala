package de.fau.spicsim.text

import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import de.fau.spicsim.SpicSim
import de.fau.spicsim.dev.DiscreteLowPass
import de.fau.spicsim.dev.PWMLowPass
import de.fau.spicsim.dev.PwmMon
import de.fau.spicsim.interfaces.AdcInterface
import de.fau.spicsim.interfaces.DevObserver
import de.fau.spicsim.interfaces.PinInterface
import de.fau.spicsim.interfaces.PinTristate
import java.io.OutputStream
import de.fau.spicsim.gui.SevenSeg
import de.fau.spicsim.interfaces.DevObserver
import de.fau.spicsim.text.LedState._
import java.io.PrintStream

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

}

