package com.bibler.awesome.bibnes20.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.bibler.awesome.bibnes20.communications.Notifiable;

public class CPUDebugFrame extends JFrame implements Notifiable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4964749373137089245L;
	private JPanel contentPane;
	private CPUDebugPanel debugPanel;
	private CPUDebugInputPanel inputPanel;

	/**
	 * Create the frame.
	 */
	public CPUDebugFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel splitPane = new JPanel();
		splitPane.setLayout(new BorderLayout());
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		inputPanel = new CPUDebugInputPanel();
		splitPane.add(inputPanel, BorderLayout.CENTER);
		inputPanel.setPreferredSize(new Dimension(400, 600));
		inputPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		debugPanel = new CPUDebugPanel(inputPanel);
		splitPane.add(debugPanel, BorderLayout.LINE_END);
		
		
		
		pack();
		setVisible(true);
	}
	
	public void updateDebugStatus(int[] statusUpdateArray) {
		debugPanel.updateDebugStatus(statusUpdateArray);
	}
	
	public CPUDebugPanel getDebugPanel() {
		return debugPanel;
	}

	@Override
	public void takeNotice(String message, Object messagePacket) {
		if(message.equalsIgnoreCase("CPU_UPDATE")) {
			updateDebugStatus((int[]) messagePacket);
		}
	}

}
