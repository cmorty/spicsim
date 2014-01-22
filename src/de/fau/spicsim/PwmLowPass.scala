package de.fau.spicsim


import avrora.sim.clock.MainClock

class PwmLowPass (val clock:MainClock) {
	
	val range = (20 * clock.millisToCycles(1)).toInt // 100ms
	val bucktes =  100;
	
	val bucketsize = range / bucktes
	
	

	class OnOffAcc (){
		var on = 0
		var off = 0 //Closures rule!
		
		def emptyFill (amount:Int, fillOn:Boolean){
			if(fillOn){
				on = amount
				off = 0
			} else {
				off = amount
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
	
	private var lastwrite:Long = 0
	private var curlevel:Boolean = false
	private var lastlevel:Boolean = false
	var flux = false
	
	private def update(doupdate:Boolean){this.synchronized {
		
		
		val tm = clock.getCount 
		val startbuck = ((tm % range) / bucketsize).toInt //Get current element
		var maxfill = ((tm % range) % bucketsize).toInt
		
		
		if(!flux){
			if(curlevel == lastlevel) return // Nothing to do
			
			buckets(startbuck).emptyFill(maxfill, lastlevel)
			lastwrite = clock.getCount
			lastlevel = curlevel
			flux = true
			//Do not update lastlevel
			return
		} 
		
		
		if(!doupdate &&  curlevel == lastlevel){
			return //Nothing changed an no need to update
		}

		
		
		var dtime = (tm - lastwrite).toInt //How much time is there to fill buckets
		val ddtime = (tm - lastwrite).toInt //How much time is there to fill buckets

		
		
		
		var curbuck = startbuck

		var stop = false
		while(dtime >= 0 && !stop){
			if(dtime > maxfill) {
				buckets(curbuck).emptyFill(maxfill, lastlevel)
			} else {
				buckets(curbuck).fill(dtime, lastlevel)
			}
			dtime -= maxfill
			maxfill = bucketsize // next bucket can be fully filled
			curbuck -= 1
			if(curbuck < 0) curbuck = bucktes -1
			if(curbuck == startbuck) stop == true
			
		}
		
		//Fill rest
		
		if(buckets.exists(b => {b.on + b.off > bucketsize + 1})) throw new Exception("Overfull bucket")
		
		
		
		
		
		lastwrite = tm
		lastlevel = curlevel
	}}
	
	
	
	def setLevel(level:Boolean) = {
		this.synchronized {
			if(curlevel == level){
				false
			} else { 
				curlevel = level
				update(false)
				true
			}
		}
	}
	
	def getlevel:Float = {
		
		
		if(!flux){
			return if(curlevel) 1 else 0 
		}
		
		update(true)
		
		var on = 0
		var off = 0
		this.synchronized {
			buckets.foreach(x => {on += x.on; off += x.off}) //Aggregate
			
			if((off == 0 && curlevel) || (on == 0 && !curlevel)) flux = false
		
		}
		val rv = on.toFloat / (on + off)
		
		rv
	}
	
}


