package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.bibler.awesome.bibnes20.systems.console.Console;
import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class DebugFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5724393543836879345L;
	private JSplitPane bodyPane;
	private JSplitPane rightPane;
	private CPUDebugStatusPanel statusPanel;
	private JTabbedPane videoViewPane;
	private JTabbedPane memoryViewPane;
	private VideoViewPanel nametablePanel;
	private VideoViewPanel patterntablePanel;
	private NametableView[] nameTables;
	private PatterntableView[] patternTables;
	
	private DebugMemoryView[] memoryViews = new DebugMemoryView[] {
		new DebugMemoryView("PPU Ram"), new DebugMemoryView("PPU Objects"),
		new DebugMemoryView("CPU Ram"), new DebugMemoryView("CHR Rom"),
		new DebugMemoryView("CHR Ram"), new DebugMemoryView("PRG Rom"),
		new DebugMemoryView("PRG Ram")
	};
	
	private Console console;
	
	public DebugFrame() {
		super();
		initialize();
	}
	
	private void initialize() {
		initializeMemoryViews();
		initializeVideoViews();
		initializeStatusView();
		initializeSplitPanes();
		finalizeLayout();
	}
	
	private void initializeMemoryViews() {
		memoryViewPane = new JTabbedPane();
		for(DebugMemoryView view : memoryViews) {
			memoryViewPane.addTab(view.getTitle(), view);
		}
		
	}
	
	private void initializeVideoViews() {
		videoViewPane = new JTabbedPane();
		nameTables = new NametableView[] {
				new NametableView(0), new NametableView(1), 
				new NametableView(2), new NametableView(3)
		};
		patternTables = new PatterntableView[2];
		nametablePanel = new VideoViewPanel(nameTables);
		patterntablePanel = new VideoViewPanel(patternTables);
		videoViewPane.addTab("Nametables", nametablePanel);
		videoViewPane.addTab("PatternTables", patterntablePanel);
	}
	
	private void initializeStatusView() {
		statusPanel = new CPUDebugStatusPanel();
		
	}
	
	private void initializeSplitPanes() {
		bodyPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		bodyPane.setResizeWeight(0.5);
		rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		rightPane.setResizeWeight(0.8);
		bodyPane.setLeftComponent(videoViewPane);
		bodyPane.setRightComponent(rightPane);
		rightPane.setTopComponent(memoryViewPane);
		rightPane.setBottomComponent(statusPanel);
	}
	
	private void finalizeLayout() {
		setLayout(new BorderLayout());
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(bodyPane, BorderLayout.CENTER);
		pack();
	}

	public void setConsole(Console console) {
		this.console = console;
	}
	
	public void updateFrame() {
		for(NametableView ntView : nameTables) {
			ntView.updateFrame(console.getMotherboard().getPPU());
		}
	}
	
	public void setMemory(RAM ppuRam, RAM cpuRam, GamePak gamePak) {
		setMemory(0, ppuRam.getMemArray());
		setMemory(2, cpuRam.getMemArray());
		setMemory(3, gamePak.getCHRRom().getMemArray());
		setMemory(5, gamePak.getPRGRom().getMemArray());
	}
	
	private void setMemory(int memoryViewIndex, int[] viewData) {
		memoryViews[memoryViewIndex].setViewData(viewData);
	}
}
