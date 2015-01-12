package de.fau.spicsim.dev

import de.fau.spicsim.interfaces.DevObserver
import avrora.sim.Simulator
import de.fau.spicsim.interfaces.SpicSimDev

/**
 * @param plp    The PWMLowPass to monitor
 * @param levels Number of discretes levels to support
 * @param hyst   Level of change before reporting a change
 * @param res    Highes resolultion in the time domain. This avoids to many events if the value is at the border.
 *
 */

class DiscreteLowPass(plp:PWMLowPass, levels:Int, hyst:Int = 2, res:Int = 20) extends  SpicSimDev(plp) with  DevObservable with DevObserver{

	def maxlevel = levels - 1
	
	if(hyst < 1){
		throw new IllegalArgumentException("Hysteresis must be grater 0")
	}
	
	//Register at clock
	
	
	private var _level = 0;
	def level = _level
	
	private var level_fire = 0
	
	
	var queued = false
	
	plp.addObserver(this)
	
	
	val sMon = new Simulator.Event{
		// Fired by timer
		def fire {
			if(queued) recalc 
			
			if (plp.flux) { //Stuff in flux - need to reschedule
				if(queued == true) {
					clock.insertEvent(this, clock.millisToCycles(res))
				} else {
					clock.insertEvent(this, clock.millisToCycles(res) - (clock.getCount % clock.millisToCycles(res)))
					queued = true
				}
			} else {
				queued = false
			}
		}
	}
	
	//Recalc current value
	private def recalc {
		val curlevel = Math.round(plp.level * maxlevel)
		val diff = Math.abs(curlevel - level_fire)
		if (diff >= hyst || (diff > 0 && (curlevel == 0 || curlevel == maxlevel))) {
			update()
			level_fire = curlevel
		}
		_level = curlevel
		notifyObservers(_level)
	}
	
	def notify(subject:Any ,data:Any){
		if(queued == false) sMon.fire
	}

	override
	def registerSim(sim:Simulator){
		super.registerSim(sim)
		queued = false
		sMon.fire
	}
	
}