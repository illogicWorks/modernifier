package illogicworks.modernifier;

import static illogicworks.modernifier.Modernifiability.*;

import java.awt.datatransfer.*;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class DndHandler extends TransferHandler  {
	private final JProgressBar progress;
	private final JLabel doneLabel;
	private ModCache cache;

	public DndHandler(JProgressBar progress, JLabel doneLabel) {
		this.progress = progress;
		this.doneLabel = doneLabel;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (!(support.isDrop() && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && (support.getSourceDropActions() & LINK) != 0)) {
			return false;
		}
		support.setDropAction(LINK);
		Path path;
		try {
			path = from(support);
		} catch (InvalidDnDOperationException e) {
			// For some reason (now) sometimes we get this exception in the confirming DND check,
			// while a better solution is found, return whether we cached a valid file in general
			// It may also have been a temporary issue on my computer at the time so it should be
			// re-tested
			System.err.println("DnD: No drop current while handling canImport");
			return cache != null && cache.modernifiability() == MODERNIFIABLE;
		}
		if (path == null) {
			return false;
		}
		if (cache != null && cache.matches(path)) {
			return cache.modernifiability() == MODERNIFIABLE;
		} else {
			cache = null;
			Modernifiability mod = Installation.modernifiabilityOf(path);
			cache = new ModCache(path, mod);
			System.out.println("DnD: Checking modernifiability of " + path);
			return mod == MODERNIFIABLE;
		}
	}

	@Override
	public boolean importData(TransferSupport support) {
		Path path = from(support);
		assert Installation.modernifiabilityOf(path) == MODERNIFIABLE;
		System.out.println("DnD: Handling " + path);
		ModernifyTask task = new ModernifyTask(path, progress, doneLabel);
	    task.execute();
		return true;
	}

	/**
	 * Extracts the {@link Path} from the given {@link TransferSupport}, or returns {@code null} if
	 * it's not a valid single path
	 */
	@SuppressWarnings("unchecked")
	private static Path from(TransferSupport support) {
		List<File> files;
		try {
			files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		} catch (IOException | UnsupportedFlavorException e) {
			throw new IllegalArgumentException(e);
		}
		if (files.size() != 1) {
			return null;
		}
		return files.get(0).toPath();
	}

	private static class ModCache {
		private final Path path;
		private final Modernifiability mod;
		private Instant expiryTime = Instant.now().plusSeconds(5);;

		public ModCache(Path path, Modernifiability mod) {
			this.path = path;
			this.mod = mod;
			expiryTime = Instant.now().plusSeconds(5);
		}

		/**
		 * Returns whether the given {@link Path} matches this cache. Matching includes the cache being expired
		 */
		public boolean matches(Path p) {
			if (expiryTime.compareTo(Instant.now()) < 0) {
				// cache expired, require re-check
				return false;
			} else if (path.equals(p)) {
				expiryTime = Instant.now().plusSeconds(5);
				return true;
			}
			return false;
		}

		public Modernifiability modernifiability() {
			return mod;
		}
	}
}
