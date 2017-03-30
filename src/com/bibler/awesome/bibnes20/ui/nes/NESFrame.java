package com.bibler.awesome.bibnes20.ui.nes;

import javax.swing.JFrame;

import com.bibler.awesome.bibnes20.systems.console.ThreadRunner;

public class NESFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5752494635375881055L;
	private NESFrameMenuManager menuManager;
	
	public NESFrame() {
		super();
		initializeMenu();
		finalizeFrame();
	}
	
	public void setRunner(ThreadRunner runner) {
		menuManager.setRunner(runner);
	}
	
	private void initializeMenu() {
		menuManager = new NESFrameMenuManager();
		setJMenuBar(menuManager);
	}
	
	private void finalizeFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	

}
