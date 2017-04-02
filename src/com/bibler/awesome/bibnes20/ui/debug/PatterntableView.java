package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.NESPalette;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;

public class PatterntableView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5571858209948170498L;
	private BufferedImage displayImage;
	private int ptNumber;
	
	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(displayImage, 0, 0, null);
	}


}
