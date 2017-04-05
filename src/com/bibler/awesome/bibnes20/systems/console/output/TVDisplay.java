package com.bibler.awesome.bibnes20.systems.console.output;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JPanel;

public class TVDisplay extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7320807209819565759L;
	private int[] frame;
	private BufferedImage displayImage;
	
	public TVDisplay() {
		super();
		displayImage = new BufferedImage(256, 240, BufferedImage.TYPE_4BYTE_ABGR);
		setPreferredSize(new Dimension(1024, 960));
	}
	
	public void updateFrame(int[] frame) {
		this.frame = frame;
		for(int i = 0; i < frame.length; i++) {
			displayImage.setRGB(i % 256, i / 256, frame[i]);
		}
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintDisplay(g);
	}
	
	private void paintDisplay(Graphics g) {
		g.drawImage(displayImage, 0, 0, getWidth(), getHeight(), null);
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
