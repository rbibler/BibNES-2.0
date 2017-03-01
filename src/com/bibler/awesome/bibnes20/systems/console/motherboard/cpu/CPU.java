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
	private int effectiveAddressLow;
	private int effectiveAddressHigh;
	private int dataLatch;
	
	
	
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
		tN = 0;
	}
	
	
	public int cycle() {
		if(tN == 0) {
			fetchInstruction();
			tN++;
			return tN;
		}
		executeInstruction();
		tN++;
		programCounter &= 0xFFFF;
		return tN;
	}
	
	private void fetchInstruction() {
		addressBus.assertAddress(programCounter++);
		instructionLatch = dataBus.read();
	}
	
	private void executeInstruction() {
		boolean done = false;
		int BALX = 0;
		int BALY = 0;
		int carry;
		int result = 0;
		switch(instructionLatch) {
		case 0x00: 										// Brk
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				dataBus.latch((programCounter >> 8) & 0xFF);
				addressBus.assertAddressAndWrite(0x100 + (stackPointer--));
				break;
			case 3:
				dataBus.latch(programCounter & 0xFF);
				addressBus.assertAddressAndWrite(0x100 + (stackPointer--));
				break;
			case 4:
				dataBus.latch(statusRegister);
				addressBus.assertAddressAndWrite(0x100 + (stackPointer--));
				break;
			case 5:
				addressBus.assertAddress(0xFFFE);
				addressLatchLow = dataBus.read();
				break;
			case 6:
				addressBus.assertAddress(0xFFFF);
				addressLatchHigh = dataBus.read();
				programCounter = addressLatchLow | (addressLatchHigh << 8);
				break;
			}
			break;
		case 0x01:										// ORA, Indexed Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(BALX);
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress((BALX + 1) & 0xFF); 
				effectiveAddressHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x05:										// ORA, ZP
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~Z;
				}
				break;
			}
			break;
		case 0x06:										// ASL, ZP
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(addressLatchLow);
					dataLatch = dataBus.read();
					break;
				case 3:
					if((dataLatch & 0x80) > 0) {
						statusRegister |= C;
					} else {
						statusRegister &= ~C;
					}
					dataLatch = (dataLatch << 1) & 0xFF;
					break;
				case 4:
					dataBus.latch(dataLatch);
					addressBus.assertAddressAndWrite(addressLatchLow);
					tN = -1;
					if(dataLatch == 0) {
						statusRegister |= Z;
					} else {
						statusRegister &= ~Z;
					}
					if((dataLatch & S) > 0) {
						statusRegister |= S;
					} else {
						statusRegister &= ~S;
					}
					break;
			}
			break;
		case 0x08:										// PHP
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				dataBus.latch(statusRegister);
				addressBus.assertAddressAndWrite(0x100 + (stackPointer--));
				tN = -1;
				break;
			}
			break;
		case 0x09:										// ORA, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x0A:										// ASL, Accumulator
			if(tN == 1) {
				if((accumulator & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator << 1) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x0D:										// ORA, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x0E:										// ASL, Absolute
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					addressLatchHigh = dataBus.read();
					break;
				case 3:
					addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
					dataLatch = dataBus.read();
					break;
				case 4:
					if((dataLatch & 0x80) > 0) {
						statusRegister |= C;
					} else {
						statusRegister &= ~C;
					}
					dataLatch = (dataLatch << 1) & 0xFF;
					break;
				case 5:
					dataBus.latch(dataLatch);
					addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
					tN = -1;
					if(dataLatch == 0) {
						statusRegister |= Z;
					} else {
						statusRegister &= ~Z;
					}
					if((dataLatch & S) > 0) {
						statusRegister |= S;
					} else {
						statusRegister &= ~S;
					}
					break;
			}
			break;
		case 0x10:										// BPL, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & S) == 1) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0x11:										// ORA, Indirect Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(BALY | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x15:										// ORA, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x16:										// ASL, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				break;
			case 4:
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x18:										// CLC
			if(tN == 1) {
				statusRegister &= ~C;
				tN = -1;
			}
			break;
		case 0x19:										// ORA, Absolute Indexed Y
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					addressLatchHigh = dataBus.read();
					break;
				case 3:
					BALY = addressLatchLow + indexY;
					carry = ((BALY ^ addressLatchLow) >> 8) & 1;
					effectiveAddressHigh = addressLatchHigh + carry;
					effectiveAddressLow = BALY & 0xFF;
					if(carry == 0) {
						done = true;
					}
					break;
				case 4:
					effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
					effectiveAddressHigh = addressLatchHigh + 1;
					done = true;
					break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				accumulator = (accumulator | dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}	
			break;
		case 0x1D:										// ORA, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALX = addressLatchLow + indexX;
				carry = ((BALX ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALX & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
		}
		if(done) {
			addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
			dataLatch = dataBus.read();
			accumulator = (accumulator | dataLatch) & 0xFF;
			tN = -1;
			if(accumulator == 0) {
				statusRegister |= Z;
			} else {
				statusRegister &= ~Z;
			}
			if((accumulator & S) > 0) {
				statusRegister |= S;
			} else {
				statusRegister &= ~S;
			}
		}	
			break;
		case 0x1E:										// ASL, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = ((effectiveAddressLow ^ addressLatchLow) >> 8) & 1;
				effectiveAddressLow &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 5:
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				break;
			case 6:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x20:										// JSR, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(stackPointer);
				dataLatch = dataBus.read();
				break;
			case 3:
				dataBus.latch((programCounter >> 8) & 0xFF); 
				addressBus.assertAddressAndWrite(stackPointer--);
				break;
			case 4:
				dataBus.latch(programCounter & 0xFF);
				addressBus.assertAddressAndWrite(stackPointer--);
				break;
			case 5:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				programCounter = addressLatchLow | (addressLatchHigh << 8);
				tN = -1;
				break;
			}
			break;
		case 0x21:										// AND, Indexed Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(BALX);
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress((BALX + 1) & 0xFF); 
				effectiveAddressHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			
			break;
		case 0x24:										// BIT, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				result = dataLatch & accumulator;
				tN = -1;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if((dataLatch & V) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				break;
			}
			break;
		case 0x25:										// AND, Zero Page									
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
			break;
		case 0x26:										// ROL, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				if(shouldCarry) {
					dataLatch |= 1;
				}
				break;
			case 4:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x28:										// PLP
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(0x100 + stackPointer);
				dataLatch = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(0x100 + (++stackPointer));
				statusRegister = dataBus.read();
				tN = -1;
				break;
			}
			break;
		case 0x29:										// AND, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x2A:										// ROL, Accumulator
			if(tN == 1) {
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((accumulator & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator << 1) & 0xFF;
				if(shouldCarry) {
					accumulator |= 1;
				}
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x2C:										// BIT, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				result = dataLatch & accumulator;
				tN = -1;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if((dataLatch & V) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				break;
			}
			break;
		case 0x2D:										// AND, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x2E:										// ROL, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				if(shouldCarry) {
					dataLatch |= 1;
				}
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x30:										// BMI, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & S) == 0) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0x31:										// AND, Indirected Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8)); 
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x35:										// AND, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x36:										// ROL, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				break;
			case 4:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				if(shouldCarry) {
					dataLatch |= 1;
				}
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x38:										// SEC
			if(tN == 1) {
				statusRegister |= C;
				tN = -1;
			}
			break;
		case 0x39:										// AND, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALY = addressLatchLow + indexY;
				carry = ((BALY ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALY & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8)); 
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x3D:										// AND, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALX = addressLatchLow + indexX;
				carry = ((BALX ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALX & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
		}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8)); 
				dataLatch = dataBus.read();
				accumulator = (accumulator & dataLatch) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x3E:										// ROL, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = ((effectiveAddressLow ^ addressLatchLow) >> 8) & 1;
				effectiveAddressLow &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 5:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				if(shouldCarry) {
					dataLatch |= 1;
				}
				break;
			case 6:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x40:										// RTI
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(0x100 + stackPointer);
				dataLatch = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(0x100 + (++stackPointer));
				statusRegister = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(0x100 + (++stackPointer));
				addressLatchLow = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(0x100 + (++stackPointer));
				addressLatchHigh = dataBus.read();
				programCounter = addressLatchLow | (addressLatchHigh << 8);
				tN = -1;
				break;
			}
			break;
		case 0x41:										// EOR, Indexed Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(BALX);
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress((BALX + 1) & 0xFF); 
				effectiveAddressHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x45:										// EOR, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
			break;
		case 0x46:										// LSR, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				if((dataLatch & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch >> 1) & 0xFF;
				break;
			case 4:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x48:										// PHA
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				dataBus.latch(accumulator);
				addressBus.assertAddressAndWrite(0x100 + (stackPointer--));
				tN = -1;
				break;
			}
			break;
		case 0x49:										// EOR, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x4A:										// LSR, Accumulator
			if(tN == 1) {
				if((accumulator & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator >> 1) & 0xFF;
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x4C:										// JMP, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				programCounter = effectiveAddressLow | (effectiveAddressHigh << 8);
				tN = -1;
				break;
				
			}
			break;
		case 0x4D:										// EOR, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x4E:										// LSR, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				if((dataLatch & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch >> 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x50:										// BVC, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & V) == 1) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0x51:										// EOR, Indirect Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				
			}
			break;
		case 0x55:										// EOR, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
			}
			break;
		case 0x56:										// LSR, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				break;
			case 4:
				if((dataLatch & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch >> 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x58:										// CLI
			if(tN == 1) {
				statusRegister &= ~I;
				tN = -1;
			}
			break;
		case 0x59:										// EOR, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALY = addressLatchLow + indexY;
				carry = ((BALY ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALY & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				
			}
			break;
		case 0x5D:										// EOR, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALX = addressLatchLow + indexX;
				carry = ((BALX ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALX & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				accumulator = (accumulator ^ dataLatch) & 0xFF;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				
			}
			break;
		case 0x5E:										// LSR, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = ((effectiveAddressLow ^ addressLatchLow) >> 8) & 1;
				effectiveAddressLow &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 5:
				if((dataLatch & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch >> 1) & 0xFF;
				break;
			case 6:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x60:										// RTS
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(0x100 + stackPointer);
				dataLatch = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(0x100 + (++stackPointer));
				addressLatchLow = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(0x100 + (++stackPointer));
				addressLatchHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				dataLatch = dataBus.read();
				programCounter = addressLatchLow | (addressLatchHigh << 8);
				programCounter++;
				tN = -1;
				break;
			}
			break;
		case 0x61:										// ADC, Indexed Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(BALX);
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress((BALX + 1) & 0xFF); 
				effectiveAddressHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				break;
			}
			break;
		case 0x65:										// ADC, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				break;
			}
			
			break;
		case 0x66:										// ROR, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((accumulator & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator >> 1) & 0xFF;
				if(shouldCarry) {
					accumulator |= 0x80;
				}
				break;
			case 4:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x68:										// PLA
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter);
				dataLatch = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(0x100 + stackPointer);
				dataLatch = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(0x100 + (++stackPointer));
				accumulator = dataBus.read();
				tN = -1;
				break;
			}
			break;
		case 0x69:										// ADC, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
			}
			break;
		case 0x6A:										// ROR, Accumulator
			if(tN == 1) {
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((accumulator & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator >> 1) & 0xFF;
				if(shouldCarry) {
					accumulator |= 0x80;
				}
				tN = -1;
				if(accumulator == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & 80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
			}
			break;
		case 0x6C:										// JMP, Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | (addressLatchHigh << 8));
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress((addressLatchLow | (addressLatchHigh << 8)) + 1);
				effectiveAddressHigh = dataBus.read();
				programCounter = effectiveAddressLow | (effectiveAddressHigh << 8);
				tN = -1;
				break;
			}
			break;
		case 0x6D:										// ADC, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				break;
			}
			break;
		case 0x6E:										// ROR, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((dataLatch & 0x80) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				dataLatch = (dataLatch << 1) & 0xFF;
				if(shouldCarry) {
					dataLatch |= 1;
				}
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x70:										// BVS, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & V) == 0) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0x71:										// ADC, Indirect Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
			}
			break;
		case 0x75:										// ADC, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				break;
			}
			break;
		case 0x76:										// ROR, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				break;
			case 4:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((accumulator & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator >> 1) & 0xFF;
				if(shouldCarry) {
					accumulator |= 0x80;
				}
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0x78:										// SEI
			if(tN == 1) {
				statusRegister |= I;
				tN = -1;
			}
			break;
		case 0x79:										// ADC, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALY = addressLatchLow + indexY;
				carry = ((BALY ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALY & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
			}
			break;
		case 0x7D:										// ADC, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALX = addressLatchLow + indexX;
				carry = ((BALX ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALX & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = (dataLatch + accumulator + (statusRegister & C));
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~C;
				}
				if((result & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(((result >> 8) & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if( ((result ^ accumulator) & (result ^ dataLatch) & 0x80) > 0) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
			}
			break;
		case 0x7E:										// ROR, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = ((effectiveAddressLow ^ addressLatchLow) >> 8) & 1;
				effectiveAddressLow &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 5:
				final boolean shouldCarry = (statusRegister & C) == 1;
				if((accumulator & 1) > 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				accumulator = (accumulator >> 1) & 0xFF;
				if(shouldCarry) {
					accumulator |= 0x80;
				}
				break;
			case 6:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;								
		case 0x81:										// STA, Indexed Indirect
			switch(tN) {
				case 1:	
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(addressLatchLow);
					dataBus.read();
					break;
				case 3:
					addressBus.assertAddress(addressLatchLow + indexX);
					effectiveAddressLow = dataBus.read();
					break;
				case 4:
					addressBus.assertAddress(addressLatchLow + indexX + 1);
					effectiveAddressHigh = dataBus.read();
					break;
				case 5:
					dataBus.latch(accumulator);
					addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
					tN = -1;
					break;
			}
			break;
		case 0x84:										// STY, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				dataBus.latch(indexY);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				break;
		}
			break;
		case 0x85:										// STA, Zero Page
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					effectiveAddressLow = dataBus.read();
					break;
				case 2:
					dataBus.latch(accumulator);
					addressBus.assertAddressAndWrite(effectiveAddressLow);
					tN = -1;
					break;
			}
			break;
		case 0x86:										// STX, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				dataBus.latch(indexX);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				break;
		}
			break;
		case 0x88:										// DEY
			if(tN == 1) {
				indexY = (indexY - 1) & 0xFF;
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
		case 0x8A:										// TXA
			if(tN == 1) {
				accumulator = indexX;
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
		case 0x8C:										// STY, Absolute
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					effectiveAddressLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					effectiveAddressHigh = dataBus.read();
					break;
				case 3:
					dataBus.latch(indexY);
					addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
					tN = -1;
					break;
			}
			break;
		case 0x8D:										// STA, Absolute
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					effectiveAddressLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					effectiveAddressHigh = dataBus.read();
					break;
				case 3:
					dataBus.latch(accumulator);
					addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
					tN = -1;
					break;
			}
			break;
		case 0x8E:										// STX, Absolute
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					effectiveAddressLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					effectiveAddressHigh = dataBus.read();
					break;
				case 3:
					dataBus.latch(indexX);
					addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
					tN = -1;
					break;
		}
			break;
		case 0x90:										// BCC, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & C) == 1) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0x91:										// STA, Indirect Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
				dataBus.read();
				break;
			case 5:
				dataBus.latch(accumulator);
				addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
				tN = -1;
				break;					
			}
			break;
		case 0x94:										// STY, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = 0;
				dataBus.latch(indexY);
				addressBus.assertAddressAndWrite(effectiveAddressLow | (effectiveAddressHigh << 8));
				tN = -1;
				break;
			}
			break;
		case 0x95:										// STA, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = 0;
				dataBus.latch(accumulator);
				addressBus.assertAddressAndWrite(effectiveAddressLow | (effectiveAddressHigh << 8));
				tN = -1;
				break;
			}
			break;
		case 0x96:										// STX, Zero Page Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = 0;
				dataBus.latch(indexX);
				addressBus.assertAddressAndWrite(effectiveAddressLow | (effectiveAddressHigh << 8));
				tN = -1;
				break;
			}
			break;
		case 0x98:										// TYA
			if(tN == 1) {
				accumulator = indexY;
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
		case 0x99:										// STA, Absolute Indexed Y
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					addressLatchHigh = dataBus.read();
					break;
				case 3:
					effectiveAddressLow = addressLatchLow + indexY;
					carry = (effectiveAddressLow ^ addressLatchLow) >> 8 & 1;
					effectiveAddressHigh = addressLatchHigh + carry;
					effectiveAddressLow &= 0xFF;
					addressBus.assertAddress(effectiveAddressLow);
					break;
				case 4:
					dataBus.latch(accumulator);
					addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
					tN = -1;
					break;
			}
			
			break;
		case 0x9A:										// TXS
			if(tN == 1) {
				stackPointer = indexX;
				tN = -1;
				if((stackPointer & S) == 0) {
					statusRegister &= ~S;
				} else {
					statusRegister |= S;
				}
				if(stackPointer == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
			}
			break;
		case 0x9D:										// STA, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = (effectiveAddressLow ^ addressLatchLow) >> 8 & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow &= 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 4:
				dataBus.latch(accumulator);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				break;
		}
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
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(addressLatchLow);
					break;
				case 3:
					addressBus.assertAddress(addressLatchLow + indexX);
					effectiveAddressLow = dataBus.read();
					break;
				case 4:
					addressBus.assertAddress(addressLatchLow + indexX + 1);
					effectiveAddressHigh = dataBus.read();
					break;
				case 5:
					addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
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
					break;
			}
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
			if(tN == 1) {
				indexY = accumulator;
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
			if(tN == 1) {
				indexX = accumulator;
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
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & C) == 0) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0xB1:										// LDA, Indirect Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
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
		case 0xB4:										// LDY, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = 0;
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
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
				
				break;
			}
			break;
		case 0xB5:										// LDA, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = 0;
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
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
				
				break;
			}
			break;
		case 0xB6:										// LDX, Zero Page Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = 0;
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
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
				
				break;
			}
			break;
		case 0xB8:										// CLV
			if(tN == 1) {
				statusRegister &= ~V;
				tN = -1;
			}
			break;
		case 0xB9:										// LDA, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexY;
				carry = (effectiveAddressLow ^ addressLatchLow) >> 8 & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress((effectiveAddressLow & 0xFF) | effectiveAddressHigh << 8);
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				done = true;
				break;
		}
		if(done) {
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
		case 0xBA:										// TSX
			if(tN == 1) {
				indexX = stackPointer;
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
		case 0xBC:										// LDY, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = (effectiveAddressLow ^ addressLatchLow) >> 8 & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress((effectiveAddressLow & 0xFF) | effectiveAddressHigh << 8);
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				done = true;
				break;
		}
		if(done) {
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
		case 0xBD:										// LDA, Absolute X
			switch(tN) {
				case 1:
					addressBus.assertAddress(programCounter++);
					addressLatchLow = dataBus.read();
					break;
				case 2:
					addressBus.assertAddress(programCounter++);
					addressLatchHigh = dataBus.read();
					break;
				case 3:
					effectiveAddressLow = addressLatchLow + indexX;
					carry = (effectiveAddressLow ^ addressLatchLow) >> 8 & 1;
					effectiveAddressHigh = addressLatchHigh + carry;
					addressBus.assertAddress((effectiveAddressLow & 0xFF) | effectiveAddressHigh << 8);
					if(carry == 0) {
						done = true;
					}
					break;
				case 4:
					effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
					effectiveAddressHigh = addressLatchHigh + 1;
					addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
					done = true;
					break;
			}
			if(done) {
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
		case 0xBE:										// LDX, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexY;
				carry = (effectiveAddressLow ^ addressLatchLow) >> 8 & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress((effectiveAddressLow & 0xFF) | effectiveAddressHigh << 8);
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				done = true;
				break;
		}
		if(done) {
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
		case 0xC0:										// CPY, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				tN = -1;
				result = indexY - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((indexY & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				
			}
			break;
		case 0xC1:										// CMP, Indexed Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(BALX);
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress((BALX + 1) & 0xFF); 
				effectiveAddressHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xC4:										// CPY, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				tN = -1;
				result = indexY - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((indexY & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xC5:										// CMP, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			
			break;
		case 0xC6:										// DEC, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				dataLatch = (dataLatch - 1) & 0xFF;
				break;
			case 4:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xC8:										// INY
			if(tN == 1) {
				indexY = (indexY + 1) & 0xFF;
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
		case 0xC9:										// CMP, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				
			}
			break;
		case 0xCA:										// DEX
			if(tN == 1) {
				indexX = (indexX - 1) & 0xFF;
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
		case 0xCC:										// CPY, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = indexY - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((indexY & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xCD:										// CMP, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xCE:										// DEC, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				dataLatch = (dataLatch - 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xD0:										// BNE, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & Z) == 1) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0xD1:										// CMP, Indirect Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
			}
			break;
		case 0xD5:										// CMP, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xD6:										// DEC, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				break;
			case 4:
				dataLatch = (dataLatch - 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xD8:										// CLD
			if(tN == 1) {
				statusRegister &= ~D;
				tN = -1;
			}
			break;
		case 0xD9:										// CMP, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALY = addressLatchLow + indexY;
				carry = ((BALY ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALY & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
			}
			break;
		case 0xDD:										// CMP, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALX = addressLatchLow + indexX;
				carry = ((BALX ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALX & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = accumulator - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((accumulator & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
			}
			break;
		case 0xDE:										// DEC, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = ((effectiveAddressLow ^ addressLatchLow) >> 8) & 1;
				effectiveAddressLow &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 5:
				dataLatch = (dataLatch - 1) & 0xFF;
				break;
			case 6:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xE0:										// CPX, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				tN = -1;
				result = indexX - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((indexX & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				
			}
			break;
		case 0xE1:										// SBC, Indexed Indirect
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(BALX);
				effectiveAddressLow = dataBus.read();
				break;
			case 4:
				BALX = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress((BALX + 1) & 0xFF); 
				effectiveAddressHigh = dataBus.read();
				break;
			case 5:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
			break;
			}
		case 0xE4:										// CPX, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				tN = -1;
				result = indexX - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((indexX & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xE5:										// SBC, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
				break;
			}
			break;
		case 0xE6:										// INC, Zero Page
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				dataLatch = dataBus.read();
				break;
			case 3:
				dataLatch = (dataLatch + 1) & 0xFF;
				break;
			case 4:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xE8:										// INX
			if(tN == 1) {
				indexX = (indexX + 1) & 0xFF;
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
		case 0xE9:										// SBC, Immediate
			if(tN == 1) {
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
			}
			break;
		case 0xEA:										// NOP
			programCounter++;
			if(tN == 2) {
				tN = -1;
			}
			break;
		case 0xEC:										// CPX, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				tN = -1;
				result = indexX - dataLatch;
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((indexX & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				break;
			}
			break;
		case 0xED:										// SBC, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				effectiveAddressLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				effectiveAddressHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
				break;
			}
			break;
		case 0xEE:										// INC, Absolute
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow | addressLatchHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				dataLatch = (dataLatch + 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(addressLatchLow | addressLatchHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xF0:										// BEQ, Relative
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				dataLatch = dataBus.read();
				if((statusRegister & Z) == 0) {											// Branch not taken. Done with inst.
					tN = -1;
				}
				break;
			case 2:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				if((result & 0x100) == (programCounter & 0x100)) {
					done = true;
				}
				break;
			case 3:
				if((dataLatch & 0x80) > 0) {
					result = -(((~dataLatch) + 1) & 0xFF) + programCounter;
				} else {
					result = programCounter + dataLatch;
				}
				done = true;
				break;
			}
			if(done) {
				tN = -1;
				programCounter = result;
			}
			break;
		case 0xF1:										// SBC, Indirect Indexed	
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(addressLatchLow);
				effectiveAddressLow = dataBus.read();
				break;
			case 3:
				addressBus.assertAddress(addressLatchLow + 1);
				addressLatchHigh = dataBus.read();
				break;
			case 4:
				BALY = effectiveAddressLow + indexY;
				carry = ((effectiveAddressLow ^ BALY) >> 8) & 1;
				BALY &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				if(carry == 0) {
					done = true;
				}
				break;
			case 5:
				BALY = (effectiveAddressLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
			}
			break;
		case 0xF5:										// SBC, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
				break;
			}
			break;
		case 0xF6:										// INC, Zero Page Indexed
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				effectiveAddressLow = (addressLatchLow + indexX) & 0xFF;
				addressBus.assertAddress(effectiveAddressLow);
				break;
			case 3:
				addressBus.assertAddress(effectiveAddressLow);
				dataLatch = dataBus.read();
				break;
			case 4:
				dataLatch = (dataLatch + 1) & 0xFF;
				break;
			case 5:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		case 0xF8:										// SED
			if(tN == 1) {
				statusRegister |= D;
				tN = -1;
			}
			break;
		case 0xF9:										// SBC, Absolute Indexed Y
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALY = addressLatchLow + indexY;
				carry = ((BALY ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALY & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
			}
			break;
		case 0xFD:										// SBC, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				BALX = addressLatchLow + indexX;
				carry = ((BALX ^ addressLatchLow) >> 8) & 1;
				effectiveAddressHigh = addressLatchHigh + carry;
				effectiveAddressLow = BALX & 0xFF;
				if(carry == 0) {
					done = true;
				}
				break;
			case 4:
				effectiveAddressLow = (addressLatchLow + indexY) & 0xFF;
				effectiveAddressHigh = addressLatchHigh + 1;
				done = true;
				break;
			}
			if(done) {
				addressBus.assertAddress(effectiveAddressLow | (effectiveAddressHigh << 8));
				dataLatch = dataBus.read();
				result = ~dataLatch + (statusRegister & C) + accumulator;
				if(result >= 0) {
					statusRegister |= C;
				} else {
					statusRegister &= ~C;
				}
				if((result & 0x80) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				if(result == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				
				if( (((accumulator ^ dataLatch) & 0x80) != 0)
		                && (((accumulator ^ result) & 0x80) != 0)) {
					statusRegister |= V;
				} else {
					statusRegister &= ~V;
				}
				accumulator = result & 0xFF;
				tN = -1;
			}
			break;
		case 0xFE:										// INC, Absolute Indexed X
			switch(tN) {
			case 1:
				addressBus.assertAddress(programCounter++);
				addressLatchLow = dataBus.read();
				break;
			case 2:
				addressBus.assertAddress(programCounter++);
				addressLatchHigh = dataBus.read();
				break;
			case 3:
				effectiveAddressLow = addressLatchLow + indexX;
				carry = ((effectiveAddressLow ^ addressLatchLow) >> 8) & 1;
				effectiveAddressLow &= 0xFF;
				effectiveAddressHigh = addressLatchHigh + carry;
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 4:
				addressBus.assertAddress(effectiveAddressLow | effectiveAddressHigh << 8);
				dataLatch = dataBus.read();
				break;
			case 5:
				dataLatch = (dataLatch + 1) & 0xFF;
				break;
			case 6:
				dataBus.latch(dataLatch);
				addressBus.assertAddressAndWrite(effectiveAddressLow | effectiveAddressHigh << 8);
				tN = -1;
				if(dataLatch == 0) {
					statusRegister |= Z;
				} else {
					statusRegister &= ~Z;
				}
				if((dataLatch & S) > 0) {
					statusRegister |= S;
				} else {
					statusRegister &= ~S;
				}
				break;
		}
		break;
		}
	}

	public int[] getStatusUpdate() {
		return new int[] { programCounter, stackPointer, statusRegister, accumulator, indexX, indexY};
	}
	
	
	
	
}
