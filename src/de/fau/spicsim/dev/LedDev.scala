package de.fau.spicsim.dev

import avrora.sim.Simulation
import avrora.sim.clock.Synchronizer
import java.io.File
import avrora.core.LoadableProgram
import avrora.sim.mcu.ATMega32
import avrora.sim.platform.DefaultPlatform
import cck.util.Options
import scala.actors.threadpool.TimeUnit
import avrora.sim.State
import avrora.sim.Simulator
import avrora.sim.mcu.Microcontroller
import avrora.sim.AtmelInterpreter
import cck.util.Arithmetic
import avrora.sim.clock.MainClock
import de.fau.spicsim.gui.SevenSeg
import java.awt.Color
import de.fau.spicsim.gui.Led
import de.fau.spicsim.PwmLowPass

class LedDev(sim: Simulator, leds:Array[Led]) extends Simulator.Watch.Empty {
	val mcu = sim.getMicrocontroller

	val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]

	val clock = sim.getClock

	private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)

	class LEDControl(val id: Int, val port: Int, val ddr: Int, val bit: Int) extends PwmLowPass(clock) {
		
	}

	val ledlist = List("D7", "C0", "C1", "C6", "C7", "A7", "A6", "A5")

	val ledmap = {
		var el = 0;
		for (dat <- ledlist) yield {
			val port = mcu.getProperties.getIORegAddr("PORT" + dat(0))
			val ddr = mcu.getProperties.getIORegAddr("DDR" + dat(0))
			val led = new LEDControl(el, port, ddr, dat(1).toString.toByte)
			el += 1
			led
		}
	}

	for (ior <- ledmap.flatMap(x => { List(x.port, x.ddr) }).distinct) yield {
		sim.insertWatch(LedDev.this, ior);
	}

	override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {

		for (led <- ledmap) yield if (led.ddr == data_addr || led.port == data_addr) {

			val ddr = interp.getRegisterByte(led.ddr)
			val port = interp.getRegisterByte(led.port)
			val ddrl = bs(ddr, led.bit)
			val portl = bs(port, led.bit)

			leds(led.id).setLedOn(ddrl && !portl)
		}
	}
}
