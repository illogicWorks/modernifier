package illogiclaunch;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.GraphicsEnvironment;
import java.io.*;

import javax.swing.JOptionPane;

import com.formdev.flatlaf.FlatLightLaf;

public class ModernLauncher {
	public static final String MAIN_FILE_PATH = "/illogicmainclass.txt";

	public static void main(String[] args) {
		FlatLightLaf.setup();
		String mainClass;
		try (InputStream is = ModernLauncher.class.getResourceAsStream(MAIN_FILE_PATH)) {
			if (is == null) {
				fail("Launch info not found");
				throw new IllegalStateException("Launch info not found!");
			}
			mainClass = new BufferedReader(new InputStreamReader(is))
				        .lines()
				        .findFirst().get();
		} catch (IOException e) {
			fail("Exception reading launch info");
			throw new UncheckedIOException(e);
		}
		System.out.println("About to launch modernified app with class " + mainClass);
		try {
			Class.forName(mainClass).getDeclaredMethod("main", String[].class).invoke(null, new Object[] { args });
		} catch (ReflectiveOperationException e) {
			fail("Exception finding or invoking main class");
			throw new IllegalStateException(e);
		}
	}

	private static void fail(String reason) {
		System.err.println("Failed to launch! " + reason);
		if (!GraphicsEnvironment.isHeadless())
			JOptionPane.showMessageDialog(null, "Failed to launch! " + reason, "Failed to launch", ERROR_MESSAGE);
	}
}
