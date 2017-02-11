package com.bibler.awesome.bibnes20.communications;

import java.util.ArrayList;

public abstract class Notifier {
	
	protected ArrayList<Notifiable> objectsToNotify;
	
	
	protected void notify(String message, Object messagePacket) {
		for(Notifiable objectToNotify : objectsToNotify) {
			objectToNotify.takeNotice(message, messagePacket);
		}
	}
	
	public void registerObjectToNotify(Notifiable objectToNotify) {
		if(objectsToNotify == null) {
			objectsToNotify = new ArrayList<Notifiable>();
		}
		objectsToNotify.add(objectToNotify);
	}
	

}
