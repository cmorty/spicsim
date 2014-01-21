package de.fau.spicsim.dev

import de.fau.spicsim.gui.Led
import avrora.sim.Simulator
import avrora.sim.AtmelInterpreter
import avrora.sim.mcu.AtmelMicrocontroller
import avrora.sim.mcu.ADC
import avrora.sim.mcu.ADC.ADCInput
import java.awt.Adjustable
import javax.swing.JSlider

class AdcDev(sim: Simulator, inputs:Array[JSlider]) {
	val mcu = sim.getMicrocontroller.asInstanceOf[AtmelMicrocontroller]
	val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]
	val clock = sim.getClock
	
	val adcdev = mcu.getDevice("adc").asInstanceOf[ADC]
	
	class Adc(i:JSlider) extends ADCInput {
		def getVoltage:Float = (i.getValue / i.getMaximum.toFloat) * 5
	}
	
	adcdev.connectADCInput(new Adc(inputs(0)),0)
	adcdev.connectADCInput(new Adc(inputs(1)),1)

}