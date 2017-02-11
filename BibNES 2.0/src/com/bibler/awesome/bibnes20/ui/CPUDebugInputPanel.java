package com.bibler.awesome.bibnes20.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class CPUDebugInputPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2144732188378601806L;
	
	private JTextArea inputArea;
	
	public CPUDebugInputPanel() {
		inputArea = new JTextArea();
		setLayout(new BorderLayout(0,0));
		add(inputArea, BorderLayout.CENTER);
	}
	
	public String getInput() {
		return inputArea.getText();
	}

}
