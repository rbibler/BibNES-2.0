package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class VideoViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 166303758513230533L;
	private JPanel[] views;
	
	public VideoViewPanel(JPanel[] views) {
		super();
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new GridLayout(0, 2, 1, 1));
		holderPanel.setPreferredSize(new Dimension((256 * 2) + 4, (240 * 2) + 4));
		this.views = views;
		for(JPanel panel : views) {
			if(panel != null) {
				holderPanel.add(panel);
			}
		}
		//setLayout(new BorderLayout());
		add(holderPanel);
	}

}
