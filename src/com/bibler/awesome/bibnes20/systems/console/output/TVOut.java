package com.bibler.awesome.bibnes20.systems.console.output;

import javax.swing.JFrame;

import com.bibler.awesome.bibnes20.systems.console.ThreadRunner;

public class TVOut extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3077334006725572621L;
	
	private TVDisplay display;
	private TVOutMenuManager menuManager;
	
	public TVOut() {
		super();
		initializeMenu();
		finalizeFrame();
	}

	public void displayFrame(int[] frame) {
		display.updateFrame(frame);
		
	}
	
	public void setRunner(ThreadRunner runner) {
		menuManager.setRunner(runner);
	}
	
	private void initializeMenu() {
		menuManager = new TVOutMenuManager();
		setJMenuBar(menuManager);
	}
	
	private void finalizeFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		display = new TVDisplay();
		add(display);
		pack();
		setVisible(true);
	}
	

}
