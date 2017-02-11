package com.bibler.awesome.bibnes20.utilities;

public class StringUtilities {
	
	public static String convertIntToFourPaddedHex(int number) {
		String s = "";
		if(number > 0xFFF) {
			s = "";
		} else if(number > 0xFF) {
			s = "0";
		} else if(number > 0xF) {
			s = "00";
		} else {
			s = "000";
		}
		String returnString = s + Integer.toHexString(number).toUpperCase();
		return returnString;
	}
	
	public static String convertIntToTwoPaddedHex(int number) {
		String s = "";
		if(number > 0xF) {
		} else {
			s = "0";
		}
		return (s + Integer.toHexString(number)).toUpperCase();
	}

}
