package de.fau.spicsim.dev

import de.fau.spicsim.interfaces.DevObserver

trait DevObservable {
	private var observers = List[DevObserver]()
	private var isupdate = false
	def addObserver(observer: DevObserver) { observers = observer :: observers }

	def update() {
		isupdate = true
	}

	def notifyObservers() { notifyObservers(null) }
	def notifyObservers(data: Any) {
		if (isupdate) observers.foreach(_.notify(this, data))
		isupdate = false
	}

	def updateAndNotify() { updateAndNotify(null) }
	def updateAndNotify(data: Any) {
		update
		notifyObservers(data)
	}
}

