package org.gfcyb.arp.server.pojo;

public enum Type {
	// ArpWatch msgs: from cmd-line manual
					//  ------- emails:
	NEW_ACTIVITY,	// (mac/ip)-addr pair was unused for 6+ months
	NEW_STATION,	// mac-addr never seen before
	FLIP_FLOP,		// mac-addr changed from the most-recently-seen addr to 2nd-most-recently-seen
	CHANGED_MAC,	// the ip-addr switched to a new mac-addr
					//  ------- syslog:
	BROADCAST_L2,	// host mac-addr is a broadcast
	BROADCAST_L3,	// host ip-addr is a broadcast
	// https://en.wikipedia.org/wiki/Martian_packet:
	BOGON,			// src ip-addr not local to current subnet, or from reserved addr-space (e.g. 0.0.0.0=this-net, 169.254.*.*=zero-cfg) by accidental/malicious miscfg
	// https://tools.ietf.org/html/rfc5227#page-5
	BROADCAST_L2SRC,// src mac-addr is all-1's(=FF:..:FF=broadcast) or all-0's(=00:...:00=unknown)
	MISMATCH,		// src mac-addr didn't match the address inside the arp-packet
	REUSED_MAC,		// mac-addr changed from the most-recently-seen addr to 3nd+least-recently-seen (similar to flip flop)
	SUPPRESSED,		// flip-flop report suppressed because mac-addr is from a DECnet(=flawed) NIC
	// my msgs:
	KEEPALIVE,		// hart-beat
	POISONED_DGW,	// local arp-table monitor detected default-gateway mac-addr change (MITM attack)
	UNKNOWN			// default type (unhandled)
}

enum Level {
	// Cisco-style log-levels: http://www.ciscopress.com/articles/article.asp?p=101658&seqNum=3
	_0_EMERGENCY,		// (severity 0)—The system is unusable
	_1_ALERT,			// (severity 1)—Immediate action is needed
	_2_CRITICAL,		// (severity 2)—Critical condition
	_3_ERROR,			// (severity 3)—Error condition
	_4_WARNING,		// (severity 4)—Warning condition
	_5_NOTIFY,			// (severity 5)—Normal but significant condition
	_6_INFO,			// (severity 6)—Informational message
	_7_DEBUG,			// (severity 7)—Debugging message
	// https://build.opensuse.org/package/view_file?file=arpwatch-2.1a11-tokenring.diff&package=arpwatch.649&project=openSUSE%3A12.1%3AUpdate&rev=53684a5227b2f202eb2f663fc31fea39
}