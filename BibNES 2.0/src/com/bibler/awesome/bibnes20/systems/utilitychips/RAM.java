package com.bibler.awesome.bibnes20.systems.utilitychips;

public class RAM {
	
	private int size;
	private int[] memoryArray;
	
	
	public RAM(int size) {
		this.size = size;
		memoryArray = new int[size];
	}
	public RAM(int[] memoryArray) {
		this.memoryArray = memoryArray;
		size = memoryArray.length;
	}
	public int read(int address) {
		return memoryArray[address % size];
	}

}
