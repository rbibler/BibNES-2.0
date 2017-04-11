package com.bibler.awesome.bibnes20.systems.console.motherboard.ppu;


import static java.awt.image.BufferedImage.TYPE_INT_BGR;
import static java.util.Arrays.fill;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.bibler.awesome.bibnes20.systems.console.motherboard.busses.PPUAddressBus;
import com.bibler.awesome.bibnes20.systems.console.motherboard.cpu.CPU;
import com.bibler.awesome.bibnes20.systems.utilitychips.RAM;
import com.bibler.awesome.bibnes20.utilities.BitUtils;
import com.bibler.awesome.bibnes20.utilities.StringUtilities;
import static com.bibler.awesome.bibnes20.systems.console.motherboard.ppu.utils.reverseByte;


public class PPU {
	
	private PPUAddressBus addressBus;
    private int oamaddr, oamstart, readbuffer = 0;
    private int loopyV = 0x0;//ppu memory pointer
    private int loopyT = 0x0;//temp pointer
    private int loopyX = 0;//fine x scroll
    public int scanline = 0;
    public int cycles = 0;
    private int framecount = 0;
    private int div = 2;
    private final int[] OAM = new int[256], secOAM = new int[32],
            spriteshiftregH = new int[8],
            spriteshiftregL = new int[8], spriteXlatch = new int[8],
            spritepals = new int[8], bitmap = new int[240 * 256];
    private int found, bgShiftRegH, bgShiftRegL, bgAttrShiftRegH, bgAttrShiftRegL;
    private final boolean[] spritebgflags = new boolean[8];
    private boolean even = true, bgpattern = true, sprpattern, spritesize, nmicontrol,
            grayscale, bgClip, spriteClip, bgOn, spritesOn,
            vblankflag, sprite0hit, spriteoverflow;
    private int emph;
    public final int[] pal;
    private int vraminc = 1;
    private BufferedImage nametableView;
    private final int[] bgcolors = new int[256];
    private int openbus = 0; //the last value written to the PPU
    private int nextattr;
    private int linelowbits;
    private int linehighbits;
    private int penultimateattr;
    private int numscanlines;
    private int vblankline;
    private int[] cpudivider;
    
    private int currentXScroll;

    public PPU() {
        this.pal = new int[]{0x09, 0x01, 0x00, 0x01, 0x00, 0x02, 0x02, 0x0D,
            0x08, 0x10, 0x08, 0x24, 0x00, 0x00, 0x04, 0x2C, 0x09, 0x01, 0x34,
            0x03, 0x00, 0x04, 0x00, 0x14, 0x08, 0x3A, 0x00, 0x02, 0x00, 0x20,
            0x2C, 0x08};
        /*
     power-up pallette checked by Blargg's power_up_palette test. Different
     revs of NES PPU might give different initial results but there's a test
     expecting this set of values and nesemu1, BizHawk, RockNES, MyNes use it
         */
        fill(OAM, 0xff);
        setParameters();
    }

    final void setParameters() {
        //set stuff to NTSC or PAL or Dendy values
       numscanlines = 262;
       vblankline = 241;      
    }

    public void runFrame() {
        for (int line = 0; line < numscanlines; ++line) {
            clockLine(line);
        }
    }

