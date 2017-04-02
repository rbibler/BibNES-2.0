package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.NESPalette;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;
import com.bibler.awesome.bibnes20.utilities.BitUtils;

public class PatterntableView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5571858209948170498L;
	private BufferedImage displayImage;
	private int ptNumber;
	private ROM chrRom;
	private RAM paletteRam;
	private int currentPaletteIndex;
	
	
	public PatterntableView(int ptNumber) {
		super();
		setPreferredSize(new Dimension(512, 512));
		displayImage = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
		this.ptNumber = ptNumber;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(displayImage, 0, 0, 512, 512, null);
	}
	
	public void updateView() {
		for(int i = 0; i < 256; i++) {
			drawPattern(i);
		}
		repaint();
	}
	
	private void drawPattern(int patternIndex) {
		int lowByte;
		int highByte;
		int x;
		int y;
		int startX = (patternIndex % 16) * 8;
		int startY = (patternIndex / 16) * 8;
		int pixel;
		final int startIndex = (patternIndex * 16) + (0x1000 * ptNumber);
		for(int i = 0; i < 8; i++) {												// loop through rows
			lowByte = BitUtils.reverseByte(chrRom.read(startIndex + i));
			highByte = BitUtils.reverseByte(chrRom.read(startIndex + i + 8));
			for(int j = 0; j < 8; j++) {											// loop through cols
				x = startX + j;
				y = startY + i;
				pixel = (lowByte >> j) & 1;
				pixel |= ((highByte >> j) & 1) << 1;
				pixel |= currentPaletteIndex << 2;
				displayImage.setRGB(x, y, NESPalette.getColor(paletteRam.read(pixel)));
			}
		}
	}
	
	public void setPPU(PPU ppu) {
		paletteRam = ppu.getPaletteRam();
	}
	
	public void setCHRRom(ROM chrRom) {
		this.chrRom = chrRom;
	}


}
