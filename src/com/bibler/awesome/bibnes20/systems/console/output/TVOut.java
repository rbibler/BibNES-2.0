package com.bibler.awesome.bibnes20.systems.console.output;

import javax.swing.JFrame;

public class TVOut extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3077334006725572621L;
	
	private TVDisplay display;
	
	public TVOut() {
		super();
		display = new TVDisplay();
		add(display);
		pack();
		setVisible(true);
	}

	public void displayFrame(int[] frame) {
		display.updateFrame(frame);
		
	}

}
