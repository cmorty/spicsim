package de.fau.spicsim.dev

import avrora.sim.clock.MainClock
import scala.collection.mutable.MutableList
import scala.collection.mutable.Queue
import de.fau.spicsim.interfaces.SpicSimDev
import avrora.sim.Simulator
import de.fau.spicsim.interfaces.SpicSimDevUpdater
import de.fau.spicsim.interfaces.DevObserver
import avrora.sim.clock.Clock


class PWMLowPass(pwmMon: PwmMon, time:Long = (30))  extends SpicSimDev(pwmMon) with DevObserver with DevObservable{

		
	pwmMon.addObserver(this, time)
	
	def flux = pwmMon.flux(time)
	
	def notify(subject:Any, data:Any){
		updateAndNotify()
	}
	
	def level:Float = {
		this.synchronized {
			
			if(!pwmMon.flux(time)) return {if(pwmMon.curlevel) 1 else 0} 
			
			//The ontime
			
			val tm = clock.getCount
			var startt = tm - clock.millisToCycles(time)
			var ontime:Long = 0;
			
			for(s <- pwmMon.state)if(s.time >= startt){
				s match {
					case o:pwmMon.On  => startt = o.time //Switching On 
					case o:pwmMon.Off => ontime = ontime + o.time - startt //Switching Off
				}
			}
			if(pwmMon.curlevel){ //We currently on - need to use curleve, becuase queue mitght be empty
				ontime = ontime + (tm - pwmMon.state.last.time)
			}
			val on = ontime.toFloat /  clock.millisToCycles(time)
			on
		}
		
	}
}