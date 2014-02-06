package de.fau.spicsim.interfaces;

trait  DevObserver {
	def notify(subject:Any ,data:Any):Unit
}
