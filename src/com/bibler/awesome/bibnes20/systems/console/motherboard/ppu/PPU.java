package com.bibler.awesome.bibnes20.systems.console.motherboard.ppu;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.PPUAddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.utilities.BitUtils;

public class PPU {
	
	private PPUAddressBus addressBus;
	private RAM paletteRam = new RAM(0x20);
	private CPU cpu;
	private int[] frame;
	int cycleCount;
	
	private int linesPerFrame = 262;
	private int dotsPerLine = 341;
	
	private int currentDot;
	private int currentScanline;
	
	private int PPU_CTRL;
	private int PPU_MASK;
	private int PPU_STATUS;
	private int OAM_ADDR;
	private int OAM_DATA;
	private int PPU_DATA;
	private int OAM_DMA;
	private int xScroll;
	private int yScroll;
	
	private int t;
	private int v;
	private int w;
	
	private int ntByte;
	private int atByte;
	private int ptByteLow;
	private int ptByteHigh;
	
	private int bgShiftLow;
	private int bgShiftHigh;
	private int bgAtShiftLow;
	private int bgAtShiftHigh;
	private int bgAtLatchLow;
	private int bgAtLatchHigh;
	
	private int oddEvenLine;
	
	public PPU() {
		frame = new int[256 * 240];
		//currentScanline = 261;
	}
	
	public void reset() {
		t = 0;
		v = 0;
		w = 0;
		xScroll = 0;
		currentScanline = 261;
		currentDot = 0;
		oddEvenLine = 0;
	}
	
	public void setAddressBus(PPUAddressBus addressBus) {
		this.addressBus = addressBus;
	}
	
	public int read(int registerToRead) {
		switch(registerToRead) {
		case 0x02:
			w = 0;
			final int retValue = PPU_STATUS;
			return retValue;
		}
		return 0;
	}
	
	public void write(int registerToWrite, int dataToWrite) {
		switch(registerToWrite) {
		case 0:
			PPU_CTRL = dataToWrite;
			t &= ~0xC;
			t = (dataToWrite & 3) << 2;
			break;
		case 1:
			PPU_MASK = dataToWrite;
			break;
		case 3:
			OAM_ADDR = dataToWrite;
			break;
		case 4:
			OAM_DATA = dataToWrite;
			break;
		case 5:
			if(w == 0) {
				t &= ~0x1F;
				t |= (dataToWrite >> 3) & 0x1F;
				xScroll = dataToWrite & 7;
				w = 1;
			} else {
				t &= ~0x73E0;
				t |= (dataToWrite & 3) << 12;
				t |= ((dataToWrite >> 3) & 0x1F) << 5;
				w = 0;
			}
			break;
		case 6:
			//System.out.println("Address write w " + w + " : " + Integer.toHexString(dataToWrite).toUpperCase());
			if(w == 0) {
				t &= ~0x7F00;
				t |= (dataToWrite & 0x3F) << 8;
				w = 1;
				
			} else {
				t &= ~0xFF;
				t |= (dataToWrite & 0xFF);
				w = 0;
				v = t;
			}
			
			break;
		case 7:
			PPU_DATA = dataToWrite;
			if(v >= 0x3F00) {
				paletteRam.write(v - 0x3F00, dataToWrite);
			} else {
				addressBus.latch(dataToWrite);
				addressBus.assertAddressAndWrite(v);
			}
			final int vInc = (PPU_CTRL >> 2 & 1) == 0 ? 1 : 32;
			if(!renderingAndVisible()) {
				v += vInc;
			} else {
				incrementHorizontal();
				incrementVertical();
			}
			break;
		}
	}
	
	private boolean renderingAndVisible() {
		return renderOn() && (currentScanline <= 240 || currentScanline == 261);
	}
	
	private boolean renderOn() {
		return (PPU_MASK & 0x8) > 0 || (PPU_MASK & 0x10) > 0;
	}
	
	public void cycle() {
		if(renderOn()) {
			cycleRenderOn();
		} else {
			cycleRenderOff();
		}
		currentDot++;
		if(currentDot == dotsPerLine) {
			currentScanline++;
			currentDot = 0;
			if(currentScanline == linesPerFrame) {
				currentDot = 0;
				currentScanline = 0;
				System.out.println("-----Frame-----");
			}
		}
		//System.out.println("Scanline: " + currentScanline + " Pixel: " + currentDot);
	}
	
