package com.bibler.awesome.bibnes20.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bibler.awesome.bibnes20.communications.Notifier;
import com.bibler.awesome.bibnes20.utilities.InputUtilities;

public class CPUDebugButtonPanel extends Notifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3244432370285726769L;
	
	private JButton runButton;
	private JButton stepButton;
	private JButton resetButton;
	private CPUDebugInputPanel inputPanel;
	
	public JPanel panel;
	
	public CPUDebugButtonPanel(CPUDebugInputPanel inputPanel) {
		this.inputPanel = inputPanel;
		panel = new JPanel() {
			@Override
	        public void addNotify() {
	            super.addNotify();
	            SwingUtilities.getRootPane(stepButton).setDefaultButton(stepButton);
	        }
			
		};
		panel.setLayout(new BorderLayout(0, 0));
		initialize();
	}
	
	private void initialize() {
		ButtonListener listener = new ButtonListener();
		runButton = new JButton("Run");
		runButton.addActionListener(listener);
		runButton.setActionCommand("RUN");
		panel.add(runButton, BorderLayout.WEST);
		
		stepButton = new JButton("Step");
		stepButton.addActionListener(listener);
		stepButton.setActionCommand("STEP");
		panel.add(stepButton, BorderLayout.CENTER);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(listener);
		resetButton.setActionCommand("RESET");
		panel.add(resetButton, BorderLayout.EAST);
	}
	
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object obj = this;
			if(arg0.getActionCommand() == "RUN") {
				final String s = inputPanel.getInput();
				int[] rom = InputUtilities.parseMemoryInputRom(s);
				int[] ram = InputUtilities.parseMemoryInputRam(s);
				obj = new int[][] {rom, ram};
			}
			CPUDebugButtonPanel.this.notify(arg0.getActionCommand(), obj);
			
		}
		
	}

}
