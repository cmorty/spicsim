package de.fau.spicsim.interfaces;

import java.util.List;

public interface LedInterface {
	List<Float> levels();
	Float level(int el);
}
