package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

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
	private DebugMemoryView ppuRam;
	private DebugMemoryView cpuRam;
	private DebugMemoryView ppuObject;
	private DebugMemoryView chrRom;
	private DebugMemoryView prgRom;
	private DebugMemoryView prgRam;
	private DebugMemoryView chrRam;
	private NametableView[] nameTables;
	private PatterntableView[] patternTables;
	
	
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
		memoryViewPane.addTab("PPU Ram", ppuRam);
		memoryViewPane.addTab("PPU Objects", ppuObject);
		memoryViewPane.addTab("CPU Ram", cpuRam);
		memoryViewPane.addTab("CHR Rom", chrRom);
		memoryViewPane.addTab("CHR Ram", chrRam);
		memoryViewPane.addTab("PRG Rom", prgRom);
		memoryViewPane.addTab("PRG Ram", prgRam);
		
	}
	
	private void initializeVideoViews() {
		videoViewPane = new JTabbedPane();
		nameTables = new NametableView[4];
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
}
