
#include <stdint.h>
#include <led.h>
#include <7seg.h>
#include <button.h>
#include <avr/interrupt.h>
#include <util/delay.h>


static void wait(uint16_t wait){
	uint16_t i;
	for(i = wait; i; i--){
		 _delay_ms(1);
	}
}

void main () {
	sei();
	uint8_t cnt = 0;
	while(1){
		sb_7seg_showNumber(cnt);
		wait(1000);
		sb_7seg_disable();
		wait(1000);
		cnt += 1;
		if(cnt > 100 ) cnt = 0;
	}


}



