package org.gfcyb.arp.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.gfcyb.arp.server.pojo.Event;
import org.gfcyb.arp.server.pojo.RemoteAgent;
import org.gfcyb.arp.server.pojo.Type;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/* from: http://spring.io/guides/gs/spring-boot/
 * 
 *  [...] The class is flagged as a @RestController, meaning it’s ready for use by Spring MVC to handle web requests.
 *  @RequestMapping maps / to the index() method. When invoked from a browser or using curl on the command line,
 *  the method returns pure text. That’s because @RestController combines @Controller and @ResponseBody, 
 *  two annotations that results in web requests returning data (rather than a view).
 */
@RestController
public class MyRestController { // don't do @RequestMapping to value="/": it shadows static folder(=UI)
	
	static Map<String,RemoteAgent> agents = new Hashtable<String,RemoteAgent>();
	static String serverId = "pippo";
	public MyRestController() {
		try {
			serverId= InetAddress.getLocalHost().getHostName().toLowerCase();
		} catch (UnknownHostException e) { }
	}
	
	@RequestMapping(value = "/postEvent", method = RequestMethod.POST)
    public ResponseEntity<Event> postEvent(@RequestBody Event evt) {
		if (evt!=null) {
			// se l'agent è sconosciuto al server (1^ volta che contatta server), registralo
			if( ! agents.containsKey(evt.getAgentId())) {
				agents.put( evt.getAgentId(),
						new RemoteAgent(
								evt.getAgentId(),
								evt.getMac(),
								evt.getIp()
						)
				);
			}
			agents.get(evt.getAgentId()).setLastActivity(evt.getTimestamp()); //TODO: better than new Date()?
			// se è un keepalive, aggiorna ultimo contatto e basta
			if (evt.getType()!=Type.KEEPALIVE) {
				if( agents.get(evt.getAgentId()).getEvents().size() >= 10000 ) {
					agents.get(evt.getAgentId()).getEvents().subList(0, 8000).clear();
				}
				agents.get(evt.getAgentId()).add(evt);
			}
		}
		System.out.println(
				"Remote event from: "+evt.getAgentId()+"; "+evt
				//+" ; (agents#=" + agents.size() + ", events#=" + agents.get(evt.getAgentId()).size() + ")"
				);
		// TODO: add persistence here
    	return new ResponseEntity<Event>( evt, HttpStatus.OK );
    }
	
	@RequestMapping(value = "/agents") // default HTTP-method= GET
    public ResponseEntity<List<RemoteAgent>> allAsList() {
		return new ResponseEntity<List<RemoteAgent>>(
				new ArrayList<RemoteAgent>(agents.values()), HttpStatus.OK );
    }
	
	// see: http://www.baeldung.com/spring-requestmapping
	// see: http://www.journaldev.com/3358/spring-requestmapping-requestparam-pathvariable-example
	
	@RequestMapping(value = "/agent/{agentId}") // default HTTP-method= GET
    public ResponseEntity<List<Event>> agentMessages(
    		@PathVariable("agentId") String agent
    		//@RequestParam(value="agentId", defaultValue="pippo") String agent //makes URL like /agent?agentId=pippo
    ) { List<Event> evts = agents.get(agent).getEvents();
    	return new ResponseEntity<List<Event>>( evts, HttpStatus.OK );
    }
	
	@RequestMapping(value = "/msg/{agentId}/{msgId}") // default HTTP-method= GET
    public ResponseEntity<Event> msgDetails(
    		@PathVariable("agentId") String agent, @PathVariable("msgId") int msgId
    		//@RequestParam(value="agentId") String agent, @RequestParam(value="msgId") int msgId
    ) {
		Event response = null;
		for (Event evt : agents.get(agent).getEvents() ) {
			if( evt.getMsgId() == msgId ) { response = evt; }
		}
    	return new ResponseEntity<Event>( response, HttpStatus.OK );
    }
}
