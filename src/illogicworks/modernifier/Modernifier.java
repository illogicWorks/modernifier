package illogicworks.modernifier;

import static javax.swing.SwingConstants.*;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
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
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		frame.setSize(400, 500);// 400 width and 500 height
		frame.setLayout(new GridBagLayout());// using no layout managers
		
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("."));

		JButton b = new JButton("Install");
		
		b.setPreferredSize(new Dimension(100, 60));
		b.setBounds(130, 100, 100, 40);// x axis, y axis, width, height
		b.addActionListener(ev -> {
			try {
				int res = fc.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION) {
					p = fc.getSelectedFile().toPath();
				} else {
					throw null;
				}
				//Installation.install(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		JLabel title = new JLabel("Modernifier!");
		title.setHorizontalTextPosition(CENTER);
		title.setHorizontalAlignment(CENTER);
		title.setFont(new Font("Courier", Font.PLAIN, 30));
		
		JProgressBar progress = new JProgressBar();
		progress.setPreferredSize(new Dimension(100, 10));
		
		frame.add(title, gbc);
		frame.add(b, gbc);
		frame.add(progress, gbc);
		
		frame.setVisible(true);// making the frame visible
	}
}