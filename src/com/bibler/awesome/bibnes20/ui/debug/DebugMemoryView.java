package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import tv.porst.jhexview.JHexView;
import tv.porst.jhexview.SimpleDataProvider;
import tv.porst.jhexview.JHexView.DefinitionStatus;

public class DebugMemoryView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9020057516403411260L;
	private JHexView hexView;
	private String title;
	
	public DebugMemoryView(String title) {
		super();
		hexView = new JHexView();
		this.title = title;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setLayout(new BorderLayout());
		add(hexView, BorderLayout.CENTER);
	}
	
	public void setViewData(int[] viewData) {
		hexView.setData(new SimpleDataProvider(viewData));
		hexView.setDefinitionStatus(DefinitionStatus.DEFINED);
		hexView.setEnabled(true);
		hexView.setBytesPerColumn(1);
		hexView.repaint();
		hexView.setBorder(BorderFactory.createLineBorder(Color.RED));
	}
	
	public String getTitle() {
		return title;
	}
	

}
