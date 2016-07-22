package org.gfcyb.arp.agent;

import java.io.*;
import java.net.Inet4Address;
import java.util.*;
//import org.apache.commons.launcher.*; // https://commons.apache.org/proper/commons-launcher/apidocs/
//import org.apache.commons.logging.*; // https://commons.apache.org/proper/commons-logging/apidocs/
//import org.apache.http.Header; // https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/ && /httpcomponents-client-ga/quickstart.html
import org.apache.http.*;
//import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.gfcyb.arp.agent.pojo.Event;
import org.gfcyb.arp.agent.pojo.Type;

import com.google.gson.Gson;


/* Web-Sources:
 * http://stackoverflow.com/questions/18708087/how-to-execute-bash-command-with-sudo-privileges-in-java
 * http://stackoverflow.com/questions/3643939/java-process-with-input-output-stream
 * http://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
 * http://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html , Process.html
 * http://www.rgagnon.com/javadetails/java-0014.html
 * http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
 * http://stackoverflow.com/questions/10407308/redirect-stdin-and-stdout-in-java
 * http://stackoverflow.com/questions/5389632/capturing-contents-of-standard-output-in-java
 * */

public class Engine {
	
	public final Config cfg;
	private final Queue<Event> pendingQ;				//buffer: from local stdout before sending remotely
	private Process awProc;
	private Gson gson;
	CloseableHttpClient httpclient;
	public Hashtable<Inet4Address,String> baseGws, currGws;
	boolean poisoned = false;
	
	Engine(String cfgPath) throws Exception {
		
		cfg = new Config(cfgPath);
		pendingQ = new MyQ<Event>();
		gson = new Gson();
		httpclient = HttpClients.createDefault();
		
		if( login() ) {
			cleanup();										// only 1 arpwatch process allowed (so killing them all, before starting it)
			Thread.sleep(1000);								// msec
			// ##############################
			startLocalMonitor();
			// ##############################
			if ( Boolean.parseBoolean(cfg.props.getProperty("extendMonitorToLAN")) ) {
				doAW();										// spawning arpwatch process
	    		Thread.sleep(1000);							// msec
				listenToAW();								// a new thread looping over stdout/stderr to capture events
			}
			keepalive();
			// ##############################
			int delay = 0;								// msec
			while ( true ) {							// looping forever ...
				if (! pendingQ.isEmpty()) {
					delay = 0;							// let's rush at full-speed!
					sendToServer( pendingQ.remove() );	// consume an event, and send it over HTTP
				}
				else { // pendingQ is empty
					if( delay!=500 ) delay=500;			// msec relax
				}
				Thread.sleep(delay);
			}
		} else { throw new Exception("login failed"); }
	}

	private void cleanup() {							// kill all running arpwatch processes
		System.out.print("Stopping arpwatch ... ");
		String cmd = "./aw.kill.sh";
		String pwd = cfg.props.getProperty("localpwd");
		try {
			new ProcessBuilder( SystemHelper.getAdministrativeCmdline( pwd, cmd) ).start();
		} catch (IOException e) {e.printStackTrace(); }
		System.out.println("done");
	}
		
	private void startLocalMonitor() {
		System.out.print("Starting local monitor ... ");
		String os = cfg.props.getProperty("os");
		baseGws = SystemHelper.getCurrentGwMacs(os);
		int msec = 2000 ;
		new Timer().scheduleAtFixedRate(				// check local routing-table for changes at regular intervals
				new TimerTask() {
					@Override
					public void run() {
						currGws = SystemHelper.getCurrentGwMacs(os);
						poisoned = SystemHelper.conflictsWith(baseGws,currGws);
					}
				},
				msec*2,		// initial delay
				msec		// iteration period
		);
		new Thread( new Runnable() {					// if poisoned, fire new Event
		    public void run() {
		    	//System.out.println("Starting monitor on master flag (poisoned).");
		    	while (true) {
	            	if(poisoned) {
	            		//System.out.println("ARP-POISONING detected");
	            		Event evt = new Event( new Date(), 
	            				"POISONED routing-table: \n"
	            				+ "BEFORE: "+SystemHelper.expand(baseGws)+"\n"
	            				+ "AFTER: "+SystemHelper.expand(currGws) );
	            		pendingQ.add( evt );
	            		// reset integrity, because we already alerted the server:
	            		poisoned = false;
	            		baseGws = currGws;
	            	}
	            	try { Thread.sleep(400); } catch (InterruptedException e) {}
	            }
		    }
		}).start(); // it calls this.run()
		System.out.println("done");
	}