	private void cycleRenderOn() {
		if(currentScanline < 240 && currentDot >= 2) {
			if(currentDot <= 256) {
				renderDot();
			}
			if(currentDot <= 257 || (currentDot >= 322 && currentDot <= 337)) {
				shiftBGRegisters();
			}
		}
		if(currentScanline < 261) {
			if(currentScanline < 240) {
				if(currentDot < 257) {
					evaluateBG();
					if(currentDot == 256) {
						incrementVertical();
					}
				} else if(currentDot == 257) {
					v &= ~0x41F;
					v |= t & 0x41F;
					updateShiftRegisters();
				} else if(currentDot >= 321 && currentDot < 337) {
					evaluateBG();
				} else if(currentDot >= 337 && currentDot <= 340) {
					if(currentDot == 337) {
						updateShiftRegisters();
					}
					dummyNTFetch();
				}
			} else if(currentScanline == 241){
				if(currentDot == 1) {
					PPU_STATUS |= 0x80; 											// Set Vblank flag
					if((PPU_CTRL & 0x80) > 0) {
						cpu.setNMI();
					}
				}
			} 
		} else if(currentScanline == 261) {
			if(currentDot == 1) {
				PPU_STATUS &= ~0x80;												// Clear vBlank flag
			} else if(currentDot < 257) {
				evaluateBG();
				if(currentDot == 256) {
					incrementVertical();
				}
			} else if(currentDot == 257) {
				v &= ~0x41F;
				v |= t & 0x41F;
				updateShiftRegisters();
			} else if(currentDot >= 280 && currentDot <= 304) {
				equalizeVerticalScroll();
			} else if(currentDot >= 321 && currentDot < 337) {
				evaluateBG();
			} else if(currentDot >= 337 && currentDot <= 340) {
				if(currentDot == 337) {
					updateShiftRegisters();
				}
				dummyNTFetch();
			}
		}
		
	}
	
	private void cycleRenderOff() {
		if(currentScanline == 261 && currentDot == 1) {
			PPU_STATUS &= ~0x80;
		} else if(currentScanline == 241 && currentDot == 1) {
			PPU_STATUS |= 0x80;
			if((PPU_CTRL & 0x80) > 0) {
				cpu.setNMI();
			}
		}
	}
	
	private void dummyNTFetch() {
		// TODO Auto-generated method stub
		
	}

	private void equalizeVerticalScroll() {
		v = t;
	}

	private void evaluateBG() {
		final int adjustedDot = (currentDot - 1) % 8;
		switch(adjustedDot) {
		case 0:															// Assert NT address.
			addressBus.assertAddress(0x2000 | (v & 0xFFF));
			System.out.println("SL: " + currentScanline + " Dot: " + currentDot + " NT: " + Integer.toHexString(v & 0xFFF));
			if(currentDot >= 9 && currentDot != 321) {
				updateShiftRegisters();
			}
			//System.out.println("NT Address Fetch: " + currentDot);
			break;
		case 1:															// Latch NT byte
			ntByte = addressBus.readLatchedData();
			break;
		case 2:															// Assert AT address
			addressBus.assertAddress(0x23C0 | (v & 0xC00) | ((v >> 4) & 0x38) | ((v >> 2) & 0x07));
			break;
		case 3:															// Latch AT byte
			atByte = addressBus.readLatchedData();
			break;
		case 4:															// Assert PT low address
			addressBus.assertAddress( ((PPU_CTRL >> 4) & 1) << 0xC | ntByte << 4 |  (v >> 12) & 7);
			break;
		case 5:															// Latch PT low byte
			ptByteLow = addressBus.readLatchedData();
			break;
		case 6:															// Assert PT high address
			addressBus.assertAddress( ((PPU_CTRL >> 4) & 1) << 0xC | ntByte << 4 | 8 |  (v >> 12) & 7);
			 break;
		case 7:															// Latch PT high byte		
			ptByteHigh = addressBus.readLatchedData();
			incrementHorizontal();
			break;
		}
		
	}

	private void updateShiftRegisters() {
		//System.out.println("Update Shift Registers: " + currentDot);
		bgShiftLow &= ~0xFF00;
		bgShiftLow |= (BitUtils.reverseByte(ptByteLow) << 8);
		bgShiftHigh &= ~0xFF00;
		bgShiftHigh |= (BitUtils.reverseByte(ptByteHigh) << 8);
		int tempAtByte = 0;
		if((v >> 1 & 1) == 0) {
			if((v >> 5 & 1) == 0) {
				tempAtByte = atByte & 3;
			} else {
				tempAtByte = (atByte >> 4) & 3;
			}
		} else {
			if((v >> 5 & 1) == 0) {
				tempAtByte = atByte >> 2 & 3;
			} else {
				tempAtByte = atByte >> 6 & 3;
			}
		}
		bgAtLatchLow = tempAtByte & 1;
		bgAtLatchHigh = tempAtByte >> 1 & 1;
	}
	
