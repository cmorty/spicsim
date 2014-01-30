package de.fau.spicsim.dev


import avrora.sim.AtmelInterpreter
import avrora.sim.Simulator
import avrora.sim.State
import avrora.sim.Simulation

class StackMon(sim: Simulator, sPan: de.fau.spicsim.gui.StackPanel) extends Simulator.Watch.Empty with Simulator.Event { 


	private val mcu = sim.getMicrocontroller

	private val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]

	private val clock = sim.getClock
	 
	
	private var stacktop = -1
	
	private  var maxStack = -1;
	private  var lastMaxStack = 0
	private var lastupdate = 0
	
	private val updaterate = 10
	
	private var lastsp = -1;

	
	private def getStackSize = {
		val sp = interp.getSP
		stacktop - sp
	}
	
	//It's not possible to monitor the stack, so we just check after each cycle.
	def fire {
		val sp = interp.getSP
		if(lastsp != sp){
			lastsp = sp
			stacktop = Math.max(interp.getSP, stacktop)
			maxStack = Math.max(maxStack, getStackSize)
			
		}
		sim.insertEvent(this, 1)
		
	}
	
	val updater = new Simulator.Event {
		override
		def fire{
			val curtime = clock.cyclesToMillis(clock.getCount)
			val update = (curtime % 1000 == 0) 
			if(lastMaxStack != maxStack || update){
				if(lastupdate != curtime - 1){
					sPan.addVal(curtime.toLong, lastMaxStack)
				}
				sPan.addVal(curtime.toLong, maxStack)
				lastMaxStack = maxStack
			}
			maxStack = -1
			sim.insertEvent(this, clock.millisToCycles(updaterate))
		}
	}
	
	//Register
	updater.fire
	sim.insertEvent(this, 1)
	
	
}