    /**
     * Performs a read from a PPU register, as well as causes any side effects
     * of reading that specific register.
     *
     * @param regnum register to read (address with 0x2000 already subtracted)
     * @return the data in the PPU register, or open bus (the last value written
     * to a PPU register) if the register is read only
     */
    public final int read(final int regnum) {
        switch (regnum) {
            case 2:
                even = true;
                if (scanline == 241) {
                    if (cycles == 1) {//suppress NMI flag if it was just turned on this same cycle
                        vblankflag = false;
                    }
                    //OK, uncommenting this makes blargg's NMI suppression test
                    //work but breaks Antarctic Adventure.
                    //I'm going to need a cycle accurate CPU to fix that...
//                    if (cycles < 4) {
//                        //show vblank flag but cancel pending NMI before the CPU
//                        //can actually do anything with it
//                        //TODO: use proper interface for this
//                        mapper.cpu.nmiNext = false;
//                    }
                }
                openbus = (vblankflag ? 0x80 : 0)
                        | (sprite0hit ? 0x40 : 0)
                        | (spriteoverflow ? 0x20 : 0)
                        | (openbus & 0x1f);
                vblankflag = false;
                break;
            case 4:
                // reading this is NOT reliable but some games do it anyways
                openbus = OAM[oamaddr];
                //System.err.println("codemasters?");
                if (renderingOn() && (scanline <= 240)) {
                    if (cycles < 64) {
                        return 0xFF;
                    } else if (cycles <= 256) {
                        return 0x00;
                    } //Micro Machines relies on this:
                    else if (cycles < 320) {
                        return 0xFF;
                    } //and this:
                    else {
                        return secOAM[0]; //is this the right value @ the time?
                    }
                }
                break;
            case 7:
                // PPUDATA
                // correct behavior. read is delayed by one
                // -unless- is a read from sprite pallettes
                final int temp;
                if ((loopyV & 0x3fff) < 0x3f00) {
                    temp = readbuffer;
                    addressBus.assertAddress(loopyV & 0x3fff);
                    readbuffer = addressBus.readLatchedData();
                } else {
                	addressBus.assertAddress((loopyV & 0x3fff) - 0x1000);
                	readbuffer = addressBus.readLatchedData();
                	addressBus.assertAddress(loopyV);
                    temp = addressBus.readLatchedData();
                }
                if (!renderingOn() || (scanline > 240 && scanline < (numscanlines - 1))) {
                    loopyV += vraminc;
                } else {
                    //if 2007 is read during rendering PPU increments both horiz
                    //and vert counters erroneously.
                    incLoopyVHoriz();
                    incLoopyVVert();
                }
                openbus = temp;
                break;

            // and don't increment on read
            default:
                return openbus; // last value written to ppu
        }
        return openbus;
    }

