package com.bibler.awesome.bibnes20.ui.debug;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bibler.awesome.bibnes20.utilities.StringUtilities;

public class CPUDebugStatusPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5847820034789146703L;
	
	private JTextField PCTextField;
	private JLabel stackPointerLabel;
	private JTextField stackPointerField;
	
	private JLabel accumulatorLabel;
	private JLabel accumulatorValue;
	private JLabel indexXLabel;
	private JLabel indexXValue;
	private JLabel indexYLabel;
	private JLabel indexYValue;
	
	private JLabel signLabel;
	private JLabel overflowLabel;
	private JLabel breakLabel;
	private JLabel decimalLabel;
	private JLabel interruptLabel;
	private JLabel zeroLabel;
	private JLabel carryLabel;
	
	private JCheckBox signBox;
	private JCheckBox overflowBox;
	private JCheckBox breakBox;
	private JCheckBox decimalBox;
	private JCheckBox interruptBox;
	private JCheckBox zeroBox;
	private JCheckBox carryBox;
	
	public CPUDebugStatusPanel() {
		super();
		initialize();
	}
	
	public void upateStatus(int[] statusUpdateArray) {
		String pc = StringUtilities.convertIntToFourPaddedHex(statusUpdateArray[0]);
		PCTextField.setText(pc);
		String sp = StringUtilities.convertIntToFourPaddedHex(statusUpdateArray[1]);
		int status = statusUpdateArray[2];
		signBox.setSelected((status & 0x80) > 1);
		overflowBox.setSelected((status & 0x40) > 1);
		breakBox.setSelected((status & 0x10) > 1);
		decimalBox.setSelected((status & 0x08) > 1);
		interruptBox.setSelected((status & 0x04) > 1);
		zeroBox.setSelected((status & 0x02) > 1);
		carryBox.setSelected((status & 0x01) >= 1);
		
		accumulatorValue.setText(StringUtilities.convertIntToTwoPaddedHex(statusUpdateArray[3]));
		indexXValue.setText(StringUtilities.convertIntToTwoPaddedHex(statusUpdateArray[4]));
		indexYValue.setText(StringUtilities.convertIntToTwoPaddedHex(statusUpdateArray[5]));
	}
	
	
	private void initialize() {
		GridBagLayout gbl_cpuDebugControlPanel = new GridBagLayout();
		gbl_cpuDebugControlPanel.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_cpuDebugControlPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		gbl_cpuDebugControlPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_cpuDebugControlPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_cpuDebugControlPanel);
		
		JLabel PCLabel = new JLabel("PC:");
		GridBagConstraints gbc_PCLabel = new GridBagConstraints();
		gbc_PCLabel.anchor = GridBagConstraints.EAST;
		gbc_PCLabel.insets = new Insets(0, 0, 5, 5);
		gbc_PCLabel.gridx = 1;
		gbc_PCLabel.gridy = 1;
		add(PCLabel, gbc_PCLabel);
		
		PCTextField = new JTextField();
		GridBagConstraints gbc_PCTextField = new GridBagConstraints();
		gbc_PCTextField.gridwidth = 5;
		gbc_PCTextField.insets = new Insets(0, 0, 5, 5);
		gbc_PCTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_PCTextField.gridx = 2;
		gbc_PCTextField.gridy = 1;
		add(PCTextField, gbc_PCTextField);
		PCTextField.setColumns(10);
		
		stackPointerLabel = new JLabel("SP:");
		GridBagConstraints gbc_stackPointerLabel = new GridBagConstraints();
		gbc_stackPointerLabel.insets = new Insets(0, 0, 5, 5);
		gbc_stackPointerLabel.anchor = GridBagConstraints.EAST;
		gbc_stackPointerLabel.gridx = 1;
		gbc_stackPointerLabel.gridy = 2;
		add(stackPointerLabel, gbc_stackPointerLabel);
		
		stackPointerField = new JTextField();
		GridBagConstraints gbc_SPTextField = new GridBagConstraints();
		gbc_SPTextField.gridwidth = 5;
		gbc_SPTextField.insets = new Insets(0, 0, 5, 5);
		gbc_SPTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_SPTextField.gridx = 2;
		gbc_SPTextField.gridy = 2;
		add(stackPointerField, gbc_SPTextField);
		stackPointerField.setColumns(10);
		
		accumulatorLabel = new JLabel("A:");
		GridBagConstraints gbc_accumulatorLabel = new GridBagConstraints();
		gbc_accumulatorLabel.insets = new Insets(0, 0, 5, 5);
		gbc_accumulatorLabel.gridx = 1;
		gbc_accumulatorLabel.gridy = 3;
		add(accumulatorLabel, gbc_accumulatorLabel);
		
		accumulatorValue = new JLabel("00");
		GridBagConstraints gbc_accumulatorValue = new GridBagConstraints();
		gbc_accumulatorValue.insets = new Insets(0, 0, 5, 5);
		gbc_accumulatorValue.gridx = 2;
		gbc_accumulatorValue.gridy = 3;
		add(accumulatorValue, gbc_accumulatorValue);
		
		indexXLabel = new JLabel("X:");
		GridBagConstraints gbc_indexXLabel = new GridBagConstraints();
		gbc_indexXLabel.insets = new Insets(0, 0, 5, 5);
		gbc_indexXLabel.gridx = 3;
		gbc_indexXLabel.gridy = 3;
		add(indexXLabel, gbc_indexXLabel);
		
		indexXValue = new JLabel("00");
		GridBagConstraints gbc_indexXValue = new GridBagConstraints();
		gbc_indexXValue.insets = new Insets(0, 0, 5, 5);
		gbc_indexXValue.gridx = 4;
		gbc_indexXValue.gridy = 3;
		add(indexXValue, gbc_indexXValue);
		
		indexYLabel = new JLabel("Y:");
		GridBagConstraints gbc_indexYLabel = new GridBagConstraints();
		gbc_indexYLabel.insets = new Insets(0, 0, 5, 5);
		gbc_indexYLabel.gridx = 5;
		gbc_indexYLabel.gridy = 3;
		add(indexYLabel, gbc_indexYLabel);
		
		indexYValue = new JLabel("00");
		GridBagConstraints gbc_indexYValue = new GridBagConstraints();
		gbc_indexYValue.insets = new Insets(0, 0, 5, 5);
		gbc_indexYValue.gridx = 6;
		gbc_indexYValue.gridy = 3;
		add(indexYValue, gbc_indexYValue);
		
		signLabel = new JLabel("S");
		GridBagConstraints gbc_signLabel = new GridBagConstraints();
		gbc_signLabel.insets = new Insets(0, 0, 5, 5);
		gbc_signLabel.gridx = 1;
		gbc_signLabel.gridy = 5;
		add(signLabel, gbc_signLabel);
		
		overflowLabel = new JLabel("V");
		GridBagConstraints gbc_overflowLabel = new GridBagConstraints();
		gbc_overflowLabel.insets = new Insets(0, 0, 5, 5);
		gbc_overflowLabel.gridx = 2;
		gbc_overflowLabel.gridy = 5;
		add(overflowLabel, gbc_overflowLabel);
		
		breakLabel = new JLabel("B");
		GridBagConstraints gbc_breakLabel = new GridBagConstraints();
		gbc_breakLabel.insets = new Insets(0, 0, 5, 5);
		gbc_breakLabel.gridx = 3;
		gbc_breakLabel.gridy = 5;
		add(breakLabel, gbc_breakLabel);
		
		decimalLabel = new JLabel("D");
		GridBagConstraints gbc_decimalLabel = new GridBagConstraints();
		gbc_decimalLabel.insets = new Insets(0, 0, 5, 5);
		gbc_decimalLabel.gridx = 4;
		gbc_decimalLabel.gridy = 5;
		add(decimalLabel, gbc_decimalLabel);
		
		interruptLabel = new JLabel("I");
		GridBagConstraints gbc_interruptLabel = new GridBagConstraints();
		gbc_interruptLabel.insets = new Insets(0, 0, 5, 5);
		gbc_interruptLabel.gridx = 5;
		gbc_interruptLabel.gridy = 5;
		add(interruptLabel, gbc_interruptLabel);
		
		zeroLabel = new JLabel("Z");
		GridBagConstraints gbc_zeroLabel = new GridBagConstraints();
		gbc_zeroLabel.insets = new Insets(0, 0, 5, 5);
		gbc_zeroLabel.gridx = 6;
		gbc_zeroLabel.gridy = 5;
		add(zeroLabel, gbc_zeroLabel);
		
		carryLabel = new JLabel("C");
		GridBagConstraints gbc_carryLabel = new GridBagConstraints();
		gbc_carryLabel.insets = new Insets(0, 0, 5, 5);
		gbc_carryLabel.gridx = 7;
		gbc_carryLabel.gridy = 5;
		add(carryLabel, gbc_carryLabel);
		
		signBox = new JCheckBox("");
		GridBagConstraints gbc_signBox = new GridBagConstraints();
		gbc_signBox.insets = new Insets(0, 0, 0, 5);
		gbc_signBox.gridx = 1;
		gbc_signBox.gridy = 6;
		add(signBox, gbc_signBox);
		
		overflowBox = new JCheckBox("");
		GridBagConstraints gbc_overflowBox = new GridBagConstraints();
		gbc_overflowBox.insets = new Insets(0, 0, 0, 5);
		gbc_overflowBox.gridx = 2;
		gbc_overflowBox.gridy = 6;
		add(overflowBox, gbc_overflowBox);
		
		breakBox = new JCheckBox("");
		GridBagConstraints gbc_breakBox = new GridBagConstraints();
		gbc_breakBox.insets = new Insets(0, 0, 0, 5);
		gbc_breakBox.gridx = 3;
		gbc_breakBox.gridy = 6;
		add(breakBox, gbc_breakBox);
		
		decimalBox = new JCheckBox("");
		GridBagConstraints gbc_decimalBox = new GridBagConstraints();
		gbc_decimalBox.insets = new Insets(0, 0, 0, 5);
		gbc_decimalBox.gridx = 4;
		gbc_decimalBox.gridy = 6;
		add(decimalBox, gbc_decimalBox);
		
		interruptBox = new JCheckBox("");
		GridBagConstraints gbc_interruptBox = new GridBagConstraints();
		gbc_interruptBox.insets = new Insets(0, 0, 0, 5);
		gbc_interruptBox.gridx = 5;
		gbc_interruptBox.gridy = 6;
		add(interruptBox, gbc_interruptBox);
		
		zeroBox = new JCheckBox("");
		GridBagConstraints gbc_zeroBox = new GridBagConstraints();
		gbc_zeroBox.insets = new Insets(0, 0, 0, 5);
		gbc_zeroBox.gridx = 6;
		gbc_zeroBox.gridy = 6;
		add(zeroBox, gbc_zeroBox);
		
		carryBox = new JCheckBox("");
		GridBagConstraints gbc_carryBox = new GridBagConstraints();
		gbc_carryBox.insets = new Insets(0, 0, 0, 5);
		gbc_carryBox.gridx = 7;
		gbc_carryBox.gridy = 6;
		add(carryBox, gbc_carryBox);
	}
	
	

}
