package com.bibler.awesome.bibnes20.controllers;
import java.util.HashMap;

//import net.java.games.input.Component;
//import net.java.games.input.Controller;
//import net.java.games.input.Event;
//import net.java.games.input.EventQueue;

public class USBGamepad extends BaseController implements Runnable  {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int read() {
		// TODO Auto-generated method stub
		return 0;
	}
	/*
	Controller controller;
	
	private Component[] buttons = new Component[4];
	private HashMap<Integer, String> prompts = new HashMap<Integer, String>();
	
	
	
	
	private int gamepadValues;
	
	public USBGamepad(Controller controller) {
		this.controller = controller;
		fillMap();
		Thread t = new Thread(this);
		t.start();
	}
	
	private void fillMap() {
		prompts.put(1, "Right");
		prompts.put(2, "Left");
		prompts.put(4, "Down");
		prompts.put(8, "Up");
		prompts.put(16, "Start");
		prompts.put(32, "Select");
		prompts.put(64, "B");
		prompts.put(128, "A");
		Component[] components = controller.getComponents();
		for(Component c : components) {
			if(c.getName().equalsIgnoreCase("Button 9")) {
				buttons[3] = c;
			} else if(c.getName().equalsIgnoreCase("Button 8")) {
				buttons[2] = c;
			} else if(c.getName().equalsIgnoreCase("Button 2")) {
				buttons[1] = c;
			} else if(c.getName().equalsIgnoreCase("Button 1")) {
				buttons[0] = c;
			}
		}
		controller.poll();
		controller.getEventQueue();
	}
	
	@Override
	public void write() {
		strobeRegister = gamepadValues;
		
	}

	@Override
	public int read() {
		final int output = strobeRegister & 1;
		strobeRegister  >>= 1;
		return output;
	}
	
	public void calibrate() {
		controller.poll();
		controller.setEventQueueSize(2);
		controller.getEventQueue();
		boolean finished = false;
		int state = 1;
		while(!finished) {
			System.out.println("Press " + prompts.get(state));
			Component c = null;
			while(c == null) {
				c = getComponentIdentifer();
			}
			//buttons.put(c, state);
			state *= 2;
			if(state > 128) {
				finished = true;
				break;
			}
		}
	}
	
	private Component getComponentIdentifer() {
		controller.poll();
		final EventQueue events = controller.getEventQueue();
		Event event = new Event();
		events.getNextEvent(event);
		if(event.getValue() == 1 || event.getValue() == -1) {
			return event.getComponent();
		} else {
			return null;
		}
	}
	
	private boolean isPressed(Event event) {
        Component component = event.getComponent();
        if (component.isAnalog()) {
            if (Math.abs(event.getValue()) > 0.2f) {
                return true;
            } else {
                return false;
            }
        } else if (event.getValue() == 0) {
            return false;
        } else {
            return true;
        }
    }
	
	@Override
	public void run() {
		if(controller != null) {
			Event event = new Event();
			while(!Thread.interrupted()) {
				controller.poll();
				EventQueue queue = controller.getEventQueue();
				Component component;
				while(queue.getNextEvent(event)) {
					component = event.getComponent();
					if (component.getIdentifier() == Component.Identifier.Axis.X) {
						if (event.getValue() > .25) {
							gamepadValues |= 128;//left on, right off
                            gamepadValues &= ~64;
                        } else if (event.getValue() < -.25) {
                            gamepadValues |= 64;
                            gamepadValues &= ~128;
                        } else {
                            gamepadValues &= ~(128 | 64);
                        }
                    } else if (component.getIdentifier() == Component.Identifier.Axis.Y) {
                        if (event.getValue() > .25) {
                            gamepadValues |= 32;//up on, down off
                            gamepadValues &= ~16;
                        } else if (event.getValue() < -.25) {
                            gamepadValues |= 16;//down on, up off
                            gamepadValues &= ~32;
                        } else {
                            gamepadValues &= ~(16 | 32);
                        }
                    } else if (component == buttons[0]) {
                        if (isPressed(event)) {
                            gamepadValues |= 1;
                        } else {
                            gamepadValues &= ~1;
                        }
                    } else if (component == buttons[1]) {
                        if (isPressed(event)) {
                            gamepadValues |= 2;
                        } else {
                            gamepadValues &= ~2;
                        }
                    } else if (component == buttons[2]) {
                        if (isPressed(event)) {
                            gamepadValues |= 4;
                        } else {
                            gamepadValues &= ~4;
                        }
                    } else if (component == buttons[3]) {
                        if (isPressed(event)) {
                            gamepadValues |= 8;
                        } else {
                            gamepadValues &= ~8;
                        }
                    }
				}
			}
		}
	}*/
}
