
#include <stdint.h>
#include <led.h>
#include <7seg.h>
#include <button.h>
#include <avr/interrupt.h>
#include <util/delay.h>

/*
static void wait(uint16_t wait){
	uint16_t i;
	for(i = wait; i; i--){
		 _delay_ms(1);
	}
}*/

void main () {
	sei();
	sb_7seg_showNumber(8);
	while(1);


}



