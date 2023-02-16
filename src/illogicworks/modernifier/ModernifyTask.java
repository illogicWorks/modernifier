package illogicworks.modernifier;

import java.nio.file.Path;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ModernifyTask extends SwingWorker<Object, Object> {
	Path p;
	JProgressBar progress;
	JLabel doneLabel;
	
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
		progress.setIndeterminate(true); // This must be changed
		Installation.install(p);
		progress.setIndeterminate(false);
		return null;
	}
	
}