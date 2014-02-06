package de.fau.spicsim.dev

import avrora.sim.Simulator
import avrora.sim.AtmelInterpreter
import avrora.sim.mcu.AtmelMicrocontroller
import avrora.sim.mcu.ADC
import avrora.sim.mcu.ADC.ADCInput
import java.awt.Adjustable
import javax.swing.JSlider
import de.fau.spicsim.interfaces.AdcInterface
import de.fau.spicsim.interfaces.SpicSimDev
import de.fau.spicsim.interfaces.SpicSimDevUpdater

class AdcDev(ssdu: SpicSimDevUpdater) extends SpicSimDev(ssdu) {

	var refvolt: Float = 5

	class Adc() extends ADCInput with AdcInterface with DevObservable {
		private var volt: Float = refvolt / 2

		def getVoltage: Float = volt

		def setVoltage(v: Float) {
			if (volt != v) {
				volt = v
				updateAndNotify
			}
		}

		def setLevel(level: Int) {
			setVoltage(refvolt * level / 1024)
		}

		def getLevel = (volt * 1024 / refvolt).toInt

	}

	val adcs = List.fill(2)(new Adc)

	override def registerSim(sim: Simulator) {
		super.registerSim(sim)
		val adcdev = mcu.getDevice("adc").asInstanceOf[ADC]
		refvolt = adcdev.getVoltageRef
		adcdev.connectADCInput(adcs(0), 0)
		adcdev.connectADCInput(adcs(1), 1)
	}

	def adc(sel: Int) = adcs(sel)

}