	private void shiftBGRegisters() {
		bgShiftLow >>= 1;
		bgShiftHigh >>= 1;
		bgAtShiftLow >>= 1;
		bgAtShiftHigh >>= 1;
		bgAtShiftLow &= ~0x80;
		bgAtShiftLow |= bgAtLatchLow << 7;
		bgAtShiftHigh &= ~0x80;
		bgAtShiftHigh |= bgAtLatchHigh << 7;
		//System.out.println("Shifted at : " + currentDot);
	}
	
	private void renderDot() {
		int bgPixel = (bgShiftLow >> (7 - xScroll)) & 1;
		bgPixel |= (bgShiftHigh >> (7 - xScroll)) & 1 << 1;
		bgPixel |= (bgAtShiftLow >> (xScroll) & 1) << 2;
		bgPixel |= (bgAtShiftHigh >> (xScroll) & 1) << 3;
		frame[(currentScanline * 256) + (currentDot - 1)] = NESPalette.getColor(paletteRam.read(bgPixel));
	}

	private void incrementHorizontal() {
		if((v & 0x1F) == 31) {
			v &= ~0x1F;
			v ^= 0x400;
		} else {
			v++;
		}
	}
	
	private void incrementVertical() {
		if( (v & 0x7000) != 0x7000) {
			v += 0x1000;
		} else {
			v &= ~0x7000;
			int y = (v & 0x3E0) >> 5;
			if(y == 29) {
				y = 0;
				v ^= 0x800;
			} else if(y == 31) {
				y = 0;
			} else {
				y += 1;
			}
			v = (v & ~0x3E0) | (y << 5);
		}
	}

	private void updateScreen() {
		int pixel;
		int row;
		int col;
		int address;
		int lowBg = 0;
		int highBg = 0;
		int ntByte;
		int fineY;
		int x;
		int y;
		int attrX;
		int attrY;
		int curAttr;
		final int length = frame.length;
		for(int i = 0; i < length; i++) {
			x = i % 256;
			y = (i / 256);
			row = y / 8;
			col = x / 8;
			address = (0x2000 | ((PPU_CTRL & 0b11) << 10)) + (row * 32) + col;
			addressBus.assertAddress(address);
			ntByte = addressBus.readLatchedData();
			address = (0x2000 | ((PPU_CTRL & 0b11) << 10)) + 0x3C0 + (((y / 32) * 8) + (x / 32));
			addressBus.assertAddress(address);
			curAttr = addressBus.readLatchedData() & 0xFF;
			row = (ntByte / 16);
			col = ntByte % 16;
			fineY = (y % 8);
			address = ((PPU_CTRL >> 4 & 1) << 0xC) | (row << 8) | (col << 4) | fineY & 7; 
			if(address >= 0) {
				addressBus.assertAddress(address);
				lowBg = addressBus.readLatchedData();
			}
			address = ((PPU_CTRL >> 4 & 1) << 0xC) | (row << 8) | (col << 4) | (1 << 3) | fineY & 7;
			if(address >= 0) {
				addressBus.assertAddress(address);
				highBg = addressBus.readLatchedData();
			}
			int attrStart = (((y / 32) * 32) * 256) + (((x / 32) * 32));
			attrX = (x / 32) * 4;
			attrY = (y / 32) * 4;
			int ntX = x / 8;
			int ntY = y / 8;
			attrStart = i - attrStart;
			int attrBitShift = (((ntX - attrX) / 2) * 2) + (((ntY - attrY) / 2) * 4);
			int palVal = ((curAttr >> attrBitShift) & 3) << 2;
			pixel = ((highBg >> (7 - (i % 8)) & 1) << 1 | (lowBg >> (7 -(i % 8)) & 1));
			address = 0x3F00 + (palVal + pixel);
			addressBus.assertAddress(address);
			frame[i] = NESPalette.getColor(addressBus.readLatchedData());
		}
	}

	public int[] getFrame() {
		//updateScreen();
		cycleCount = 0;
		return frame;
	}

	public void setCPU(CPU cpu) {
		this.cpu = cpu;
	}

	public PPUAddressBus getAddressBus() {
		return addressBus;
	}
	
	public int getByte(int address) {
		if(address >= 0x3F00) {
			return paletteRam.read(address - 0x3F00);
		}
		addressBus.assertAddress(address);
		return addressBus.readLatchedData();
	}
	
	public int getPPUCTRL() {
		return PPU_CTRL;
	}

	public RAM getPaletteRam() {
		return paletteRam;
	}
	
	

}
