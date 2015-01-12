package de.fau.spicsim.gui

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
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JSlider
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class ButtonMonitor(jb: JButton, pc: PinInterface) {

	val bModel = jb.getModel

	val cl = new ChangeListener {
		def stateChanged(cEvt: ChangeEvent) {
			if (bModel.isPressed()) {
				pc.setInput(PinTristate.Low)
			} else if (!bModel.isPressed()) {
				pc.setInput(PinTristate.HighZ)
			}
		}
	}

	jb.addChangeListener(cl)
}

class SlideMonitor(js: JSlider, pc: AdcInterface) {

	val cl = new ChangeListener {
		def stateChanged(cEvt: ChangeEvent) {
			pc.setLevel(js.getValue)
		}
	}
	js.addChangeListener(cl)

	val devo = new DevObserver {
		def notify(subject: Any, data: Any) {
			js.setValue(pc.getLevel.min(js.getMaximum()))
		}
	}

	devo.notify(null, null)

	pc.addObserver(devo)

}

class SSegMon(ss: SevenSeg, el: Int, input: PwmMon) extends DevObserver {
	val col = Color.red

	val dlp = new DiscreteLowPass(new PWMLowPass(input), 256)
	dlp.addObserver(this)

	notify(dlp, null)

	private def getC(level: Int) = {
		val mult = (level.toFloat * 2 / 255).min(1)
		val r = col.getRed()
		new Color((col.getRed * mult).toInt, (col.getGreen * mult).toInt, (col.getBlue() * mult).toInt, col.getAlpha())
	}

	def notify(subject: Any, data: Any) {
		subject match {
			case dlp: DiscreteLowPass =>
				ss.setSegment(el, getC(dlp.level))
			case _ => throw new Exception("Did not get a DLP")
		}

	}
}

class LedMon(led: Led, input: PwmMon) extends DevObserver {

	val dlp = new DiscreteLowPass(new PWMLowPass(input), 256)
	dlp.addObserver(this)
	notify(dlp, null)

	def col = led.color

	private def getC(level: Int) = {
		val mult = (level.toFloat * 2 / 255).min(1)
		new Color((col.getRed * mult).toInt, (col.getGreen * mult).toInt, (col.getBlue() * mult).toInt, col.getAlpha())
	}

	def notify(subject: Any, data: Any) {
		subject match {
			case dlp: DiscreteLowPass => led.setForeground(getC(dlp.level))
			case _ => throw new Exception("Did not get a DLP")
		}

	}

}

class Gui(ssim: SpicSim) {
	//Create Main Window
	private val mw = new MainWindow

	var lastpath = "."
	lastpath = "/home/inf4/morty/Lehre/V_SPIC/uebungen/spicboard/boardtest"
	lastpath = "testcode"
	lastpath = "."

	val bm = List(
		{ new ButtonMonitor(mw.btn(0), ssim.pindev.pin(0)) },
		{ new ButtonMonitor(mw.btn(1), ssim.pindev.pin(1)) }
	)

	//Connect Adc
	val sm = List(
		{ new SlideMonitor(mw.adc(0), ssim.adcdev.adc(0)) },
		{ new SlideMonitor(mw.adc(1), ssim.adcdev.adc(1)) }
	)

	val ssegm = for (sseg <- 0 to 1) yield {
		for (seg <- 0 to 6) yield {
			val smon = new SSegMon(mw.sSeg(sseg), seg, ssim.segwatch.segc(sseg)(seg))
			smon
		}
	}

	val ledm = for (i <- 0 to 7) yield {
		val led = new LedMon(mw.leds(i), ssim.ledwatch.leds(i))
		led
	}

	mw.btnLoad.addActionListener(new ActionListener {
		def actionPerformed(e: ActionEvent) {
			openFile
		}

	})

	def openFile() {
		val chooser = new JFileChooser(lastpath);
		if (chooser.showOpenDialog(mw.frame) != JFileChooser.APPROVE_OPTION) return ;
		lastpath = chooser.getSelectedFile.getParentFile.toString

		ssim.load(chooser.getSelectedFile)
		ssim.start
		/*if(sp != null) sp.dispose
		if(stackF != null) stackF.dispose*/

		/*sp = new StackPanel
		sim.addStackmon(sp)*/

	}

}