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
			for (Path p : iter(Files.walk(temp)
					.filter(p -> !p.toString().startsWith("illogicworks")))) {
				if (Files.isDirectory(p) && !p.toString().equals("illogicworks")) {
					System.out.println("Temping " + p);
					Files.copy(p, temp.resolve(p.toString()), REPLACE_EXISTING);
				}
			}
		}
		
		for (Path p : iter(Files.walk(temp))) {
			if (Files.isDirectory(p)) {
				System.out.println("Copying " + p);
				Files.copy(p, target.getPath(p.toString()), REPLACE_EXISTING);
			}
		}
		
		us.close();
		target.close();
		System.out.println("DID Stuff, temp at " + temp);
		Files.deleteIfExists(temp);
	}
	static URI zipURI(URI path) throws URISyntaxException {
		return URI.create("jar:" + path);
	}
	static Iterable<Path> iter(Stream<Path> s) {
		return () -> s.iterator();
	}
}
