package com.bibler.awesome.bibnes20.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

public class CPUDebugFrame extends JFrame {

	private JPanel contentPane;
	private JTextField PCTextField;
	private JTextField SPTextField;

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
		
		JPanel leftPanel = new JPanel();
		splitPane.add(leftPanel, BorderLayout.CENTER);
		leftPanel.setPreferredSize(new Dimension(400, 600));
		
		JPanel cpuDebugPanel = new JPanel();
		splitPane.add(cpuDebugPanel, BorderLayout.LINE_END);
		cpuDebugPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel cpuDebugButtonPanel = new JPanel();
		//cpuDebugButtonPanel.setPreferredSize(new Dimension(350, 50));
		cpuDebugPanel.add(cpuDebugButtonPanel, BorderLayout.SOUTH);
		cpuDebugButtonPanel.setLayout(new BorderLayout(0, 0));
		
		JButton btnRun = new JButton("Run");
		cpuDebugButtonPanel.add(btnRun, BorderLayout.WEST);
		
		JButton btnStep = new JButton("Step");
		cpuDebugButtonPanel.add(btnStep, BorderLayout.CENTER);
		
		JButton btnReset = new JButton("Reset");
		cpuDebugButtonPanel.add(btnReset, BorderLayout.EAST);
		
		JPanel cpuDebugControlPanel = new JPanel();
		//cpuDebugControlPanel.setPreferredSize(new Dimension(350, 250));
		cpuDebugPanel.add(cpuDebugControlPanel, BorderLayout.NORTH);
		GridBagLayout gbl_cpuDebugControlPanel = new GridBagLayout();
		gbl_cpuDebugControlPanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_cpuDebugControlPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		gbl_cpuDebugControlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_cpuDebugControlPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		cpuDebugControlPanel.setLayout(gbl_cpuDebugControlPanel);
		
		JLabel PCLabel = new JLabel("PC:");
		GridBagConstraints gbc_PCLabel = new GridBagConstraints();
		gbc_PCLabel.anchor = GridBagConstraints.EAST;
		gbc_PCLabel.insets = new Insets(0, 0, 5, 5);
		gbc_PCLabel.gridx = 1;
		gbc_PCLabel.gridy = 1;
		cpuDebugControlPanel.add(PCLabel, gbc_PCLabel);
		
		PCTextField = new JTextField();
		GridBagConstraints gbc_PCTextField = new GridBagConstraints();
		gbc_PCTextField.gridwidth = 5;
		gbc_PCTextField.insets = new Insets(0, 0, 5, 5);
		gbc_PCTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_PCTextField.gridx = 2;
		gbc_PCTextField.gridy = 1;
		cpuDebugControlPanel.add(PCTextField, gbc_PCTextField);
		PCTextField.setColumns(10);
		
		JLabel stackPointerLabel = new JLabel("SP:");
		GridBagConstraints gbc_stackPointerLabel = new GridBagConstraints();
		gbc_stackPointerLabel.insets = new Insets(0, 0, 5, 5);
		gbc_stackPointerLabel.anchor = GridBagConstraints.EAST;
		gbc_stackPointerLabel.gridx = 1;
		gbc_stackPointerLabel.gridy = 2;
		cpuDebugControlPanel.add(stackPointerLabel, gbc_stackPointerLabel);
		
		SPTextField = new JTextField();
		GridBagConstraints gbc_SPTextField = new GridBagConstraints();
		gbc_SPTextField.gridwidth = 5;
		gbc_SPTextField.insets = new Insets(0, 0, 5, 5);
		gbc_SPTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_SPTextField.gridx = 2;
		gbc_SPTextField.gridy = 2;
		cpuDebugControlPanel.add(SPTextField, gbc_SPTextField);
		SPTextField.setColumns(10);
		