    /**
     * Performs a write to a PPU register
     *
     * @param regnum register number from 0 to 7, memory addresses are decoded
     * to these elsewhere
     * @param data the value to write to the register (0x00 to 0xff valid)
     */
    public final void write(final int regnum, final int data) {
//        if (regnum != 4 /*&& regnum != 7*/) {
//            System.err.println("PPU write - wrote " + utils.hex(data) + " to reg "
//                    + utils.hex(regnum + 0x2000)
//                    + " frame " + framecount + " scanline " + scanline);
//        }
        //debugdraw();
        openbus = data;
        switch (regnum) {
            case 0: //PPUCONTROL (2000)
                //set 2 bits of vram address (nametable select)
                //bits 0 and 1 affect loopyT to change nametable start by 0x400
                loopyT &= ~0xc00;
                loopyT |= (data & 3) << 10;
                /*
                 SMB1 writes here at the end of its main loop and if this write
                 lands on one exact PPU clock, the address bits are set to 0.
                 This only happens on one CPU/PPU alignment of real hardware 
                 though so it only shows up ~33% of the time.
                 */
                vraminc = (((data & (utils.BIT2)) != 0) ? 32 : 1);
                sprpattern = ((data & (utils.BIT3)) != 0);
                bgpattern = ((data & (utils.BIT4)) != 0);
                spritesize = ((data & (utils.BIT5)) != 0);
                /*bit 6 is kind of a halt and catch fire situation since it outputs
                 ppu color data on the EXT pins that are tied to ground if set
                 and that'll make the PPU get very hot from sourcing the current. 
                 Only really useful for the NESRGB interposer board, kind of
                 useless for emulators. I will ignore it.
                 */
                nmicontrol = ((data & (utils.BIT7)) != 0);

                break;
            case 1: //PPUMASK (2001)
                grayscale = ((data & (utils.BIT0)) != 0);
                bgClip = !((data & (utils.BIT1)) != 0); //clip left 8 pixels when its on
                spriteClip = !((data & (utils.BIT2)) != 0);
                bgOn = ((data & (utils.BIT3)) != 0);
                spritesOn = ((data & (utils.BIT4)) != 0);
                emph = (data & 0xe0) << 1;
                if (numscanlines == 312) {
                    //if PAL switch position of red and green emphasis bits (6 and 5)
                    //red is bit 6 -> bit 7
                    //green is bit 7 -> bit 6
                    int red = (emph >> 6) & 1;
                    int green = (emph >> 7) & 1;
                    emph &= 0xf3f;
                    emph |= (red << 7) | (green << 6);
                }
                break;
            case 3:
                // PPUOAMADDR (2003)
                // most games just write zero and use the dma
                oamaddr = data & 0xff;
                break;
            case 4:
                // PPUOAMDATA(2004)
                if ((oamaddr & 3) == 2) {
                    OAM[oamaddr++] = (data & 0xE3);
                } else {
                    OAM[oamaddr++] = data;
                }
                oamaddr &= 0xff;
                // games don't usually write this directly anyway, it's unreliable
                break;

            // PPUSCROLL(2005)
            case 5:
                if (even) {
                    // update horizontal scroll
                	//System.out.println("Set scroll to: " + (data >> 3) + " at " + scanline);
                    loopyT &= ~0x1f;
                    loopyX = data & 7;
                    loopyT |= data >> 3;
                    currentXScroll = data >> 3;

                    even = false;
                } else {
                    // update vertical scroll
                    loopyT &= ~0x7000;
                    loopyT |= ((data & 7) << 12);
                    loopyT &= ~0x3e0;
                    loopyT |= (data & 0xf8) << 2;
                    even = true;

                }
                break;

            case 6:
                // PPUADDR (2006)
                if (even) {
                    // high byte
                    loopyT &= 0xc0ff;
                    loopyT |= ((data & 0x3f) << 8);
                    loopyT &= 0x3fff;
                    even = false;
                } else {
                    loopyT &= 0xfff00;
                    loopyT |= data;
                    loopyV = loopyT;
                    even = true;
                }
                break;
            case 7:
                // PPUDATA  
            	addressBus.latch(data);
            	addressBus.assertAddressAndWrite((loopyV & 0x3fff));
                if (!renderingOn() || (scanline > 240 && scanline < (numscanlines - 1))) {
                    loopyV += vraminc;
                } else if ((loopyV & 0x7000) == 0x7000) {
                    int YScroll = loopyV & 0x3E0;
                    loopyV &= 0xFFF;
                    switch (YScroll) {
                        case 0x3A0:
                            loopyV ^= 0xBA0;
                            break;
                        case 0x3E0:
                            loopyV ^= 0x3E0;
                            break;
                        default:
                            loopyV += 0x20;
                            break;
                    }
                } else {
                    // while rendering, it seems to drop by 1 line, regardless of increment mode
                    loopyV += 0x1000;
                }
                break;
            default:
                break;
        }
    }

    /**
     * PPU is on if either background or sprites are enabled
     *
     * @return true
     */
    public boolean renderingOn() {
        return bgOn || spritesOn;
    }

    /**
     * MMC3 scan line counter isn't clocked if background and sprites are using
     * the same half of the pattern table
     *
     * @return true if PPU is rendering and BG and sprites are using different
     * pattern tables
     */
    public final boolean mmc3CounterClocking() {
        return (bgpattern != sprpattern) && renderingOn();
    }

    /**
     * Runs the PPU emulation for one NES scan line.
     */
    public final void clockLine(int scanline) {
        //skip a PPU clock on line 0 of odd frames when rendering is on
        //and we are in NTSC mode (pal has no skip)
        int skip = (numscanlines == 262
                && scanline == 0
                && renderingOn()
                && !((framecount & (utils.BIT1)) != 0)) ? 1 : 0;
        for (cycles = skip; cycles < 341; ++cycles) {
            clock();
        }
    }

