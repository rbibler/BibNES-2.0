package com.bibler.awesome.bibnes20.systems.console.motherboard.ppu;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.PPUAddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.utilities.BitUtils;

public class PPU {
	
	private PPUAddressBus addressBus;
	private RAM paletteRam = new RAM(0x20);
	private CPU cpu;
	private int[] frame;
	int cycleCount;
	
	private int linesPerFrame = 262;
	private int dotsPerLine = 341;
	
	private int currentDot;
	private int currentScanline;
	
	private int PPU_CTRL;
	private int PPU_MASK;
	private int PPU_STATUS;
	private int OAM_ADDR;
	private int OAM_DATA;
	private int PPU_DATA;
	private int OAM_DMA;
	private int xScroll;
	private int yScroll;
	
	private int t;
	private int v;
	private int w;
	
	private int ntByte;
	private int atByte;
	private int ptByteLow;
	private int ptByteHigh;
	
	private int bgShiftLow;
	private int bgShiftHigh;
	private int bgAtShiftLow;
	private int bgAtShiftHigh;
	private int bgAtLatchLow;
	private int bgAtLatchHigh;
	
	private int oddEvenLine;
	
	public PPU() {
		frame = new int[256 * 240];
		//currentScanline = 261;
	}
	
	public void reset() {
		t = 0;
		v = 0;
		w = 0;
		xScroll = 0;
		currentScanline = 261;
		currentDot = 0;
		oddEvenLine = 0;
	}
	
	public void setAddressBus(PPUAddressBus addressBus) {
		this.addressBus = addressBus;
	}
	
	public int read(int registerToRead) {
		switch(registerToRead) {
		case 0x02:
			w = 0;
			final int retValue = PPU_STATUS;
			return retValue;
		}
		return 0;
	}
	
	public void write(int registerToWrite, int dataToWrite) {
		switch(registerToWrite) {
		case 0:
			PPU_CTRL = dataToWrite;
			t &= ~0xC;
			t = (dataToWrite & 3) << 2;
			break;
		case 1:
			PPU_MASK = dataToWrite;
			break;
		case 3:
			OAM_ADDR = dataToWrite;
			break;
		case 4:
			OAM_DATA = dataToWrite;
			break;
		case 5:
			if(w == 0) {
				t &= ~0x1F;
				t |= (dataToWrite >> 3) & 0x1F;
				xScroll = dataToWrite & 7;
				w = 1;
			} else {
				t &= ~0x73E0;
				t |= (dataToWrite & 3) << 12;
				t |= ((dataToWrite >> 3) & 0x1F) << 5;
				w = 0;
			}
			break;
		case 6:
			//System.out.println("Address write w " + w + " : " + Integer.toHexString(dataToWrite).toUpperCase());
			if(w == 0) {
				t &= ~0x7F00;
				t |= (dataToWrite & 0x3F) << 8;
				w = 1;
				
			} else {
				t &= ~0xFF;
				t |= (dataToWrite & 0xFF);
				w = 0;
				v = t;
			}
			
			break;
		case 7:
			PPU_DATA = dataToWrite;
			if(v >= 0x3F00) {
				paletteRam.write(v - 0x3F00, dataToWrite);
			} else {
				addressBus.latch(dataToWrite);
				addressBus.assertAddressAndWrite(v);
			}
			final int vInc = (PPU_CTRL >> 2 & 1) == 0 ? 1 : 32;
			if(!renderingAndVisible()) {
				v += vInc;
			} else {
				incrementHorizontal();
				incrementVertical();
			}
			break;
		}
	}
	
	private boolean renderingAndVisible() {
		return renderOn() && (currentScanline <= 240 || currentScanline == 261);
	}
	
	private boolean renderOn() {
		return (PPU_MASK & 0x8) > 0 || (PPU_MASK & 0x10) > 0;
	}
	
	public boolean cycle() {
		if(renderOn()) {
			cycleRenderOn();
		} else {
			cycleRenderOff();
		}
		currentDot++;
		if(currentDot == dotsPerLine) {
			currentScanline++;
			currentDot = 0;
			if(currentScanline == linesPerFrame) {
				currentDot = 0;
				currentScanline = 0;
				return true;
			}
		}
		return false;
	}
	
