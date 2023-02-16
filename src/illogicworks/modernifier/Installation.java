package illogicworks.modernifier;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.jar.*;
import java.util.stream.Stream;

import illogiclaunch.ModernLauncher;

import static illogicworks.modernifier.Modernifiability.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Installation {
	private static final String MF_PATH = "META-INF/MANIFEST.MF";
	private static final String MF_ENTRY = "Modernified";
	private static final String LAUNCHER_CLASS = ModernLauncher.class.getName();
	private static final String DEV_PATH = "bin/built.jar";
	private static final boolean DEV_ENV = true;
	private static int FILE_COUNT; // lazy at getFileCount
	
	public static void install(Path targetPath, ProgressHandler progress) throws IOException {
		if (modernifiabilityOf(targetPath) != MODERNIFIABLE)
			throw new IllegalArgumentException("Target is not modernifiable! " + modernifiabilityOf(targetPath));

		URI ourJar, targetJar;
		try {
			ourJar = zipURI(!DEV_ENV ? Installation.class.getProtectionDomain().getCodeSource().getLocation().toURI()
							: Paths.get(DEV_PATH).toUri());
			targetJar = zipURI(targetPath.toUri());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
		try (FileSystem us = FileSystems.newFileSystem(ourJar, new HashMap<>(), null);
			 FileSystem target = FileSystems.newFileSystem(targetJar, new HashMap<>(), null)) {
			progress.setMax(getFileCount(us) + 2);
			int copied = 0;
			for (Path root : us.getRootDirectories()) {
				for (Path p : iter(Files.walk(root)
						.filter(Installation::shouldCopy))) {
					progress.detail("Installing class " + p);
					Path pathInTarget = target.getPath(p.toString());
					Files.deleteIfExists(pathInTarget); // REPLACE_EXISTING doesn't seem to actually work on zipfs
					Files.createDirectories(pathInTarget);
					Files.copy(p, pathInTarget, REPLACE_EXISTING);
					copied++;
					progress.update(copied);
				}
			}
			handleManifest(target, progress);
		}
	}

	private static void handleManifest(FileSystem target, ProgressHandler progress) throws IOException {
		Manifest manifest;
		try (InputStream is = Files.newInputStream(target.getPath(MF_PATH))) {
			manifest = new Manifest(is);
		}
		
		Attributes attributes = manifest.getMainAttributes();

		String originalMainClass = attributes.getValue("Main-Class");
		progress.detail("Pointing launcher to " + originalMainClass);
		Files.write(target.getPath(ModernLauncher.MAIN_FILE_PATH), Collections.singleton(originalMainClass)); // Java 8 :(

		attributes.putValue("Main-Class", LAUNCHER_CLASS);
		attributes.putValue("Multi-Release", "true"); // flatlaf has some J9+ specifics
		attributes.putValue(MF_ENTRY, "true"); // add an entry for checking already modernified jars
		// TODO check if we need to merge more things
		
		progress.detail("Updating main class to launcher");
		try (OutputStream out = Files.newOutputStream(target.getPath(MF_PATH))) {
			manifest.write(out);
		}
	}
	
	public static Modernifiability modernifiabilityOf(Path path) throws IOException {
		if (!path.toString().endsWith(".jar")) {
			return NOT_A_JAR;
		}
		try (JarFile jar = new JarFile(path.toFile())) {
			if ("true".equals(jar.getManifest().getMainAttributes().getValue(MF_ENTRY))) {
				return ALREADY_MODERNIFIED;
			}
		} catch (IOException e) {
			// Either actually not a jar, given it couldn't open as one,
			// or not a runnable jar, because of no manifest
			return NOT_A_JAR;
		}
		return MODERNIFIABLE;
	}

	private static int getFileCount(FileSystem ourJar) throws IOException {
		int count = FILE_COUNT;
		if (count == 0) {
			// zips and jars have a single root
			Path root = ourJar.getRootDirectories().iterator().next();
			return FILE_COUNT = (int)Files.walk(root)
									.filter(Installation::shouldCopy)
									.count();
		}
		return count;
	}
	
	// filter for walks, here for reusability
	private static boolean shouldCopy(Path p) {
		return !Files.isDirectory(p)
				&& !p.toString().startsWith("/illogicworks") // don't copy installer
				&& !p.toString().equals('/' + MF_PATH) // we special-case the manifest later
				&& !p.toString().equals("/module-info.class");
	}

	private static URI zipURI(URI path) throws URISyntaxException {
		return URI.create("jar:" + path);
	}

	private static Iterable<Path> iter(Stream<Path> s) {
		return () -> s.iterator();
	}
}
