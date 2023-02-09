package illogicworks.modernifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;

public class Modernifier {
	private static Path p;
	public static void main(String[] args) {
		FlatLightLaf.setup();
		JFrame frame = new JFrame();
		
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("."));

		JButton b = new JButton("Install");
		b.setBounds(130, 100, 100, 40);// x axis, y axis, width, height
		b.addActionListener(ev -> {
			try {
				int res = fc.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION) {
					p = fc.getSelectedFile().toPath();
				} else {
					throw null;
				}
				Installation.install(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		frame.add(b);

		frame.setSize(400, 500);// 400 width and 500 height
		frame.setLayout(null);// using no layout managers
		frame.setVisible(true);// making the frame visible
	}
}