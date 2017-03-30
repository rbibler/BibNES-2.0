package com.bibler.awesome.bibnes20.ui.nes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.bibler.awesome.bibnes20.systems.console.ThreadRunner;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.utilities.FileUtils;

public class NESFrameMenuManager extends JMenuBar {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8846824869706790507L;
	private ThreadRunner runner;
	
	public NESFrameMenuManager() {
		super();
		initializeMenu();
	}
	
	public void setRunner(ThreadRunner runner) {
		this.runner = runner;
	}

	private void initializeMenu() {
		JMenu file = new JMenu("File");
		add(file);
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

}
