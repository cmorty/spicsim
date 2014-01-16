package de.fau.spicsim


import avrora.sim.clock.MainClock

class PwmLowPass (val clock:MainClock) {
	
	val range = (30 * clock.millisToCycles(1)).toInt // 100ms
	val bucktes =  100;
	
	val bucketsize = range / bucktes
	
	var debugthis = false

	class OnOffAcc (){
		var on = 0
		var off = bucketsize //Closures rule!
		
		def emptyFill (amount:Int, fillOn:Boolean){
			if(fillOn){
				on += amount
				off = 0
			} else {
				off += amount
				on = 0
			}
		}
		
		def fill(amount:Int, fillOn:Boolean){
			if(amount < 0) throw new Exception("There is something wrong here")
			if(fillOn){
				on += amount
			} else {
				off += amount
			}
		}
	}	
	
	val buckets = Array.fill(bucktes)(new OnOffAcc())
	
	var lastwrite:Long = 0
	var curlevel:Boolean = false
	var lastlevel:Boolean = false
	var flux = false
	
	private def update(doupdate:Boolean) {
		
		var dthis = debugthis
		
		if(!flux){
			if(curlevel == lastlevel) return // Nothing to do
			lastwrite = clock.getCount
			lastlevel = curlevel
			flux = true
			//Do not update lastlevel
			return
		} 
		
		
		if(!doupdate &&  curlevel == lastlevel){
			return //Nothing changed an no need to update
		}

		val tm = clock.getCount 
		
		var dtime = (tm - lastwrite).toInt //How much time is there to fill buckets

		if(dtime > range){
			dtime = range //Only range time
			flux = false;
		} 
		
		var curbuck = ((tm % range) / bucketsize).toInt //Get current element
		var maxfill = ((tm % range) % bucketsize).toInt
		
		
		while(dtime >= maxfill){
			buckets(curbuck).emptyFill(maxfill, lastlevel)
			dtime -= maxfill
			maxfill = bucketsize // next bucket can be fill more
			curbuck -= 1
			if(curbuck < 0) curbuck = bucktes -1
		}
		
		//Fill rest
		buckets(curbuck).fill(dtime, lastlevel)
		
		lastwrite = tm
		lastlevel = curlevel
		
	}
	
	
	
	def setLevel(level:Boolean) = {
		if(curlevel == level){
			false
		} else { 
			curlevel = level
			update(false)
			true
		}
	}
	
	def getlevel:Float = {
		
		var dthis = debugthis
		
		if(!flux){
			return if(curlevel) 1 else 0 
		}
		
		update(true)
		
		var on = 0
		var off = 0
		buckets.foreach(x => {on += x.on; off += x.off}) //Aggregate
		
		if((off == 0 && curlevel) || (on == 0 && !curlevel)) flux = false
		
		val rv = on.toFloat / (on + off)
		
		rv
	}
	
}