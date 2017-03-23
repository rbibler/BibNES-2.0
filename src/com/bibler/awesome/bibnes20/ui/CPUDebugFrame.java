package com.bibler.awesome.bibnes20.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.bibler.awesome.bibnes20.communications.Notifiable;
import com.bibler.awesome.bibnes20.communications.Notifier;
import com.bibler.awesome.bibnes20.systems.console.ThreadRunner;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.utilities.FileUtils;

public class CPUDebugFrame extends JFrame implements Notifiable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4964749373137089245L;
	private JPanel contentPane;
	private CPUDebugPanel debugPanel;
	private CPUDebugInputPanel inputPanel;
	private ThreadRunner runner;

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
		
		initializeMenu();
		
		pack();
		setVisible(true);
	}
	
	public void setRunner(ThreadRunner runner) {
		this.runner = runner;
	}
	
	private void initializeMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		menuBar.add(file);
		setJMenuBar(menuBar);
		JMenuItem loadBin = new JMenuItem("Load Bin");
		file.add(loadBin);
		loadBin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadBin();
			}
			
		});
		
		JMenuItem loadROM = new JMenuItem("Load ROM");
		file.add(loadROM);
		loadROM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadNES();
			}
			
		});
	}
	
	protected void loadBin() {
		JFileChooser chooser = new JFileChooser();
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			int[] rom = FileUtils.loadBinFromFile(f);
			rom[0x7FFD] = 0x80;
			int[] ram = new int[0x2000];
			runner.takeNotice("LOAD", new int[][] {rom, ram});
		}
	}
	
	protected void loadNES() {
		JFileChooser chooser = new JFileChooser();
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if(f.getName().toLowerCase().endsWith(".nes")) {
				GamePak pak = FileUtils.loadRomFromFile(f);
				int[] ram = new int[0x2000];
				runner.takeNotice("LOAD_ROM", new Object[] {pak, ram});
			}
		}
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
