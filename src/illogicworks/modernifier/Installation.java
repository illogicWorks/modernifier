package illogicworks.modernifier;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import illogiclaunch.ModernLauncher;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Installation {
	private static final String MF_PATH = "META-INF/MANIFEST.MF";
	private static final String LAUNCHER_CLASS = ModernLauncher.class.getName();
	private static final String DEV_PATH = "bin/built.jar";
	private static final boolean DEV_ENV = true;

	static void install(Path targetPath) throws IOException {
		URI ourJar, targetJar;
		try {
			ourJar = zipURI(!DEV_ENV ? Installation.class.getProtectionDomain().getCodeSource().getLocation().toURI()
							: Paths.get(DEV_PATH).toUri());
			targetJar = zipURI(targetPath.toRealPath().toUri());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
		FileSystem us = FileSystems.newFileSystem(ourJar, new HashMap<>(), null);
		FileSystem target = FileSystems.newFileSystem(targetJar, new HashMap<>(), null);

		if (Files.exists(target.getPath(ModernLauncher.MAIN_FILE_PATH))) {
			us.close();
			target.close();
			throw new IllegalArgumentException("Target is already modernified!");
		}
		
		Path temp = Files.createTempDirectory("modernifier-temp");
		
		for (Path root : us.getRootDirectories()) {
			for (Path p : iter(Files.walk(root)
					.filter(p -> !p.toString().startsWith("/illogicworks") // don't copy installer
								&& !p.toString().equals("/") // special case, idk what is this
								&& !p.toString().equals('/' + MF_PATH) // we special-case the manifest later
								&& !p.toString().equals("/module-info.class") // don't rename to flatlaf
					))) {
				System.out.println("Temping " + p);
				Path tempFile = temp.resolve(p.toString().substring(1));
				Files.copy(p, tempFile, REPLACE_EXISTING);
			}
		}

		handleManifest(target, temp);
		
		for (Path p : iter(Files.walk(temp))) {
			if (Files.isDirectory(p)) continue;
			System.out.println("Copying " + p);
			Path fileTarget = temp.relativize(p);
			Path finalPath = target.getPath(fileTarget.toString());
			Files.deleteIfExists(finalPath);
			Files.createDirectories(finalPath);
			Files.copy(p, finalPath, REPLACE_EXISTING);
		}
		us.close();
		target.close();
		System.out.println("DID Stuff, temp at " + temp);
	}

	private static void handleManifest(FileSystem target, Path base) throws IOException {
		Manifest manifest;
		try (InputStream is = Files.newInputStream(target.getPath(MF_PATH))) {
			manifest = new Manifest(is);
		}
		
		String originalMainClass = manifest.getMainAttributes().getValue("Main-Class");
		Files.write(base.resolve(ModernLauncher.MAIN_FILE_PATH.substring(1)), Collections.singleton(originalMainClass)); // Java 8 :(
		
		manifest.getMainAttributes().putValue("Main-Class", LAUNCHER_CLASS);
		manifest.getMainAttributes().putValue("Multi-Release", "true"); // flatlaf has some J9+ specifics
		// TODO check if we need to merge more things
		
		try (OutputStream out = Files.newOutputStream(base.resolve(MF_PATH))) {
			manifest.write(out);
		}
	}

	private static URI zipURI(URI path) throws URISyntaxException {
		return URI.create("jar:" + path);
	}

	private static Iterable<Path> iter(Stream<Path> s) {
		return () -> s.iterator();
	}
}