    private int tileAddr = 0;
    private int cpudividerctr = 0;

    /**
     * runs the emulation for one PPU clock cycle.
     */
    public final void clock() {

        //cycle based ppu stuff will go here
        if (cycles == 1) {
            if (scanline == 0) {
                dotcrawl = renderingOn();
            }
            if (scanline < 240) {
                bgcolors[scanline] = pal[0];
            }
        }
        if (scanline < 240 || scanline == (numscanlines - 1)) {
            //on all rendering lines
            if (renderingOn()
                    && ((cycles >= 1 && cycles <= 256)
                    || (cycles >= 321 && cycles <= 336))) {
                //fetch background tiles, load shift registers
                bgFetch();
            } else if (cycles == 257 && renderingOn()) {
                //x scroll reset
                //horizontal bits of loopyV = loopyT
                loopyV &= ~0x41f;
                loopyV |= loopyT & 0x41f;

            } else if (cycles > 257 && cycles <= 341) {
                //clear the oam address from pxls 257-341 continuously
                oamaddr = 0;
            }
            if ((cycles == 340) && renderingOn()) {
                //read the same nametable byte twice
                //this signals the MMC5 to increment the scanline counter
                fetchNTByte();
                fetchNTByte();
            }
            if (cycles == 65 && renderingOn()) {
                oamstart = oamaddr;
            }
            if (cycles == 260 && renderingOn()) {
                //evaluate sprites for NEXT scanline (as long as either background or sprites are enabled)
                //this does in fact happen on scanline 261 but it doesn't do anything useful
                //it's cycle 260 because that's when the first important sprite byte is read
                //actually sprite overflow should be set by sprite eval somewhat before
                //so this needs to be split into 2 parts, the eval and the data fetches
                evalSprites();
            }
            if (scanline == (numscanlines - 1)) {
                if (cycles == 0) {// turn off vblank, sprite 0, sprite overflow flags
                    vblankflag = false;
                    sprite0hit = false;
                    spriteoverflow = false;
                } else if (cycles >= 280 && cycles <= 304 && renderingOn()) {
                    //loopyV = (all of)loopyT for each of these cycles
                    loopyV = loopyT;
                }
            }
        } else if (scanline == vblankline && cycles == 1) {
            //handle vblank on / off
            vblankflag = true;
        }
        if (!renderingOn() || (scanline > 240 && scanline < (numscanlines - 1))) {
            //HACK ALERT
            //handle the case of MMC3 mapper watching A12 toggle
            //even when read or write aren't asserted on the bus
            //needed to pass Blargg's mmc3 tests
          //  mapper.checkA12(loopyV & 0x3fff);
        }
        if (scanline < 240 && cycles >= 1 && cycles <= 256) {
            int bufferoffset = (scanline << 8) + (cycles - 1);
            //bg drawing
            if (bgOn) { //if background is on, draw a line of that
                final boolean isBG = drawBGPixel(bufferoffset);
                //sprite drawing
                drawSprites(scanline << 8, cycles - 1, isBG);

            } else if (spritesOn) {
                //just the sprites then
                int bgcolor = ((loopyV > 0x3f00 && loopyV < 0x3fff) ? mapper.ppuRead(loopyV) : pal[0]);
                bitmap[bufferoffset] = bgcolor;
                drawSprites(scanline << 8, cycles - 1, true);
            } else {
                //rendering is off, so draw either the background color OR
                //if the PPU address points to the palette, draw that color instead.
                int bgcolor = ((loopyV > 0x3f00 && loopyV < 0x3fff) ? mapper.ppuRead(loopyV) : pal[0]);
                bitmap[bufferoffset] = bgcolor;
            }
            //deal with the grayscale flag
            if (grayscale) {
                bitmap[bufferoffset] &= 0x30;
            }
            //handle color emphasis
            bitmap[bufferoffset] = (bitmap[bufferoffset] & 0x3f) | emph;

        }
        //handle nmi
        if (vblankflag && nmicontrol) {
            //pull NMI line on when conditions are right
            cpu.setNMI(true);
        } else {
            cpu.setNMI(false);
        }

        //clock CPU, once every 3 ppu cycles
        div = (div + 1) % cpudivider[cpudividerctr];
        if (div == 0) {
            mapper.cpu.runcycle(scanline, cycles);
            mapper.cpucycle(1);
            cpudividerctr = (cpudividerctr + 1) % cpudivider.length;
        }
        if (cycles == 257) {
            mapper.notifyscanline(scanline);
        } else if (cycles == 340) {
            scanline = (scanline + 1) % numscanlines;
            if (scanline == 0) {
                ++framecount;
            }
        }
    }