	private void cycleRenderOn() {
		/*if(currentScanline < 240 && currentDot >= 2) {
			if(currentDot <= 256) {
				renderDot();
			}
		}
		if(currentScanline < 261) {
			if(currentScanline < 240) {
				if(currentDot < 257) {
					evaluateBG();
					if(currentDot == 256) {
						incrementVertical();
					}
				} else if(currentDot == 257) {
					v &= ~0x41F;
					v |= t & 0x41F;
					updateShiftRegisters();
				} else if(currentDot >= 321 && currentDot < 337) {
					evaluateBG();
				} else if(currentDot >= 337 && currentDot <= 340) {
					if(currentDot == 337) {
						updateShiftRegisters();
					}
					dummyNTFetch();
				}
			} else if(currentScanline == 241){
				if(currentDot == 1) {
					PPU_STATUS |= 0x80; 											// Set Vblank flag
					if((PPU_CTRL & 0x80) > 0) {
						cpu.setNMI();
					}
				}
			} 
		} else if(currentScanline == 261) {
			if(currentDot == 1) {
				PPU_STATUS &= ~0x80;												// Clear vBlank flag
			} else if(currentDot < 257) {
				evaluateBG();
				if(currentDot == 256) {
					incrementVertical();
				}
			} else if(currentDot == 257) {
				v &= ~0x41F;
				v |= t & 0x41F;
				updateShiftRegisters();
			} else if(currentDot >= 280 && currentDot <= 304) {
				equalizeVerticalScroll();
			} else if(currentDot >= 321 && currentDot < 337) {
				evaluateBG();
			} else if(currentDot >= 337 && currentDot <= 340) {
				if(currentDot == 337) {
					updateShiftRegisters();
				}
				dummyNTFetch();
			}
		}*/
		ntMemoryAccess();
		shiftRegisterReload();
		hCounterIncrement();
		vCounterUpdate();
		hCounterReload();
		vCounterReload();
		pixelRender();
		shiftRegisters();
		if(currentScanline == 241){
			if(currentDot == 1) {
				PPU_STATUS |= 0x80; 											// Set Vblank flag
				if((PPU_CTRL & 0x80) > 0) {
					cpu.setNMI();
				}
			}
		} else if(currentScanline == 261) {
			if(currentDot == 1) {
				PPU_STATUS &= ~0x80;		
			}
		}
	}
	
	private void pixelRender() {
		if(currentScanline <= 239) {
			if(currentDot >= 1 && currentDot < 257) {
				renderDot();
			}
		}
	}

	private void shiftRegisters() {
		if(currentScanline <= 239) {
			if((currentDot >= 2 && currentDot < 257) || (currentDot >= 321 && currentDot <= 336) ) {
				shiftBGRegisters();
			}
		} else if(currentScanline == 261) {
			if(currentDot >= 321 && currentDot <= 336) {
				shiftBGRegisters();
			}
		}
	}

	private void vCounterReload() {
		if(currentScanline == 261) {
			if(currentDot >= 280 && currentDot <= 304) {
				v = t;
			}
		}
	}

	private void hCounterReload() {
		if(currentScanline <= 239 || currentScanline == 261) {
			if(currentDot == 257) {
				v &= ~0x41F;
				v |= t & 0x41F;
			}
		}
	}

	private void vCounterUpdate() {
		if(currentScanline <= 239 || currentScanline == 261) {
			if(currentDot == 256) {
				incrementVertical();
			}
		}
	}

	private void hCounterIncrement() {
		if(currentScanline <= 239 || currentScanline == 261) {
			switch(currentDot) {
			case 8:
			case 16:
			case 24:
			case 32:
			case 40:
			case 48:
			case 56:
			case 64:
			case 72:
			case 80:
			case 88:
			case 96:
			case 104:
			case 112:
			case 120:
			case 128:
			case 136:
			case 144:
			case 152:
			case 160:
			case 168:
			case 176:
			case 184:
			case 192:
			case 200:
			case 208:
			case 216:
			case 224:
			case 232:
			case 240:
			case 248:
			case 328:
			case 336:
				incrementHorizontal();
				break;
			}
		}
	}

	private void shiftRegisterReload() {
		if(currentScanline <= 239 || currentScanline == 261) {
			switch(currentDot) {
			case 9:
			case 17:
			case 25:
			case 33:
			case 41:
			case 49:
			case 57:
			case 65:
			case 73:
			case 81:
			case 89:
			case 97:
			case 105:
			case 113:
			case 121:
			case 129:
			case 137:
			case 145:
			case 153:
			case 161:
			case 169:
			case 177:
			case 185:
			case 193:
			case 201:
			case 209:
			case 217:
			case 225:
			case 233:
			case 241:
			case 249:
			case 257:
			case 329:
			case 337:
				updateShiftRegisters();
				break;
			}
		}
		
	}

