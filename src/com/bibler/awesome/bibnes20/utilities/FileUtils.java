package com.bibler.awesome.bibnes20.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.bibler.awesome.bibnes20.systems.gamepak.GamePak;
import com.bibler.awesome.bibnes20.systems.utilitychips.ROM;

public class FileUtils {
	
	public static int[] loadBinFromFile(File f) {
		BufferedInputStream stream;
		ArrayList<Integer> fileList = new ArrayList<Integer>();
		try {
			stream = new BufferedInputStream(new FileInputStream(f));
			int read = 0;
			do {
				read = stream.read();
				if(read == -1) {
					break;
				}
				fileList.add(read & 0xFF);
			} while(read >= 0);
		} catch(IOException e) {}
		int[] ret = new int[0x8000];
		int fileSize = fileList.size();
		for(int i = 0; i < ret.length; i++) {
			ret[i] = fileList.get(i % fileSize);
		}
		return ret;
	}
	
	public static int[] loadPrgRomFromFile(File f) {
		BufferedInputStream stream;
		ArrayList<Integer> fileList = new ArrayList<Integer>();
		try {
			stream = new BufferedInputStream(new FileInputStream(f));
			byte[] headerBytes = new byte[16];
			stream.read(headerBytes);
			final int prgSize = headerBytes[4] * 0x4000;
			for(int i = 0; i < prgSize; i++) {
				fileList.add(stream.read());
			}
		} catch(IOException e) {}
		int[] ret = new int[0x8000];
		int fileSize = fileList.size();
		for(int i = 0; i < ret.length; i++) {
			ret[i] = fileList.get(i % fileSize);
		}
		return ret;
	}
	
	public static GamePak loadRomFromFile(File f) {
		GamePak pak = new GamePak();
		BufferedInputStream stream;
		ArrayList<Integer> filebytes = new ArrayList<Integer>();
		try {
			stream = new BufferedInputStream(new FileInputStream(f));
			byte[] headerBytes = new byte[16];
			stream.read(headerBytes);
			final int prgSize = headerBytes[4] * 0x4000;
			final int chrSize = headerBytes[5] * 0x2000;
			final int mirroring = headerBytes[6] & 1;
			int[] prgRom = new int[0x8000];
			final int prgMult = 0x8000 / prgSize;
			for(int i = 0; i < prgSize; i++) {
				final int byteRead = stream.read();
				for(int j = 0; j < prgMult; j++) {
					prgRom[(j * prgSize) + i] = byteRead;
				}
			}
			final int chrMult = 0x4000 / chrSize;
			int[] chrRom = new int[0x4000];
			for(int i = 0; i < chrSize; i++) {
				final int byteRead = stream.read();
				for(int j = 0; j < chrMult; j++) {
					chrRom[(j * chrSize) + i] = byteRead;
				}
			}
			pak.setPrgRom(new ROM(prgRom));
			pak.setChrRom(new ROM(chrRom));
			pak.setMirroring(mirroring);
		} catch(IOException e) {}
		return pak;
	}

}