    private void bgFetch() {
        //fetch tiles for background
        //on real PPU this logic is repurposed for sprite fetches as well
        //System.err.println(hex(loopyV));
        bgAttrShiftRegH |= ((nextattr >> 1) & 1);
        bgAttrShiftRegL |= (nextattr & 1);
        //background fetches
        switch ((cycles - 1) & 7) {
            case 1:
                fetchNTByte();
                break;
            case 3:
                //fetch attribute (FIX MATH)
                penultimateattr = getAttribute(((loopyV & 0xc00) + 0x23c0),
                        (loopyV) & 0x1f,
                        (((loopyV) & 0x3e0) >> 5));
                break;
            case 5:
                //fetch low bg byte
            	addressBus.assertAddress((tileAddr)
                        + ((loopyV & 0x7000) >> 12));
                linelowbits = addressBus.readLatchedData();
                break;
            case 7:
                //fetch high bg byte
            	addressBus.assertAddress((tileAddr) + 8
                        + ((loopyV & 0x7000) >> 12));
                linehighbits = addressBus.readLatchedData();
                bgShiftRegL |= linelowbits;
                bgShiftRegH |= linehighbits;
                nextattr = penultimateattr;
                if (cycles != 256) {
                    incLoopyVHoriz();
                } else {
                    incLoopyVVert();
                }
                break;
            default:
                break;
        }
        if (cycles >= 321 && cycles <= 336) {
            bgShiftClock();
        }
    }

    private void incLoopyVVert() {
        //increment loopy_v to next row of tiles
        if ((loopyV & 0x7000) == 0x7000) {
            //reset the fine scroll bits and increment tile address to next row
            loopyV &= ~0x7000;
            int y = (loopyV & 0x03E0) >> 5;
            if (y == 29) {
                //if row is 29 zero fine scroll and bump to next nametable
                y = 0;
                loopyV ^= 0x0800;
            } else {
                //increment (wrap to 5 bits) but if row is already over 29
                //we don't bump loopyV to next nt.
                y = (y + 1) & 31;
            }
            loopyV = (loopyV & ~0x03E0) | (y << 5);
        } else {
            //increment the fine scroll
            loopyV += 0x1000;
        }
    }

    private void incLoopyVHoriz() {
        //increment horizontal part of loopyv
        if ((loopyV & 0x001F) == 31) // if coarse X == 31
        {
            loopyV &= ~0x001F; // coarse X = 0
            loopyV ^= 0x0400;// switch horizontal nametable
        } else {
            loopyV += 1;// increment coarse X
        }
    }

    private void fetchNTByte() {
        //fetch nt byte
    	addressBus.assertAddress(((loopyV & 0xc00) | 0x2000) + (loopyV & 0x3ff));
        tileAddr = addressBus.readLatchedData() * 16 + (bgpattern ? 0x1000 : 0);
        //System.out.println("SL: " + scanline + " Dot: " + cycles + " Address: " + Integer.toHexString(loopyV & 0xFFF).toUpperCase());
    }

