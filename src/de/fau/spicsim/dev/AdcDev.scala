package de.fau.spicsim.dev

import avrora.sim.Simulator
import avrora.sim.AtmelInterpreter
import avrora.sim.mcu.AtmelMicrocontroller
import avrora.sim.mcu.ADC
import avrora.sim.mcu.ADC.ADCInput
import java.awt.Adjustable
import javax.swing.JSlider
import de.fau.spicsim.interfaces.SpicSimDev
import de.fau.spicsim.interfaces.SpicSimDevUpdater

/*
 * Used trait to support closures
 */
trait Adc extends DevObservable {
	def voltage: Float
	def voltage_=(v: Float): Unit
	def level_=(level: Int): Unit
	def level: Int
}

class AdcDev(ssdu: SpicSimDevUpdater) extends SpicSimDev(ssdu) {

	var refvolt: Float = 5

	class lAdc() extends ADCInput with Adc {
		private var volt: Float = refvolt / 2

		def getVoltage: Float = volt

		def voltage = volt

		def voltage_=(v: Float) {
			if (volt != v) {
				volt = v
				updateAndNotify
			}
		}

		def level_=(l: Int) {
			voltage = refvolt * l / 1024
		}

		def level = (volt * 1024 / refvolt).toInt

	}

	val adcs = List.fill(2)(new lAdc)

	override def registerSim(sim: Simulator) {
		super.registerSim(sim)
		val adcdev = mcu.getDevice("adc").asInstanceOf[ADC]
		refvolt = adcdev.getVoltageRef
		adcdev.connectADCInput(adcs(0), 0)
		adcdev.connectADCInput(adcs(1), 1)
	}

	def adc(sel: Int) = adcs(sel)

}
