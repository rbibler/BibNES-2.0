package com.bibler.awesome.bibnes20.systems.console.motherboard.ppu;

public class PPU {
	
	private int[] frame;
	int cycleCount;
	
	public PPU() {
		frame = new int[256 * 240];
	}
	
	public int read(int registerToRead) {
		System.out.println("Reading PPU register " + registerToRead);
		switch(registerToRead) {
		
		}
		return 0x80;
	}
	
	public void write(int registerToRead, int dataToWrite) {
		switch(registerToRead) {
		
		}
	}
	
	public void cycle() {
		frame[cycleCount % frame.length] = ((cycleCount & 0xFF) << 8) | 0xFF << 24 | 0xFF;
		cycleCount++;
	}

	public int[] getFrame() {
		cycleCount = 0;
		return frame;
	}

}
