package com.bibler.awesome.bibnes20.ui.cpuemulator;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.bibler.awesome.bibnes20.ui.debug.CPUDebugStatusPanel;

public class CPUDebugPanel extends JPanel {
	
	private CPUDebugStatusPanel statusPanel;
	private CPUDebugButtonPanel buttonPanel;
	
	public CPUDebugPanel(CPUDebugInputPanel inputPanel) {
		super();
		setLayout(new BorderLayout(0, 0));
		statusPanel = new CPUDebugStatusPanel();
		buttonPanel = new CPUDebugButtonPanel(inputPanel);
		add(statusPanel, BorderLayout.NORTH);
		add(buttonPanel.panel, BorderLayout.SOUTH);
	}

	public void updateDebugStatus(int[] statusUpdateArray) {
		statusPanel.upateStatus(statusUpdateArray);
	}
	
	public CPUDebugButtonPanel getButtonPanel() {
		return buttonPanel;
	}

}