    private boolean drawBGPixel(int bufferoffset) {
        //background drawing
        //loopyX picks bits
        final boolean isBG;
        if (bgClip && (bufferoffset & 0xff) < 8) {
            //left hand of screen clipping
            //(needs to be marked as BG and not cause a sprite hit)
            bitmap[bufferoffset] = pal[0];
            isBG = true;
        } else {
            final int bgPix = (((bgShiftRegH >> -loopyX + 16) & 1) << 1)
                    + ((bgShiftRegL >> -loopyX + 16) & 1);
            final int bgPal = (((bgAttrShiftRegH >> -loopyX + 8) & 1) << 1)
                    + ((bgAttrShiftRegL >> -loopyX + 8) & 1);
            isBG = (bgPix == 0);
            bitmap[bufferoffset] = isBG ? pal[0] : pal[(bgPal << 2) + bgPix];
        }
        if(cycles == (currentXScroll * 8) + loopyX) {
        	bitmap[bufferoffset] = 0x22;
        }
        bgShiftClock();
        return isBG;
    }

    private void bgShiftClock() {
    	System.out.println("Shifting at: " + cycles);
        bgShiftRegH <<= 1;
        bgShiftRegL <<= 1;
        bgAttrShiftRegH <<= 1;
        bgAttrShiftRegL <<= 1;
    }

    boolean dotcrawl = true;
    private boolean sprite0here = false;

    /**
     * evaluates PPU sprites for the NEXT scanline
     */
    private void evalSprites() {
        sprite0here = false;
        int ypos, offset;
        found = 0;
        Arrays.fill(secOAM, 0xff);
        //primary evaluation
        //need to emulate behavior when OAM address is set to nonzero here
        for (int spritestart = oamstart; spritestart < 255; spritestart += 4) {
            //for each sprite, first we cull the non-visible ones
            ypos = OAM[spritestart];
            offset = scanline - ypos;
            if (ypos > scanline || offset > (spritesize ? 15 : 7)) {
                //sprite is out of range vertically
                continue;
            }
            //if we're here it's a valid renderable sprite
            if (spritestart == 0) {
                sprite0here = true;
            }
            //actually which sprite is flagged for sprite 0 depends on the starting
            //oam address which is, on the real thing, not necessarily zero.
            if (found >= 8) {
                //if more than 8 sprites, set overflow bit and STOP looking
                //todo: add "no sprite limit" option back
                spriteoverflow = true;
                break; //also the real PPU does strange stuff on sprite overflow
                //todo: emulate register trashing that happens when overflow
            } else {
                //set up ye sprite for rendering
                secOAM[found * 4] = OAM[spritestart];
//                secOAM[found * 4 + 1] = OAM[spritestart + 1];
//                secOAM[found * 4 + 2] = OAM[spritestart + 2];
//                secOAM[found * 4 + 3] = OAM[spritestart + 3];
                final int oamextra = OAM[spritestart + 2];

                //bg flag
                spritebgflags[found] = ((oamextra & (utils.BIT5)) != 0);
                //x value
                spriteXlatch[found] = OAM[spritestart + 3];
                spritepals[found] = ((oamextra & 3) + 4) * 4;
                if (((oamextra & (utils.BIT7)) != 0)) {
                    //if sprite is flipped vertically, reverse the offset
                    offset = (spritesize ? 15 : 7) - offset;
                }
                //now correction for the fact that 8x16 tiles are 2 separate tiles
                if (offset > 7) {
                    offset += 8;
                }
                //get tile address (8x16 sprites can use both pattern tbl pages but only the even tiles)
                final int tilenum = OAM[spritestart + 1];
                spriteFetch(spritesize, tilenum, offset, oamextra);
                ++found;
            }
        }
        for (int i = found; i < 8; ++i) {
            //fill unused sprite registers with zeros
            spriteshiftregL[found] = 0;
            spriteshiftregH[found] = 0;
            //also, we need to do 8 reads no matter how many sprites we found
            //dummy reads are to sprite 0xff
            spriteFetch(spritesize, 0xff, 0, 0);
        }
    }

