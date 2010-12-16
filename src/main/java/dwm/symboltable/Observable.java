package dwm.symboltable;

public interface Observable<T extends Observation> {
	void register(T observer);
}