		JLabel accumulatorLabel = new JLabel("A:");
		GridBagConstraints gbc_accumulatorLabel = new GridBagConstraints();
		gbc_accumulatorLabel.insets = new Insets(0, 0, 5, 5);
		gbc_accumulatorLabel.gridx = 1;
		gbc_accumulatorLabel.gridy = 3;
		cpuDebugControlPanel.add(accumulatorLabel, gbc_accumulatorLabel);
		
		JLabel accumulatorValue = new JLabel("00");
		GridBagConstraints gbc_accumulatorValue = new GridBagConstraints();
		gbc_accumulatorValue.insets = new Insets(0, 0, 5, 5);
		gbc_accumulatorValue.gridx = 2;
		gbc_accumulatorValue.gridy = 3;
		cpuDebugControlPanel.add(accumulatorValue, gbc_accumulatorValue);
		
		JLabel indexXLabel = new JLabel("X:");
		GridBagConstraints gbc_indexXLabel = new GridBagConstraints();
		gbc_indexXLabel.insets = new Insets(0, 0, 5, 5);
		gbc_indexXLabel.gridx = 3;
		gbc_indexXLabel.gridy = 3;
		cpuDebugControlPanel.add(indexXLabel, gbc_indexXLabel);
		
		JLabel indexXValue = new JLabel("00");
		GridBagConstraints gbc_indexXValue = new GridBagConstraints();
		gbc_indexXValue.insets = new Insets(0, 0, 5, 5);
		gbc_indexXValue.gridx = 4;
		gbc_indexXValue.gridy = 3;
		cpuDebugControlPanel.add(indexXValue, gbc_indexXValue);
		
		JLabel indexYLabel = new JLabel("Y:");
		GridBagConstraints gbc_indexYLabel = new GridBagConstraints();
		gbc_indexYLabel.insets = new Insets(0, 0, 5, 5);
		gbc_indexYLabel.gridx = 5;
		gbc_indexYLabel.gridy = 3;
		cpuDebugControlPanel.add(indexYLabel, gbc_indexYLabel);
		
		JLabel indexYValue = new JLabel("00");
		GridBagConstraints gbc_indexYValue = new GridBagConstraints();
		gbc_indexYValue.insets = new Insets(0, 0, 5, 5);
		gbc_indexYValue.gridx = 6;
		gbc_indexYValue.gridy = 3;
		cpuDebugControlPanel.add(indexYValue, gbc_indexYValue);
		
		JLabel signLabel = new JLabel("S");
		GridBagConstraints gbc_signLabel = new GridBagConstraints();
		gbc_signLabel.insets = new Insets(0, 0, 5, 5);
		gbc_signLabel.gridx = 1;
		gbc_signLabel.gridy = 5;
		cpuDebugControlPanel.add(signLabel, gbc_signLabel);
		
		JLabel overflowLabel = new JLabel("V");
		GridBagConstraints gbc_overflowLabel = new GridBagConstraints();
		gbc_overflowLabel.insets = new Insets(0, 0, 5, 5);
		gbc_overflowLabel.gridx = 2;
		gbc_overflowLabel.gridy = 5;
		cpuDebugControlPanel.add(overflowLabel, gbc_overflowLabel);
		
		JLabel breakLabel = new JLabel("B");
		GridBagConstraints gbc_breakLabel = new GridBagConstraints();
		gbc_breakLabel.insets = new Insets(0, 0, 5, 5);
		gbc_breakLabel.gridx = 3;
		gbc_breakLabel.gridy = 5;
		cpuDebugControlPanel.add(breakLabel, gbc_breakLabel);
		
		JLabel decimalLabel = new JLabel("D");
		GridBagConstraints gbc_decimalLabel = new GridBagConstraints();
		gbc_decimalLabel.insets = new Insets(0, 0, 5, 5);
		gbc_decimalLabel.gridx = 4;
		gbc_decimalLabel.gridy = 5;
		cpuDebugControlPanel.add(decimalLabel, gbc_decimalLabel);
		