	private void doAW() throws Exception {				// post-condition: arpwatch running
		System.out.print("Starting arpwatch ... ");
		boolean usingExtScript = Boolean.parseBoolean( cfg.props.getProperty("lauchAWbyscript") );
		String bogon = Boolean.parseBoolean( cfg.props.getProperty("showBogons"))? "" : " -N "; // don't delete the space after -N
		String cmd = ( usingExtScript ? 
				"./aw.start.sh"
				: 
				"/usr/sbin/arpwatch "
				+ " -d "						// debug-mode; needed to capture stdout (stderr actually) from java
				+ " -i "+Config.iface+" "	// explicitly select network-i/f: needed in a multi-homed host
				+ bogon						// optional (but recommended to limit noise): ignore bogons (more ip-nets on 1 phy.wire)
				//+ " -p "					// optional: no promisc access to the wire
				//+ " -R 120 "				// optional: restarts in x seconds if net-i/f goes down; TODO: check if stdout/stderr changes
		);
		String pwd = cfg.props.getProperty("localpwd");
		final ProcessBuilder pb = new ProcessBuilder(
			SystemHelper.getAdministrativeCmdline(pwd, cmd)
		).redirectErrorStream(true);		// subprocess stderr is merged with stdout, so that both can be read by Process.getInputStream(); it copes with:
											// (sudo -S) = (write the prompt to stderr and read the password from stdin (instead of the terminal);
											// (arpwatch -d) = (enable debugging and inhibits forking into bg and emailing the reports; instead, they are sent to stderr)
		//Runtime.getRuntime().exec("echo "+cfg.getRootPwd()+"| sudo -S "+ launcher);
		if (usingExtScript) {
			awProc=pb.start(); // returning, thanks to script spawning in bg (&)
		} else { // not usingExtScript
			new Thread( new Runnable() { // anonymous inner class
			    public void run() {
			    	try{ awProc=pb.start(); // never returning, but in another thread!
			    	} catch(IOException e){e.printStackTrace();} }
			}).start(); // calling run()
		}
		System.out.println("done");
	}
	
	private void listenToAW() { // post-condition: it feeds the queue of ArpEvents with all messages coming from stdout/stderr
		System.out.print("Starting AW listener ... ");
		final InputStream out = awProc.getInputStream(); // Returns the input-stream connected to the normal-output of the subprocess
		//final InputStream err = proc.getErrorStream(); // Returns the input-stream connected to the error-output of the subprocess
		new Thread( new Runnable() { // the background thread watches the output from the process
		    public void run() {
		        try {
		            BufferedReader stdout = new BufferedReader(new InputStreamReader(out));
		    		//BufferedReader stderr = new BufferedReader(new InputStreamReader(err)); // useless, because: ProcessBuilder.redirectErrorStream(true)
		            stdout.readLine(); // clean-up the buffer from sudo-prompt (=1 line)
		            String line;
		            // producer loop: looping to parse stdout String's into ArpEvent's
		            while ((line=stdout.readLine()) != null) {	// it only exits when the reader, which reads from the process's stdout, returns EoF.
																// This only happens when the process exits. It will not return end-of-file if there happens to be no more output
		            											// from the process. Instead, it will wait for the next line of output from the process and not return until it has this next line.
		                //System.out.println(line);
		                pendingQ.add( new Event( new Date(), line ) );	// TODO: check if synchronized is needed here on pendingQ
		            }
		            // you should never get here:
					System.out.println("--------- ERROR: stopped producing events!!!");
		        } catch (IOException e) { e.printStackTrace();
		        } finally { try{out.close();}catch(IOException e){} }
		    }
		}).start(); // it calls this.run()
		//System.out.println( "Script exit-value: "+proc.exitValue() );
		//int pid = proc.waitFor(); // the outer process waits for the sub-process to finish
		System.out.println("done");
	}
	
	private void keepalive() throws Exception { // looping on a parallel thread
		int msec = Integer.parseInt(cfg.props.getProperty("keepalive")) * 1000 ;
		new Timer().scheduleAtFixedRate(
				new TimerTask() { // anonymous inner class
					@Override
					public void run() {
						boolean isAWalive = (awProc==null)? false : awProc.isAlive();
						Event evt = new Event( new Date(), 
								"Keepalive from: "+ Config.agentId +
								"; AWalive="+ (""+isAWalive ).toUpperCase() + // TODO: get PID
								"; pendingMsgs="+pendingQ.size()
							);
						while( ! pendingQ.isEmpty() ) { // we dont wanna mess with multiline output from arpwatch: we wait until the queue is empty
						  try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
						}
						sendToServer(evt);
					}
				},
				0,		// initial delay
				msec	// iteration period
		);
	}
	
	private void sendToServer(Event evt) { // over HTTP
		System.out.println("Sending: "+evt);
		String logFile = cfg.props.getProperty("log");
		try {
			if (evt.getType()!=Type.KEEPALIVE) {
				if( isLogFileUnderSafeSize(logFile) ) { zipOrDeleteLogFile(logFile); }
				try( // http://www.mkyong.com/java/how-do-convert-java-object-to-from-json-format-gson-api/
					PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( logFile, true) )); // appending text to log
				) {
					gson.toJson( evt, out);
					out.append('\n');
				} // TODO: check if opening+closing the log-file for each message is too much overhead
			}
			String json = gson.toJson(evt);
			HttpPost request = new HttpPost( cfg.props.getProperty("server") + "/postEvent" );
	        request.addHeader(
	        		"content-type", 
	        		"application/json"		//"application/x-www-form-urlencoded"
	        );
	        request.setEntity( new StringEntity(json) );
	        /* //from: http://stackoverflow.com/questions/29044773/apache-httpclient-json-post
	        StringEntity params =new StringEntity(jsonString);
	        params.setContentType("application/json");
	        params.setContentEncoding("UTF-8");
	        postRequest.setEntity(params);
	        */
			CloseableHttpResponse response = httpclient.execute(request);
			try {
			    //System.out.println(response2.getStatusLine());
			    HttpEntity entity2 = response.getEntity();
			    //for(Header h : response2.getAllHeaders()) { System.out.println(h); }
			    EntityUtils.consume(entity2);
			} catch (Exception ex) { // handle exception here
		    } finally { response.close(); }
		} catch(Exception ex) {ex.printStackTrace();}
	}
	
	private void zipOrDeleteLogFile(String logFile) { }

	private boolean isLogFileUnderSafeSize(String logFile) { return true; }

	private boolean login() { return true; } // TODO do a real login
	
	
	
}
