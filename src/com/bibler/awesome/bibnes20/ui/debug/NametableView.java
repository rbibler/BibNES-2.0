package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.NESPalette;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;

public class NametableView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8675238942279347908L;
	private BufferedImage displayImage;
	private int nametableNumber;
	
	public NametableView(int nametableNumber) {
		this.nametableNumber = nametableNumber;
		displayImage = new BufferedImage(256, 240, BufferedImage.TYPE_4BYTE_ABGR);
		setPreferredSize(new Dimension(256, 240));
	}
	
	public void updateFrame(PPU ppu) {
		int pixel;
		int row;
		int col;
		int address;
		int lowBg = 0;
		int highBg = 0;
		int ntByte;
		int fineY;
		int x;
		int y;
		int attrX;
		int attrY;
		int curAttr;
		final int length = 256*240;
		for(int i = 0; i < length; i++) {
			x = i % 256;
			y = (i / 256);
			row = y / 8;
			col = x / 8;
			address = (0x2000 | ((nametableNumber & 0b11) << 10)) + (row * 32) + col;
			ntByte = ppu.getByte(address);
			address = (0x2000 | ((nametableNumber & 0b11) << 10)) + 0x3C0 + (((y / 32) * 8) + (x / 32));
			curAttr = ppu.getByte(address);
			row = (ntByte / 16);
			col = ntByte % 16;
			fineY = (y % 8);
			address = ((ppu.getPPUCTRL() >> 4 & 1) << 0xC) | (row << 8) | (col << 4) | fineY & 7; 
			if(address >= 0) {
				lowBg = ppu.getByte(address);
			}
			address = ((ppu.getPPUCTRL() >> 4 & 1) << 0xC) | (row << 8) | (col << 4) | (1 << 3) | fineY & 7;
			if(address >= 0) {
				highBg = ppu.getByte(address);
			}
			int attrStart = (((y / 32) * 32) * 256) + (((x / 32) * 32));
			attrX = (x / 32) * 4;
			attrY = (y / 32) * 4;
			int ntX = x / 8;
			int ntY = y / 8;
			attrStart = i - attrStart;
			int attrBitShift = (((ntX - attrX) / 2) * 2) + (((ntY - attrY) / 2) * 4);
			int palVal = ((curAttr >> attrBitShift) & 3) << 2;
			pixel = ((highBg >> (7 - (i % 8)) & 1) << 1 | (lowBg >> (7 -(i % 8)) & 1));
			address = 0x3F00 + (palVal + pixel);
			displayImage.setRGB(x, y, NESPalette.getColor(ppu.getByte(address)));
		}
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(displayImage, 0, 0, null);
		drawGrid(g);
	}
	
	private void drawGrid(Graphics g) {
		int gridSize = (getWidth() / 256) * 8;
		int width = getWidth();
		int height = getHeight();
		g.setColor(Color.RED);
		for(int i = 0; i < 32; i++) {
			g.drawLine(i * gridSize, 0, i * gridSize, height);
		}
		for(int i = 0; i < 30; i++) {
			g.drawLine(0, i * gridSize, width, i * gridSize);
		}
	}

}