		JLabel interruptLabel = new JLabel("I");
		GridBagConstraints gbc_interruptLabel = new GridBagConstraints();
		gbc_interruptLabel.insets = new Insets(0, 0, 5, 5);
		gbc_interruptLabel.gridx = 5;
		gbc_interruptLabel.gridy = 5;
		cpuDebugControlPanel.add(interruptLabel, gbc_interruptLabel);
		
		JLabel zeroLabel = new JLabel("Z");
		GridBagConstraints gbc_zeroLabel = new GridBagConstraints();
		gbc_zeroLabel.insets = new Insets(0, 0, 5, 5);
		gbc_zeroLabel.gridx = 6;
		gbc_zeroLabel.gridy = 5;
		cpuDebugControlPanel.add(zeroLabel, gbc_zeroLabel);
		
		JLabel carryLabel = new JLabel("C");
		GridBagConstraints gbc_carryLabel = new GridBagConstraints();
		gbc_carryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_carryLabel.gridx = 7;
		gbc_carryLabel.gridy = 5;
		cpuDebugControlPanel.add(carryLabel, gbc_carryLabel);
		
		JCheckBox signBox = new JCheckBox("");
		GridBagConstraints gbc_signBox = new GridBagConstraints();
		gbc_signBox.insets = new Insets(0, 0, 0, 5);
		gbc_signBox.gridx = 1;
		gbc_signBox.gridy = 6;
		cpuDebugControlPanel.add(signBox, gbc_signBox);
		
		JCheckBox overflowBox = new JCheckBox("");
		GridBagConstraints gbc_overflowBox = new GridBagConstraints();
		gbc_overflowBox.insets = new Insets(0, 0, 0, 5);
		gbc_overflowBox.gridx = 2;
		gbc_overflowBox.gridy = 6;
		cpuDebugControlPanel.add(overflowBox, gbc_overflowBox);
		
		JCheckBox breakBox = new JCheckBox("");
		GridBagConstraints gbc_breakBox = new GridBagConstraints();
		gbc_breakBox.insets = new Insets(0, 0, 0, 5);
		gbc_breakBox.gridx = 3;
		gbc_breakBox.gridy = 6;
		cpuDebugControlPanel.add(breakBox, gbc_breakBox);
		
		JCheckBox decimalBox = new JCheckBox("");
		GridBagConstraints gbc_decimalBox = new GridBagConstraints();
		gbc_decimalBox.insets = new Insets(0, 0, 0, 5);
		gbc_decimalBox.gridx = 4;
		gbc_decimalBox.gridy = 6;
		cpuDebugControlPanel.add(decimalBox, gbc_decimalBox);
		
		JCheckBox interruptBox = new JCheckBox("");
		GridBagConstraints gbc_interruptBox = new GridBagConstraints();
		gbc_interruptBox.insets = new Insets(0, 0, 0, 5);
		gbc_interruptBox.gridx = 5;
		gbc_interruptBox.gridy = 6;
		cpuDebugControlPanel.add(interruptBox, gbc_interruptBox);
		
		JCheckBox zeroBox = new JCheckBox("");
		GridBagConstraints gbc_zeroBox = new GridBagConstraints();
		gbc_zeroBox.insets = new Insets(0, 0, 0, 5);
		gbc_zeroBox.gridx = 6;
		gbc_zeroBox.gridy = 6;
		cpuDebugControlPanel.add(zeroBox, gbc_zeroBox);
		
		JCheckBox carryBox = new JCheckBox("");
		GridBagConstraints gbc_carryBox = new GridBagConstraints();
		gbc_carryBox.insets = new Insets(0, 0, 0, 5);
		gbc_carryBox.gridx = 7;
		gbc_carryBox.gridy = 6;
		cpuDebugControlPanel.add(carryBox, gbc_carryBox);
		
		pack();
		setVisible(true);
	}

}
