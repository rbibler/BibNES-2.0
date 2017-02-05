package com.bibler.awesome.bibnes20.systems.console.motherboard.cpu;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.AddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.DataBus;

public class CPU {
	
	private final int S = 0x80;
	private final int V = 0x40;
	private final int B = 0x10;
	private final int D = 0x08;
	private final int I = 0x04;
	private final int Z = 0x02;
	private final int C = 0x01;
	
	private AddressBus addressBus;
	private DataBus dataBus;
	
	private int statusRegister;
	private int accumulator;
	private int stackPointer;
	private int programCounter;
	private int indexX;
	private int indexY;
	private int instructionLatch;
	private int addressLatchLow;
	private int addressLatchHigh;
	
	
	private int tN;
	
	public CPU(AddressBus addressBus, DataBus dataBus) {
		this.addressBus = addressBus;
		this.dataBus = dataBus;
	}
	
	public void reset() {
		programCounter = 0xFFFC;
		addressBus.assertAddress(programCounter++);
		addressLatchLow = dataBus.read();
		addressBus.assertAddress(programCounter++);
		addressLatchHigh = dataBus.read();
		programCounter = addressLatchLow | addressLatchHigh << 8;
		tN = -1;
	}
	
	
	public void cycle() {
		if(tN == 0) {
			fetchInstruction();
			tN++;
			return;
		}
		executeInstruction();
		tN++;
		programCounter &= 0xFFFF;
	}
	
	public void printStatus() {
		System.out.println("PC: " + Integer.toHexString(programCounter) + 
				"Inst: " + Integer.toHexString(instructionLatch) + 
				" A: " + accumulator + " X: " + indexX + " Y: " + indexY);
	}
	
	private void fetchInstruction() {
		addressBus.assertAddress(programCounter++);
		instructionLatch = dataBus.read();
	}
	
