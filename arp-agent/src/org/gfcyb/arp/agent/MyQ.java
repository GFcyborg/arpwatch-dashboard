package org.gfcyb.arp.agent;

//import java.text.*;
import java.util.*;

import org.gfcyb.arp.agent.pojo.Event;

public class MyQ<T> extends LinkedList<Event> implements Queue<Event> {
	private static final long serialVersionUID = 1L;
	LinkedList<Event> tmp = new LinkedList<Event>();
	Timer countdown = new Timer();

	@Override
	public boolean add(Event evt) {
		boolean startingLinesAddressed = false;
		boolean eventQueued = false;
		
		if( evt.getOrigMsg().replaceAll("\\s","").trim().isEmpty() ) {return false;} // just discard empty lines
		
		else	// line is not empty
		if(
			evt.getOrigMsg().contains("From: arpwatch (Arpwatch ")
			|| evt.getOrigMsg().contains("POISONED routing-table:")
			|| evt.getOrigMsg().contains("arpwatch: ethernet mismatch")
		) { // line is starting a new multi-line message
			startingLinesAddressed = flushMergeTmp();				// a new start is coming: first we need to flush tmp
		}
		else { startingLinesAddressed = true; }	// line is not starting a new multi-line message
		if ( tmp.size() >= 20) { flushMergeTmp(); } // to avoid memory problems (e.g.: ethernet mismatch )
		eventQueued = addToTmp(evt);
		return startingLinesAddressed && eventQueued;
	}

	private boolean addToTmp(Event evt) {
		countdown.cancel();
		countdown = new Timer(); // javadocs: "Once a Timer is canceled, no more tasks may be scheduled on it"
		countdown.schedule(
			new TimerTask() {
				@Override
				public void run() {
					if(! tmp.isEmpty()) {
						System.out.println("Flushing tmp for timeout ...");
						flushMergeTmp();
					}
				}
			},
			2000		// msec delay from LAST line, after which we assume multi-line message is finished
		);
		return tmp.add(evt);
	}

	private boolean flushMergeTmp() {
		boolean result = false;
		if(! tmp.isEmpty()) {
			/*
			Date t;
			try { // get the timestamp of first line/event in tmp ...
				//t = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse( tmp.getFirst().getTimestamp() );
			} catch (ParseException e) { t = new Date(); } // ... or at least get current timestamp
			*/
			StringBuilder multiline = new StringBuilder();
			for(Event evt : tmp) { multiline.append( evt.getOrigMsg()+"\n"); }
			tmp.clear();
			result = super.add( new Event( new Date(), multiline.toString()) );
		} else { result = true; }		// tmp is empty
		return result;
	}

}
