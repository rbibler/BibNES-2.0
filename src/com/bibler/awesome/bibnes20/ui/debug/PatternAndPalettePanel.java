package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class PatternAndPalettePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PaletteGrid paletteGrid;
	private PatterntableView leftPattern;
	private PatterntableView rightPattern;
	
	public PatternAndPalettePanel() {
		super();
		initializePanel();
	}
	
	private void initializePanel() {
		setLayout(new BorderLayout());
		paletteGrid = new PaletteGrid();
		leftPattern = new PatterntableView(0);
		rightPattern = new PatterntableView(1);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setMaximumSize(new Dimension(1024, 512));
		panel.add(leftPattern, BorderLayout.CENTER);
		panel.add(rightPattern, BorderLayout.CENTER);
		add(panel, BorderLayout.CENTER);
		add(paletteGrid, BorderLayout.SOUTH);
	}

	public void setPPU(PPU ppu) {
		paletteGrid.setPaletteRam(ppu.getPaletteRam());
		leftPattern.setPPU(ppu);
		rightPattern.setPPU(ppu);
	}
	
	public void updateViews() {
		paletteGrid.updateGrid();
		leftPattern.updateView();
		rightPattern.updateView();
	}

	public void setChrRom(ROM chrRom) {
		leftPattern.setCHRRom(chrRom);
		rightPattern.setCHRRom(chrRom);
	}

}
