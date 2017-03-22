package com.bibler.awesome.bibnes20.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
	
	public static int[] loadRomFromFile(File f) {
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

}
