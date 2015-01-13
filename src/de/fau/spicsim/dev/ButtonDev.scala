package de.fau.spicsim.dev

import avrora.sim.Simulator
import de.fau.spicsim.avroraFixes.Pin
import de.fau.spicsim.interfaces.PinInterface
import de.fau.spicsim.interfaces.PinTristate
import de.fau.spicsim.interfaces.SpicSimDev
import de.fau.spicsim.interfaces.SpicSimDevUpdater

class Button(pin: Pin) extends DevObservable {

	var _state = PinTristate.HighZ

	def state: PinTristate = _state

	def pressed = _state == PinTristate.Low

	def pressed_=(p: Boolean) {
		state = if (p) PinTristate.Low else PinTristate.HighZ
	}

	def state_=(s: PinTristate) {
		if (s != _state) {
			_state = s
			pin.setInput(state)
			updateAndNotify(state)
		}
	}

}

class ButtonDev(ssdu: SpicSimDevUpdater) {

	val btns = List(
		new Button(new Pin(ssdu, 'D', 2, 0, 6, 6)),
		new Button(new Pin(ssdu, 'D', 3, 2, 7, 7))
	)

}