package illogicworks.modernifier;

public interface ProgressHandler {
	void setMax(int max);
	void update(int current);
	void detail(String detail);
}