	private void executeInstruction() {
		switch(instructionLatch) {
		case 0x00: 										// Brk
			
			break;
		case 0x01:										// ORA, Indexed Indirect
			
			break;
		case 0x05:										// ORA, ZP
			
			break;
		case 0x06:										// ASL, ZP
			
			break;
		case 0x08:										// PHP
			
			break;
		case 0x09:										// ORA, Immediate
			
			break;
		case 0x0A:										// ASL, Accumulator
			
			break;
		case 0x0D:										// ORA, Absolute
			
			break;
		case 0x0E:										// ASL, Absolute
			
			break;
		case 0x10:										// BPL, Relative
			
			break;
		case 0x11:										// ORA, Indirect Indexed
			
			break;
		case 0x15:										// ORA, Zero Page Indexed
			
			break;
		case 0x16:										// ASL, Zero Page Indexed
			
			break;
		case 0x18:										// CLC
			
			break;
		case 0x19:										// ORA, Absolute Indexed Y
			
			break;
		case 0x1D:										// ORA, Absolute Indexed X
			
			break;
		case 0x1E:										// ASL, Absolute Indexed X
			
			break;
		case 0x20:										// JSR, Absolute
			
			break;
		case 0x21:										// AND, Indexed Indirect
			
			break;
		case 0x24:										// BIT, Zero Page
			
			break;
		case 0x25:										// AND, Zero Page									
			
			break;
		case 0x26:										// ROL, Zero Page
			
			break;
		case 0x28:										// PLP
			
			break;
		case 0x29:										// AND, Immediate
			
			break;
		case 0x2A:										// ROL, Accumulator
			
			break;
		case 0x2C:										// BIT, Absolute
			
			break;
		case 0x2D:										// AND, Absolute
			
			break;
		case 0x2E:										// ROL, Absolute
			
			break;
		case 0x30:										// BMI, Relative
			
			break;
		case 0x31:										// AND, Indirected Indexed
			
			break;
		case 0x35:										// AND, Zero Page Indexed
			
			break;
		case 0x36:										// ROL, Zero Page Indexed
			
			break;
		case 0x38:										// SEC
			
			break;
		case 0x39:										// AND, Absolute Indexed Y
			
			break;
		case 0x3D:										// AND, Absolute Indexed X
			
			break;
		case 0x3E:										// ROL, Absolute Indexed X
			
			break;
		case 0x40:										// RTI
			
			break;
		case 0x41:										// EOR, Indexed Indirect
			
			break;
		case 0x45:										// EOR, Zero Page
			
			break;
		case 0x46:										// LSR, Zero Page
			
			break;
		case 0x48:										// PHA
			
			break;
		case 0x49:										// EOR, Immediate
			
			break;
		case 0x4A:										// LSR, Accumulator
			
			break;
		case 0x4C:										// JMP, Absolute
			
			break;
		case 0x4D:										// EOR, Absolute
			
			break;
		case 0x4E:										// LSR, Absolute
			
			break;
		case 0x50:										// BVC, Relative
			
			break;
		case 0x51:										// EOR, Indirect Indexed
			
			break;
		case 0x55:										// EOR, Zero Page Indexed
			
			break;
		case 0x56:										// LSR, Zero Page Indexed
			
			break;
		case 0x58:										// CLI
			
			break;
		case 0x59:										// EOR, Absolute Indexed Y
			
			break;
		case 0x5D:										// EOR, Absolute Indexed X
			
			break;
		case 0x5E:										// LSR, Absolute Indexed X
			
			break;
		case 0x60:										// RTS
			
			break;
		case 0x61:										// ADC, Indexed Indirect
			
			break;
		case 0x65:										// ADC, Zero Page
			
			break;
		case 0x66:										// ROR, Zero Page
			
			break;
		case 0x68:										// PLA
			
			break;
		case 0x69:										// ADC, Immediate
			
			break;
		case 0x6A:										// ROR, Accumulator
			
			break;
		case 0x6C:										// JMP, Indirect
			
			break;
		case 0x6D:										// ADC, Absolute
			
			break;
		case 0x6E:										// ROR, Absolute
			
			break;
		case 0x70:										// BVS, Relative
			
			break;
		case 0x71:										// ADC, Indirect Indexed
			
			break;
		case 0x75:										// ADC, Zero Page Indexed
			
			break;
		case 0x76:										// ROR, Zero Page Indexed
			
			break;
		case 0x78:										// SEI
			
			break;
		case 0x79:										// ADC, Absolute Indexed Y
			
			break;
		case 0x7D:										// ADC, Absolute Indexed X
			
			break;
		case 0x7E:										// ROR, Absolute Indexed X
			
			break;
		case 0x81:										// STA, Indexed Indirect
			
			break;
		case 0x84:										// STY, Zero Page
			
			break;
		case 0x85:										// STA, Zero Page
			
			break;
		case 0x86:										// STX, Zero Page
			
			break;
		case 0x88:										// DEY
			
			break;
		case 0x8A:										// TXA
			
			break;
		case 0x8C:										// STY, Absolute
			
			break;
		case 0x8D:										// STA, Absolute
			
			break;
		case 0x8E:										// STX, Absolute
			
			break;
		case 0x90:										// BCC, Relative
			
			break;
		case 0x91:										// STA, Indirect Indexed
			
			break;
		case 0x94:										// STY, Zero Page Indexed
			
			break;
		case 0x95:										// STA, Zero Page Indexed
			
			break;
		case 0x96:										// STX, Zero Page Indexed Y
			
			break;
		case 0x98:										// TYA
			
			break;
		case 0x99:										// STA, Absolute Indexed Y
			
			break;
		case 0x9A:										// TXS
			
			break;
		case 0x9D:										// STA, Absolute Indexed X
			
			break;
		case 0xA0:										// LDY, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				indexY = dataBus.read();
				tN = -1;
				if((indexY & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(indexY == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xA1:										// LDA, Indexed Indirect
			
			break;
		case 0xA2:										// LDX, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				indexX = dataBus.read();
				tN = -1;
				if((indexX & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(indexX == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xA4:										// LDY, Zero Page
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
			} else if(tN == 2) {
				addressBus.assertAddress(addressLatchLow);
				indexY = dataBus.read();
				tN = -1;
				if((indexY & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(indexY == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xA5:										// LDA, Zero Page
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
			} else if(tN == 2) {
				addressBus.assertAddress(addressLatchLow);
				accumulator = dataBus.read();
				tN = -1;
				if((accumulator & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xA6:										// LDX, Zero Page
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
			} else if(tN == 2) {
				addressBus.assertAddress(addressLatchLow);
				indexX = dataBus.read();
				tN = -1;
				if((indexX & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(indexX == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xA8:										// TAY
			
			break;
		case 0xA9:										// LDA, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				accumulator = dataBus.read();
				tN = -1;
				if((accumulator & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xAA:										// TAX
			
			break;
		case 0xAC:										// LDY, Absolute
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
			} else if(tN == 2) {
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
			} else if(tN == 3) {
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				indexY = dataBus.read();
				tN = -1;
				if((indexY & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(indexY == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xAD:										// LDA, Absolute
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
			} else if(tN == 2) {
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
			} else if(tN == 3) {
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				accumulator = dataBus.read();
				tN = -1;
				if((accumulator & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xAE:										// LDX, Absolute
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
			} else if(tN == 2) {
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
			} else if(tN == 3) {
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				indexX = dataBus.read();
				tN = -1;
				if((indexX & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(indexX == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0xB0:										// BCS, Relative
			
			break;
		case 0xB1:										// LDA, Indirect Indexed
			
			break;
		case 0xB4:										// LDY, Zero Page Indexed
			
			break;
		case 0xB5:										// LDA, Zero Page Indexed
			
			break;
		case 0xB6:										// LDX, Zero Page Indexed Y
			
			break;
		case 0xB8:										// CLV
			
			break;
		case 0xB9:										// LDA, Absolute Indexed Y
			
			break;
		case 0xBA:										// TSX
			
			break;
		case 0xBC:										// LDY, Absolute Indexed X
			
			break;
		case 0xBD:										// LDA, Absolute X
			
			break;
		case 0xBE:										// LDX, Absolute Indexed Y
			
			break;
		case 0xC0:										// CPY, Immediate
		
			break;
		case 0xC1:										// CMP, Indexed Indirect
			
			break;
		case 0xC4:										// CPY, Zero Page
			
			break;
		case 0xC5:										// CMP, Zero Page
			
			break;
		case 0xC6:										// DEC, Zero Page
			
			break;
		case 0xC8:										// INY
			
			break;
		case 0xC9:										// CMP, Immediate
			
			break;
		case 0xCA:										// DEX
			
			break;
		case 0xCC:										// CPY, Absolute
			
			break;
		case 0xCD:										// CMP, Absolute
			
			break;
		case 0xCE:										// DEC, Absolute
			
			break;
		case 0xD0:										// BNE, Relative
			
			break;
		case 0xD1:										// CMP, Indirect Indexed
			
			break;
		case 0xD5:										// CMP, Zero Page Indexed
			
			break;
		case 0xD6:										// DEC, Zero Page Indexed
			
			break;
		case 0xD8:										// CLD
			
			break;
		case 0xD9:										// CMP, Absolute Indexed Y
			
			break;
		case 0xDD:										// CMP, Absolute Indexed X
			
			break;
		case 0xDE:										// DEC, Absolute Indexed X
			
			break;
		case 0xE0:										// CPX, Immediate
			
			break;
		case 0xE1:										// SBC, Indexed Indirect
			
			break;
		case 0xE4:										// CPX, Zero Page
			
			break;
		case 0xE5:										// SBC, Zero Page
		
			break;
		case 0xE6:										// INC, Zero Page
			
			break;
		case 0xE8:										// INX
			
			break;
		case 0xE9:										// SBC, Immediate
			
			break;
		case 0xEA:										// NOP
			programCounter++;
			if(tN == 2) {
				tN = -1;
			}
			break;
		case 0xEC:										// CPX, Absolute
			
			break;
		case 0xED:										// SBC, Absolute
			
			break;
		case 0xEE:										// INC, Absolute
			
			break;
		case 0xF0:										// BEQ, Relative
			
			break;
		case 0xF1:										// SBC, Indirect Indexed
			
			break;
		case 0xF5:										// SBC, Zero Page Indexed
			
			break;
		case 0xF6:										// INC, Zero Page Indexed
			
			break;
		case 0xF8:										// SED
			
			break;
		case 0xF9:										// SBC, Absolute Indexed Y
			
			break;
		case 0xFD:										// SBC, Absolute Indexed X
			
			break;
		case 0xFE:										// INC, Absolute Indexed X
			
			break;
		
		}
	}
	
	
	
	
}