    private void spriteFetch(final boolean spritesize, final int tilenum, int offset, final int oamextra) {
        int tilefetched;
        if (spritesize) {
            tilefetched = ((tilenum & 1) * 0x1000)
                    + (tilenum & 0xfe) * 16;
        } else {
            tilefetched = tilenum * 16
                    + ((sprpattern) ? 0x1000 : 0);
        }
        tilefetched += offset;
        //now load up the shift registers for said sprite
        final boolean hflip = ((oamextra & (utils.BIT6)) != 0);
        int tile;
        if (!hflip) {
        	addressBus.assertAddress(tilefetched);
            spriteshiftregL[found] = reverseByte(addressBus.readLatchedData());
            addressBus.assertAddress(tilefetched + 8);
            spriteshiftregH[found] = reverseByte(addressBus.readLatchedData());
        } else {
        	addressBus.assertAddress(tilefetched);
            spriteshiftregL[found] = addressBus.readLatchedData();
            addressBus.assertAddress(tilefetched + 8);
            spriteshiftregH[found] = addressBus.readLatchedData();
        }
    }

    /**
     * draws appropriate pixel of the sprites selected by sprite evaluation
     */
    private void drawSprites(int bufferoffset, int x, boolean bgflag) {
        final int startdraw = !spriteClip ? 0 : 8;//sprite left 8 pixels clip
        int sprpxl = 0;
        int index = 7;
        //per pixel in de line that could have a sprite
        for (int y = found - 1; y >= 0; --y) {
            int off = x - spriteXlatch[y];
            if (off >= 0 && off <= 8) {
                if ((spriteshiftregH[y] & 1) + (spriteshiftregL[y] & 1) != 0) {
                    index = y;
                    sprpxl = 2 * (spriteshiftregH[y] & 1) + (spriteshiftregL[y] & 1);
                }
                spriteshiftregH[y] >>= 1;
                spriteshiftregL[y] >>= 1;
            }
        }
        if (sprpxl == 0 || x < startdraw || !spritesOn) {
            //no opaque sprite pixel here
            return;
        }

        if (sprite0here && (index == 0) && !bgflag
                && x < 255) {
            //sprite 0 hit!
            sprite0hit = true;
        }
        //now, FINALLY, drawing.
        if (!spritebgflags[index] || bgflag) {
            bitmap[bufferoffset + x] = pal[spritepals[index] + sprpxl];
        }
    }

    /**
     * Read the appropriate color attribute byte for the current tile. this is
     * fetched 2x as often as it really needs to be, the MMC5 takes advantage of
     * that for ExGrafix mode.
     *
     * @param ntstart //start of the current attribute table
     * @param tileX //x position of tile (0-31)
     * @param tileY //y position of tile (0-29)
     * @return attribute table value (0-3)
     */
    private int getAttribute(final int ntstart, final int tileX, final int tileY) {
    	addressBus.assertAddress(ntstart + (tileX >> 2) + 8 * (tileY >> 2));
        final int base = addressBus.readLatchedData();
        if (((tileY & (utils.BIT1)) != 0)) {
            if (((tileX & (utils.BIT1)) != 0)) {
                return (base >> 6) & 3;
            } else {
                return (base >> 4) & 3;
            }
        } else if (((tileX & (utils.BIT1)) != 0)) {
            return (base >> 2) & 3;
        } else {
            return base & 3;
        }
    }



    /**
     * Sends off a frame of NES video to be rendered by the GUI. also includes
     * dot crawl flag and BG color to be displayed around edges which are needed
     * for the NTSC renderer.
     *
     * @param gui the GUI window to render to
     */
    public final int[] getFrame() {
       return bitmap;

    }
	
