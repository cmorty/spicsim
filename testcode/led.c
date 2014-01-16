
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



    while (1) {
	for(int i = 0; i < 256; i++){
		sb_led_set_all_leds(i);
		wait(1000);

	}
    }
}