	private void ntMemoryAccess() {
		if(currentScanline <= 239 || currentScanline == 261) {
			switch(currentDot) {
			case 1:
			case 9:
			case 17:
			case 25:
			case 33:
			case 41:
			case 49:
			case 57:
			case 65:
			case 73:
			case 81:
			case 89:
			case 97:
			case 105:
			case 113:
			case 121:
			case 129:
			case 137:
			case 145:
			case 153:
			case 161:
			case 169:
			case 177:
			case 185:
			case 193:
			case 201:
			case 209:
			case 217:
			case 225:
			case 233:
			case 241:
			case 249:
			case 321:
			case 329:
				ntAddressFetch();
				break;
			case 2:
			case 10:
			case 18:
			case 26:
			case 34:
			case 42:
			case 50:
			case 58:
			case 66:
			case 74:
			case 82:
			case 90:
			case 98:
			case 106:
			case 114:
			case 122: 
			case 130:
			case 138:
			case 146:
			case 154:
			case 162:
			case 170:
			case 178:
			case 186:
			case 194:
			case 202:
			case 210:
			case 218:
			case 226:
			case 234:
			case 242:
			case 250:
			case 322:
			case 330:
			case 338:
			case 340:
				ntByteFetch();
				break;
			case 3:
			case 11:
			case 19:
			case 27:
			case 35:
			case 43:
			case 51:
			case 59:
			case 67:
			case 75:
			case 83:
			case 91:
			case 99:
			case 107:
			case 115:
			case 123: 
			case 131:
			case 139:
			case 147:
			case 155:
			case 163:
			case 171:
			case 179:
			case 187:
			case 195:
			case 203:
			case 211:
			case 219:
			case 227:
			case 235:
			case 243:
			case 251:
			case 323:
			case 331:
				atAddressFetch();
				break;
			case 4:
			case 12:
			case 20:
			case 28:
			case 36:
			case 44:
			case 52:
			case 60:
			case 68:
			case 76:
			case 84:
			case 92:
			case 100:
			case 108:
			case 116:
			case 124: 
			case 132:
			case 140:
			case 148:
			case 156:
			case 164:
			case 172:
			case 180:
			case 188:
			case 196:
			case 204:
			case 212:
			case 220:
			case 228:
			case 236:
			case 244:
			case 252:
			case 324:
			case 332:
				atByteFetch();
				break;
			case 5:
			case 13:
			case 21:
			case 29:
			case 37:
			case 45:
			case 53:
			case 61:
			case 69:
			case 77:
			case 85:
			case 93:
			case 101:
			case 109:
			case 117:
			case 125: 
			case 133:
			case 141:
			case 149:
			case 157:
			case 165:
			case 173:
			case 181:
			case 189:
			case 197:
			case 205:
			case 213:
			case 221:
			case 229:
			case 237:
			case 245:
			case 253:
			case 325:
			case 333:
				lowPTAddressFetch();
				break;
			case 6:
			case 14:
			case 22:
			case 30:
			case 38:
			case 46:
			case 54:
			case 62:
			case 70:
			case 78:
			case 86:
			case 94:
			case 102:
			case 110:
			case 118:
			case 126: 
			case 134:
			case 142:
			case 150:
			case 158:
			case 166:
			case 174:
			case 182:
			case 190:
			case 198:
			case 206:
			case 214:
			case 222:
			case 230:
			case 238:
			case 246:
			case 254:
			case 326:
			case 334:
				lowPTByteFetch();
				break;
			case 7:
			case 15:
			case 23:
			case 31:
			case 39:
			case 47:
			case 55:
			case 63:
			case 71:
			case 79:
			case 87:
			case 95:
			case 103:
			case 111:
			case 119:
			case 127: 
			case 135:
			case 143:
			case 151:
			case 159:
			case 167:
			case 175:
			case 183:
			case 191:
			case 199:
			case 207:
			case 215:
			case 223:
			case 231:
			case 239:
			case 247:
			case 255:
			case 327:
			case 335:
				highPTAddressFetch();
				break;
			case 8:
			case 16:
			case 24:
			case 32:
			case 40:
			case 48:
			case 56:
			case 64:
			case 72:
			case 80:
			case 88:
			case 96:
			case 104:
			case 112:
			case 120:
			case 128: 
			case 136:
			case 144:
			case 152:
			case 160:
			case 168:
			case 176:
			case 184:
			case 192:
			case 200:
			case 208:
			case 216:
			case 224:
			case 232:
			case 240:
			case 248:
			case 256:
			case 328:
			case 336:
				highPTByteFetch();
				break;		
			}
		}
	}
	
