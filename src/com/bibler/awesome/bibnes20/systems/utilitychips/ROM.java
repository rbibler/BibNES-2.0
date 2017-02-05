package com.bibler.awesome.bibnes20.systems.utilitychips;

public class ROM {
	private int size;
	private int[] memoryArray;
	
	public ROM(int[] memoryArray) {
		this.memoryArray = memoryArray;
		size = memoryArray.length;
	}
	
	public ROM(int size) {
		this.size = size;
		memoryArray = new int[size];
	}
	public int read(int address) {
		return memoryArray[address % size];
	}
}
