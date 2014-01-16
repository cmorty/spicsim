
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
	DDRD |= (1 << 0) | (1 << 1);
	DDRB = 0xff;
	

	while (1) {
		for(int anz = 0; anz < 2; anz++){
			if(anz){
				PORTD |=  (1 << 0);
				PORTD &= ~(1 << 1);
			} else {
				PORTD |=  (1 << 1);
				PORTD &= ~(1 << 0);
			}
				

			for(int seg = 0; seg < 7 ; seg++){
				PORTB = ~(1 << seg);
				wait(1000);
			}
		}
	}
}