	private void cycleRenderOff() {
		if(currentScanline == 261 && currentDot == 1) {
			PPU_STATUS &= ~0x80;
		} else if(currentScanline == 241 && currentDot == 1) {
			PPU_STATUS |= 0x80;
			if((PPU_CTRL & 0x80) > 0) {
				cpu.setNMI();
			}
		}
	}
	
	private void dummyNTFetch() {
		// TODO Auto-generated method stub
		
	}

	private void equalizeVerticalScroll() {
		v = t;
	}

	private void evaluateBG() {
		final int adjustedDot = (currentDot - 1) % 8;
		switch(adjustedDot) {
		case 0:															// Assert NT address.
			addressBus.assertAddress(0x2000 | (v & 0xFFF));
			System.out.println("SL: " + currentScanline + " Dot: " + currentDot + " Address: " + Integer.toHexString(v & 0xFFF).toUpperCase());
			if(currentDot >= 9 && currentDot != 321) {
				updateShiftRegisters();
			}
			break;
		case 1:															// Latch NT byte
			ntByte = addressBus.readLatchedData();
			break;
		case 2:															// Assert AT address
			addressBus.assertAddress(0x23C0 | (v & 0xC00) | ((v >> 4) & 0x38) | ((v >> 2) & 0x07));
			break;
		case 3:															// Latch AT byte
			atByte = addressBus.readLatchedData();
			break;
		case 4:															// Assert PT low address
			addressBus.assertAddress( ((PPU_CTRL >> 4) & 1) << 0xC | ntByte << 4 |  (v >> 12) & 7);
			break;
		case 5:															// Latch PT low byte
			ptByteLow = addressBus.readLatchedData();
			break;
		case 6:															// Assert PT high address
			addressBus.assertAddress( ((PPU_CTRL >> 4) & 1) << 0xC | ntByte << 4 | 8 |  (v >> 12) & 7);
			 break;
		case 7:															// Latch PT high byte		
			ptByteHigh = addressBus.readLatchedData();
			incrementHorizontal();
			break;
		}
		if (currentDot >= 321 && currentDot <= 336) {
            shiftBGRegisters();
        }
		
	}
	
	private void ntAddressFetch() {
		addressBus.assertAddress(0x2000 | (v & 0xFFF));
	}
	
	private void ntByteFetch() {
		ntByte = addressBus.readLatchedData();
	}
	
	private void atAddressFetch() {
		addressBus.assertAddress(0x23C0 | (v & 0xC00) | ((v >> 4) & 0x38) | ((v >> 2) & 0x07));
	}
	
	private void atByteFetch() {
		atByte = addressBus.readLatchedData();
	}
	
	private void lowPTAddressFetch() {
		addressBus.assertAddress( ((PPU_CTRL >> 4) & 1) << 0xC | ntByte << 4 |  (v >> 12) & 7);
	}
	
	private void lowPTByteFetch() {
		ptByteLow = addressBus.readLatchedData();
	}
	
	private void highPTAddressFetch() {
		addressBus.assertAddress( ((PPU_CTRL >> 4) & 1) << 0xC | ntByte << 4 | 8 |  (v >> 12) & 7);
	}
	
	private void highPTByteFetch() {
		ptByteHigh = addressBus.readLatchedData();
	}

	private void updateShiftRegisters() {
		bgShiftLow &= ~0xFF00;
		bgShiftLow |= (BitUtils.reverseByte(ptByteLow) << 8);
		bgShiftHigh &= ~0xFF00;
		bgShiftHigh |= (BitUtils.reverseByte(ptByteHigh) << 8);
		int tempAtByte = 0;
		if((v >> 1 & 1) == 0) {
			if((v >> 5 & 1) == 0) {
				tempAtByte = atByte & 3;
			} else {
				tempAtByte = (atByte >> 4) & 3;
			}
		} else {
			if((v >> 5 & 1) == 0) {
				tempAtByte = atByte >> 2 & 3;
			} else {
				tempAtByte = atByte >> 6 & 3;
			}
		}
		bgAtLatchLow = tempAtByte & 1;
		bgAtLatchHigh = tempAtByte >> 1 & 1;
	}
	
