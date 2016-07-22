angular.module('svcs', ['ngResource']) // using relative path for resources; relative to the root of current web-server: http://<src-host>:<src-port>/

.factory('My_data', function ($resource) {
	return $resource('agents');		// maps to: http://<src-host>:<src-port>/agents
	//query: returns a json array
	// [{agentId,mac,ip,lastActivity,events:[{msgId,agentId,mac,ip,timestamp,origMsg,lvl,type},{},{}]}]
})

.factory('SingleAgent_data', function ($resource, $stateParams) {
	return $resource('agent/' +$stateParams.agentId_URL);
	//query: returns a json array
	// [{agentId,mac,ip,lastActivity,events:[{msgId,agentId,mac,ip,timestamp,origMsg,lvl,type},{},{}]}]
})

.factory('SingleMsg_data', function ($resource, $stateParams) {
	return $resource('msg/' +$stateParams.agentId_URL +"/" +$stateParams.msgId_URL );
	//get: returns a json object
	// {msgId,agentId,mac,ip,timestamp,origMsg,lvl,type}
})
;