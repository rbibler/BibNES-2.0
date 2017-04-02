package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.NESPalette;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class PaletteGrid extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RAM paletteRam;
	private int gridSize = 32;
	
	
	public PaletteGrid() {
		super();
		initializePanel();
	}
	
	private void initializePanel() {
		setPreferredSize(new Dimension(gridSize * 16, gridSize * 2));
	}
	
	public void updateGrid() {
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPaletteGrid(g);
	}
	
	private void drawPaletteGrid(Graphics g) {
		int x;
		int y;
		for(int i = 0; i < 0x20; i++) {
			x = i % 16;
			y = i / 16; 
			g.setColor(NESPalette.getPixelColor(paletteRam.read(i)));
			g.fillRect(x * gridSize, y * gridSize, gridSize, gridSize);
		}
	}

	public void setPaletteRam(RAM paletteRam) {
		this.paletteRam = paletteRam;
		
	}


}
