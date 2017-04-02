package com.bibler.awesome.bibnes20.systems.console.motherboard.busses;

import com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.PPU;
import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;

public class PPUAddressBus {
	
	/*
	 * PPU Memory Map
	 * |  Address range  |	Size  |     Description			|
	 * | 	$0000-$0FFF	 | 	$1000 |	Pattern table 0			|
	 * | 	$1000-$1FFF	 | 	$1000 |	Pattern Table 1			|	
	 * | 	$2000-$23FF	 | 	$0400 |	Nametable 0				|
	 * | 	$2400-$27FF	 | 	$0400 |	Nametable 1				|
	 * | 	$2800-$2BFF	 | 	$0400 |	Nametable 2				|
	 * | 	$2C00-$2FFF	 | 	$0400 |	Nametable 3				|
	 * | 	$3000-$3EFF	 | 	$0F00 |	Mirrors of $2000-$2EFF	|
	 * | 	$3F00-$3F1F	 | 	$0020 |	Palette RAM indexes		|
	 * | 	$3F20-$3FFF	 | 	$00E0 |	Mirrors of $3F00-$3F1F  |

	 */
	
	private RAM ppuRam;
	private GamePak gamePak;
	private PPU ppu;
	private int currentMirroring = MIRROR_V;
	
	public static final int MIRROR_H = 0x00;
	public static final int MIRROR_V = 0x01;
	public static final int MIRROR_1 = 0x02;
	public static final int MIRROR_4 = 0x03;
	
	public static final int PAL_BUS = 0x03;
	public static final int CHR_BUS = 0x04;
	public static final int RAM_BUS = 0x05;
	
	

	private int latchedData;
	private int selector;
	
	public PPUAddressBus(RAM ppuRam, PPU ppu) {
		this.ppuRam = ppuRam;
		this.ppu = ppu;
	}


	public void assertAddress(int address) {
		if(address < 0x2000) {																		// CHR pattern tables
			selector = CHR_BUS;
			latch(gamePak.readChrRom(address));
		} else if(address < 0x3F00) {																// Nametables (PPU RAM)
			selector = RAM_BUS;
			int tempAddress = address;
			if(currentMirroring == MIRROR_H) {
				if((address & 0x800) > 0) {
					tempAddress |= 0x400;
				} else {
					tempAddress &= ~0x400;
				}
			} 
			latch(ppuRam.read(tempAddress - 0x2000));
		} 
	}
	
	public void assertAddressAndWrite(int address) {
		if(address < 0x2000) {																		// CHR pattern tables
			gamePak.writeChrRom(address, latchedData);
		} else if(address < 0x3F00) {																// Nametables (PPU RAM)
			int tempAddress = address;
			if(currentMirroring == MIRROR_H) {
				if((address & 0x800) > 0) {
					tempAddress |= 0x400;
				} else {
					tempAddress &= ~0x400;
				}
			} 
			ppuRam.write(tempAddress - 0x2000, latchedData);
		} 
	}
	
	public void latch(int dataToLatch) {
		latchedData = dataToLatch;
	}
	
	public int readLatchedData() {
		return latchedData;
	}


	public void setGamePak(GamePak gamePak) {
		this.gamePak = gamePak;
		this.currentMirroring = gamePak.getMirroring();
		System.out.println("MIRRORING!: " +  currentMirroring);
	}

}
