package dwm.symboltable;

import java.lang.ref.WeakReference;

public class Observation<T> extends WeakReference<T> {

	public Observation(T referent) {
		super(referent);
	}

}
