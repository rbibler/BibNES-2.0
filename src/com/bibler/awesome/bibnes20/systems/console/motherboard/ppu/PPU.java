package com.bibler.awesome.bibnes20.systems.console.motherboard.ppu;

public class PPU {
	
	private int[] frame;
	int cycleCount;
	
	private int linesPerFrame = 262;
	private int dotsPerLine = 341;
	
	private int currentDot;
	private int currentScanline;
	
	private int PPU_STATUS;
	
	public PPU() {
		frame = new int[256 * 240];
	}
	
	public int read(int registerToRead) {
		System.out.println("Reading PPU register " + registerToRead);
		switch(registerToRead) {
		case 0x02:
			return PPU_STATUS;
		}
		return 0;
	}
	
	public void write(int registerToRead, int dataToWrite) {
		switch(registerToRead) {
		
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

	public int[] getFrame() {
		cycleCount = 0;
		return frame;
	}

}
