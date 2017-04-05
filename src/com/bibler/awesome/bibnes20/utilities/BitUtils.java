package com.bibler.awesome.bibnes20.utilities;

public class BitUtils {
	
	//Index 1==0b0001 => 0b1000
	//Index 7==0b0111 => 0b1110
	//etc
	private static int[] lookup = new int[] {
	0x0, 0x8, 0x4, 0xc, 0x2, 0xa, 0x6, 0xe,
	0x1, 0x9, 0x5, 0xd, 0x3, 0xb, 0x7, 0xf };

	public static int reverseByte(int n) {
	   // Reverse the top and bottom nibble then swap them.
		n = n & 0xFF;
	   return (lookup[n&0b1111] << 4) | lookup[n>>4];
	}

}
