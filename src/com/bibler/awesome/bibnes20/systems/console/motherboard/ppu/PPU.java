package com.bibler.awesome.bibnes20.systems.console.motherboard.ppu;

import java.awt.image.BufferedImage;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.AddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.PPUAddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class PPU {
	
	private PPUAddressBus addressBus;
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
	private int PPU_SCROLL;
	private int PPU_ADDR;
	private int PPU_DATA;
	private int OAM_DMA;
	private int xScroll;
	private int yScroll;
	
	private RAM vRam = new RAM(0x2000);
	
	private boolean registerToggle;
	
	public PPU() {
		frame = new int[256 * 240];
	}
	
	public void setAddressBus(PPUAddressBus addressBus) {
		this.addressBus = addressBus;
	}
	
	public int read(int registerToRead) {
		//System.out.println("Reading PPU register " + registerToRead);
		switch(registerToRead) {
		case 0x02:
			PPU_ADDR = 0;
			registerToggle = false;
			final int retValue = PPU_STATUS;
			PPU_STATUS &= ~ 0x80;
			return retValue;
		}
		return 0;
	}
	
	public void write(int registerToWrite, int dataToWrite) {
		switch(registerToWrite) {
		case 0:
			PPU_CTRL = dataToWrite;
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
			if(registerToggle == false) {
				xScroll = dataToWrite;
				registerToggle = true;
			} else {
				yScroll = dataToWrite;
				registerToggle = false;
			}
			System.out.println("XScroll: " + xScroll + " yScroll: " + yScroll);
			PPU_SCROLL = dataToWrite;
			break;
		case 6:
			if(registerToggle == false) {
				PPU_ADDR = (dataToWrite << 8);
				registerToggle = true;
			} else {
				PPU_ADDR |= (dataToWrite & 0xFF);
				registerToggle = false;
			}
			break;
		case 7:
			PPU_DATA = dataToWrite;
			addressBus.latch(dataToWrite);
			addressBus.assertAddressAndWrite(PPU_ADDR);
			PPU_ADDR += ((PPU_CTRL & 0x02) > 0 ? 32 : 1);
			break;
		}
	}
	
	public void cycle() {
		if(currentScanline < 261) {
			if(currentScanline < 240) {
				// visible scanlines
			} else if(currentScanline == 240) {
				// post render
			} else if(currentScanline == 241){
				if(currentDot == 1) {
					PPU_STATUS |= 0x80; 											// Set Vblank flag
					if((PPU_CTRL & 0x80) > 0) {
						cpu.setNMI();
					}
				}
			} else {
				// vblank
			}
		} else if(currentScanline == 261) {
			if(currentDot == 1) {
				PPU_STATUS &= ~0x80;												// Clear vBlank flag
			}
		}
		currentDot++;
		if(currentDot == dotsPerLine) {
			currentScanline++;
			currentDot = 0;
			if(currentScanline == linesPerFrame) {
				currentDot = 0;
				currentScanline = 0;
			}
		}
	}
	
	private void renderFrame() {
		int ntByte;
		int atByte;
		int ptByteLow;
		int ptByteHigh;
		int atXCount = 0;
		int atYCount = 0;
		int currentATCol = 0;
		int currentATRow = 0;
		for(int i = 0; i < 0x3C0; i++) {
			addressBus.assertAddress(0x2000 + i);
			ntByte = addressBus.readLatchedData();
			addressBus.assertAddress(0x23C0 + (currentATCol) + (currentATRow * 8));
			atByte = addressBus.readLatchedData();
			atXCount++;
			if(atXCount == 4) {
				currentATCol++;
				if(currentATCol == 8) {
					currentATCol = 0;
					atXCount = 0;
					atYCount++;
					if(atYCount == 4) {
						atYCount = 0;
						currentATRow++;
					}
				}
			}
			for(int j = 0; j < 8; j++) {
				
				addressBus.assertAddress(0x1000 + (ntByte * 16) + j);
				ptByteLow = addressBus.readLatchedData();
				addressBus.assertAddress(0x1000 + ((ntByte * 16) + j) + 8);
				ptByteHigh = addressBus.readLatchedData();
				renderPatternSlice(ptByteLow, ptByteHigh, atByte, i, j);
			}
		}
	}
	
	private void renderPatternSlice(int ptByteLow, int ptByteHigh, int atByte, int ntIndex, int row) {
		int startX = (ntIndex % 32) * 8;
		int y = ((ntIndex / 32) * 8) + row;
		int x; 
		int color;
		for(int i = 0; i < 8; i++) {
			x = startX + i;
			color = (ptByteLow >> (7 - i)) | (ptByteHigh >> (7 - i)) << 1;
			addressBus.assertAddress(0x3F00 + color);
			color = addressBus.readLatchedData();
			frame[(y * 256) + x] = NESPalette.getColor(color);
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
		//renderFrame();
		updateScreen();
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
		addressBus.assertAddress(address);
		return addressBus.readLatchedData();
	}
	
	public int getPPUCTRL() {
		return PPU_CTRL;
	}
	
	

}
