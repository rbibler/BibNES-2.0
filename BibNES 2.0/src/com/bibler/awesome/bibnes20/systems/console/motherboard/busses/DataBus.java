package com.bibler.awesome.bibnes20.systems.console.motherboard.busses;

public class DataBus {
	
	private int dataLatch;
	
	public void latch(int data) {
		dataLatch = data;
	}
	
	public int read() {
		return dataLatch;
	}
	
}
