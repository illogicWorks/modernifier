package illogicworks.modernifier;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.stream.Stream;

public class Installation {
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
		
		Path temp = Files.createTempDirectory("modernifier-temp");
		
		for (Path root : us.getRootDirectories()) {
			for (Path p : iter(Files.walk(root)
					.filter(p -> !p.toString().startsWith("illogicworks")))) {
				if (p.toString().equals("/")) continue;
				System.out.println("Temping " + p);
				Path tempFile = temp.resolve(p.toString().substring(1));
				Files.copy(p, tempFile, REPLACE_EXISTING);
			}
		}
		
		for (Path p : iter(Files.walk(temp))) {
			if (Files.isDirectory(p)) continue;
			System.out.println("Copying " + p);
			Path fileTarget = temp.relativize(p);
			Path finalPath = target.getPath(fileTarget.toString());
			Files.createDirectories(finalPath);
			Files.copy(p, finalPath, REPLACE_EXISTING);
		}
		
		us.close();
		target.close();
		System.out.println("DID Stuff, temp at " + temp);
	}
	static URI zipURI(URI path) throws URISyntaxException {
		return URI.create("jar:" + path);
	}
	static Iterable<Path> iter(Stream<Path> s) {
		return () -> s.iterator();
	}
}
