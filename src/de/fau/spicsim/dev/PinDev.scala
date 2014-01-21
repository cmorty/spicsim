package de.fau.spicsim.dev

import avrora.sim.State
import avrora.sim.Simulator
import avrora.sim.AtmelInterpreter
import avrora.sim.mcu.AtmelMicrocontroller
import avrora.sim.mcu.ADC
import avrora.sim.mcu.ADC.ADCInput
import java.awt.Adjustable
import avrora.sim.mcu.Microcontroller.Pin
import cck.util.Arithmetic
import avrora.sim.mcu.ATMegaFamily
import scala.concurrent.SyncVar
import avrora.sim.mcu.Microcontroller
import java.util.Observer
import java.util.Observable
import javax.swing.JButton
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import de.fau.spicsim.SPiCSim


class PinDev(ssim: SPiCSim, sim: Simulator, inputs:Array[JButton])  {
	
	object Tristate extends Enumeration {
		type WeekDay = Value
		val High, Low, HighZ = Value
	}
	
	
	
	
	val mcu = sim.getMicrocontroller.asInstanceOf[ATMegaFamily]
	val interp = sim.getInterpreter.asInstanceOf[AtmelInterpreter]
	val clock = sim.getClock
	
	
	class PinControl (_port:Char,  pin:Int, iscoff:Int, gicroff:Int, int:Int) extends Simulator.Watch.Empty with Simulator.Event with Microcontroller.Pin.Input {
		
		private def bs(reg: Byte, bit: Int) = Arithmetic.getBit(reg, bit)
		private def getport(port: String) = interp.getRegisterByte(mcu.getProperties.getIORegAddr(port))
		private def regWatch(port: String) = sim.insertWatch(this, mcu.getProperties.getIORegAddr(port))
		
		
		var extState = Tristate.HighZ
		private var curlevel = false
	
		regWatch("MCUCR")
		regWatch("GICR")
		regWatch("DDR" + _port)
		regWatch("PORT" + _port)
		
		//Register self
		mcu.getPin("P" + _port + pin).connectInput(this);
			
			
		override def fire(){
			
			
			val rPort = getport("PORTD")
			val rDdr = getport("DDRD")
			val mcucr =  getport("MCUCR")
			val gicr =  getport("GICR")
			

			val dir = bs(rDdr, pin)
			val port = bs(rPort, pin)
			
			val level:Boolean = {
				if(extState != Tristate.HighZ){ //Input is not HighZ
					extState == Tristate.High
				} else { //Input - HighZ
					if(!dir && !port){ // Output is HighZ, too
						curlevel //HighZ -> No change
					} else {
						port //Otherwise output or Pullup
					}
				}
			}
			
			
			if(bs(gicr, gicroff)) {//Interupt activated
				//Calculate level at output
				val sInt:Boolean = (bs(mcucr, iscoff + 1),bs(mcucr, iscoff + 0)) match { 
					case (false, false) => //Low
						!level
					case (false, true) => //Both
						level != curlevel
					case (true, false) => //Faling
						curlevel && !level
					case (true, true) => //Rising
						!curlevel && level
				}	
				
				
				if(sInt){
					val flag = mcu.getEIFR_reg();
					flag.flagBit(int);
				}
			}
			curlevel = level
		}
		
		
		private def shedule {
			if(!ssim.evq.contains(this)){ 
				ssim.evq += (this -> 0)
			}

		}
		
		def setInput(in:Tristate.Value){
			if(extState != in){
				extState = in
				shedule
			}
		}
		
		override def fireAfterWrite(state: State, data_addr: Int, value: Byte) {
			shedule
		}
		
		def read:Boolean = curlevel
		
	}
	
	

	
	val pinc = Array(
		new PinControl('D',2, 0, 6,6),
		new PinControl('D',3, 2, 7, 7)
	)
	
	pinc.foreach(_.setInput(Tristate.High))

	
	
	class Inputmon(jb: JButton, pc: PinControl) {
	
		val bModel = jb.getModel
	
		val cl = new ChangeListener {
			def stateChanged(cEvt: ChangeEvent) {
				if (bModel.isPressed()) {
					pc.setInput(Tristate.Low)
				} else if (!bModel.isPressed()) {
					pc.setInput(Tristate.HighZ)
				}

			}
		}
		
		jb.addChangeListener(cl)
	}
	
	
	val imon = Array(
		new Inputmon(inputs(0), pinc(0)),
		new Inputmon(inputs(1), pinc(1))
	)
	
	
}