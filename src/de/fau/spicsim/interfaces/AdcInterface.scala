package de.fau.spicsim.interfaces;

trait AdcInterface {
	def setVoltage(v: Float): Unit
	def setLevel(l: Int): Unit

	def getLevel(): Int

	def addObserver(observer: DevObserver): Unit
}