	private void shiftBGRegisters() {
		bgShiftLow >>= 1;
		bgShiftHigh >>= 1;
		bgAtShiftLow >>= 1;
		bgAtShiftHigh >>= 1;
		bgAtShiftLow &= ~0x80;
		bgAtShiftLow |= bgAtLatchLow << 7;
		bgAtShiftHigh &= ~0x80;
		bgAtShiftHigh |= bgAtLatchHigh << 7;
	}
	
	private void renderDot() {
		int bgPixel = (bgShiftLow >> (7 - xScroll)) & 1;
		bgPixel |= (bgShiftHigh >> (7 - xScroll)) & 1 << 1;
		bgPixel |= (bgAtShiftLow >> (xScroll) & 1) << 2;
		bgPixel |= (bgAtShiftHigh >> (xScroll) & 1) << 3;
		frame[(currentScanline * 256) + (currentDot - 1)] = NESPalette.getColor(paletteRam.read(bgPixel));
		//shiftBGRegisters();
	}

	private void incrementHorizontal() {
		if((v & 0x1F) == 31) {
			v &= ~0x1F;
			v ^= 0x400;
		} else {
			v++;
		}
	}
	
	private void incrementVertical() {
		if( (v & 0x7000) != 0x7000) {
			v += 0x1000;
		} else {
			v &= ~0x7000;
			int y = (v & 0x3E0) >> 5;
			if(y == 29) {
				y = 0;
				v ^= 0x800;
			} else if(y == 31) {
				y = 0;
			} else {
				y += 1;
			}
			v = (v & ~0x3E0) | (y << 5);
		}
	}

	private void updateScreen() {
		int pixel;
		int row;
		int col;
		int address;
		int lowBg = 0;
		int highBg = 0;
		int ntByte;
		int fineY;
		int x;
		int y;
		int attrX;
		int attrY;
		int curAttr;
		final int length = frame.length;
		for(int i = 0; i < length; i++) {
			x = i % 256;
			y = (i / 256);
			row = y / 8;
			col = x / 8;
			address = (0x2000 | ((PPU_CTRL & 0b11) << 10)) + (row * 32) + col;
			addressBus.assertAddress(address);
			ntByte = addressBus.readLatchedData();
			address = (0x2000 | ((PPU_CTRL & 0b11) << 10)) + 0x3C0 + (((y / 32) * 8) + (x / 32));
			addressBus.assertAddress(address);
			curAttr = addressBus.readLatchedData() & 0xFF;
			row = (ntByte / 16);
			col = ntByte % 16;
			fineY = (y % 8);
			address = ((PPU_CTRL >> 4 & 1) << 0xC) | (row << 8) | (col << 4) | fineY & 7; 
			if(address >= 0) {
				addressBus.assertAddress(address);
				lowBg = addressBus.readLatchedData();
			}
			address = ((PPU_CTRL >> 4 & 1) << 0xC) | (row << 8) | (col << 4) | (1 << 3) | fineY & 7;
			if(address >= 0) {
				addressBus.assertAddress(address);
				highBg = addressBus.readLatchedData();
			}
			int attrStart = (((y / 32) * 32) * 256) + (((x / 32) * 32));
			attrX = (x / 32) * 4;
			attrY = (y / 32) * 4;
			int ntX = x / 8;
			int ntY = y / 8;
			attrStart = i - attrStart;
			int attrBitShift = (((ntX - attrX) / 2) * 2) + (((ntY - attrY) / 2) * 4);
			int palVal = ((curAttr >> attrBitShift) & 3) << 2;
			pixel = ((highBg >> (7 - (i % 8)) & 1) << 1 | (lowBg >> (7 -(i % 8)) & 1));
			address = 0x3F00 + (palVal + pixel);
			addressBus.assertAddress(address);
			frame[i] = NESPalette.getColor(addressBus.readLatchedData());
		}
	}

	public int[] getFrame() {
		cycleCount = 0;
		return frame;
	}

	public void setCPU(CPU cpu) {
		this.cpu = cpu;
	}

	public PPUAddressBus getAddressBus() {
		return addressBus;
	}
	
	public int getByte(int address) {
		if(address >= 0x3F00) {
			return paletteRam.read(address - 0x3F00);
		}
		addressBus.assertAddress(address);
		return addressBus.readLatchedData();
	}
	
	public int getPPUCTRL() {
		return PPU_CTRL;
	}

	public RAM getPaletteRam() {
		return paletteRam;
	}
	
	

}
