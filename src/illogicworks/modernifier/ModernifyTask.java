package illogicworks.modernifier;

import java.nio.file.Path;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ModernifyTask extends SwingWorker<Object, Object> implements ProgressHandler {
	private final Path p;
	private final JProgressBar progress;
	private final JLabel doneLabel;
	
	public ModernifyTask(Path p, JProgressBar progress, JLabel doneLabel) {
		this.p = p;
		this.progress = progress;
		this.doneLabel = doneLabel;
	}
	
	@Override
	protected void done() {
		super.done();
		doneLabel.setVisible(true);
	}
	
	@Override
	protected Object doInBackground() throws Exception {
		progress.setIndeterminate(true); // install will give it determination
		Installation.install(p, this);
		return null;
	}
	
	@Override
	public void setMax(int max) {
		progress.setMaximum(max);
		progress.setIndeterminate(false);
	}
	
	@Override
	public void update(int current) {
		progress.setValue(current);
	}
	
	@Override
	public void detail(String detail) {
		// TODO Maybe display
		System.out.println(detail);
	}
	
}