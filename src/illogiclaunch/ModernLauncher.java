package illogiclaunch;

import java.io.*;

import com.formdev.flatlaf.FlatLightLaf;

public class ModernLauncher {
	public static void main(String[] args) throws IOException, ReflectiveOperationException {
		FlatLightLaf.setup();
		try (InputStream is = ModernLauncher.class.getResourceAsStream("illogicmainclass.txt")) {
			String clazz = new BufferedReader(
				      new InputStreamReader(is))
				        .lines()
				        .findFirst().get();
			Class.forName(clazz).getDeclaredMethod("main", String[].class).invoke(null, new Object[] {args});
			
		}
	}
}