	/*private PPUAddressBus addressBus;
	private RAM paletteRam = new RAM(0x20);
	private CPU cpu;
	private int[] frame = new int[256 * 240];
	private int[] atBytes = new int[256 * 240];
	private int[] ntBytes = new int[256 * 240];
	private int[] ptLowBytes = new int[256 * 240];
	private int[] ptHighBytes = new int[256 * 240];
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
			if((PPU_MASK & 0x8) > 0) {
				System.out.println("BG RENDERING ON");
			} else if((PPU_MASK & 0x10) > 0) {
				System.out.println("SPRITE RENDERING ON");
			}
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
		if(currentDot == 0) {
			if(currentScanline == 0) {
				for(int i = 0; i < frame.length; i++) {
					frame[i] = 0;
				}
			}
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
					//updateShiftRegisters();
				} else if(currentDot >= 321 && currentDot < 337) {
					evaluateBG();
				} else if(currentDot >= 337 && currentDot <= 340) {
					if(currentDot == 337) {
						//updateShiftRegisters();
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
				//updateShiftRegisters();
			} else if(currentDot >= 280 && currentDot <= 304) {
				equalizeVerticalScroll();
			} else if(currentDot >= 321 && currentDot < 337) {
				evaluateBG();
			} else if(currentDot >= 337 && currentDot <= 340) {
				if(currentDot == 337) {
					//updateShiftRegisters();
				}
				dummyNTFetch();
			}
		}*/
		/*ntMemoryAccess();
		
		hCounterIncrement();
		vCounterUpdate();
		hCounterReload();
		vCounterReload();
		shiftRegisterReload();
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
			if((currentDot >= 1 && currentDot < 257) || (currentDot >= 321 && currentDot <= 336) ) {
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
		int tempv = v - 1;
		final int row = ((((tempv & 0x3E0) >> 5)) % 4) / 2;
		final int col = (((tempv & 0x1F) % 4) / 2);
		int attrByte = 0;
		if(row == 0) {
			if(col == 0) {		
				//Top left
				attrByte = atByte & 3;
			} else {
				//Top Right
				attrByte = atByte >> 2 & 3;
			}
		} else {
			if(col == 0) {
				//Bottom left
				attrByte = atByte >> 4 & 3;
			} else {
				//Bottom right
				attrByte = atByte >> 6 & 3;
			}
		}
		bgAtLatchHigh= attrByte >> 1 & 1;
		bgAtLatchLow = attrByte & 1;
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
		printPixelDetails();
		int bgPixel = (bgShiftLow >> (xScroll)) & 1;
		bgPixel |= (bgShiftHigh >> (xScroll)) & 1 << 1;
		bgPixel |= (bgAtShiftLow >> (xScroll) & 1) << 2;
		bgPixel |= (bgAtShiftHigh >> (xScroll) & 1) << 3;
		final int dotIndex = (currentScanline * 256) + (currentDot - 1);
		frame[(currentScanline * 256) + (currentDot - 1)] = NESPalette.getColor(paletteRam.read(bgPixel));
		atBytes[dotIndex] = atByte;
		ntBytes[dotIndex] = ntByte;
		ptLowBytes[dotIndex] = ptByteLow;
		ptHighBytes[dotIndex] = ptByteHigh;
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
	
	
	public void printPixelDetails() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtilities.padZeroes(currentScanline, 3, 10));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(currentDot, 3, 10));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(v, 16, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(ntByte, 2, 16));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(atByte, 8, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(ptByteLow, 8, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(ptByteHigh, 8, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(bgAtLatchLow, 1, 10));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(bgAtLatchHigh, 1, 10));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(bgShiftLow, 16, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(bgShiftHigh, 16, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(bgAtShiftLow, 8, 2));
		sb.append(" ");
		sb.append(StringUtilities.padZeroes(bgAtShiftHigh, 8, 2));
		System.out.println(sb.toString());
	}*/
	

}
