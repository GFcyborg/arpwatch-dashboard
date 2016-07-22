angular.module('ctrls', ['svcs'])
.controller('AppCtrl', function($scope, $ionicModal, $timeout) {
	// does nothing
})
.controller('MasterCtrl', function($scope, My_data) {
    $scope.agents_DOM = My_data.query();
    // With the new view caching in Ionic, Controllers are only called
    // when they are recreated or on app start, instead of every page change.
    // To listen for when this page is active (for example, to refresh data),
    // listen for the $ionicView.enter event:
    //$scope.$on('$ionicView.enter', function(e) { });
})
.controller('AgentCtrl', function($scope, $stateParams, SingleAgent_data) {
	$scope.agent_DOM =		SingleAgent_data.query();
	$scope.agentId_DOM =	$stateParams.agentId_URL;
	$scope.myflipflop = 0;
	$scope.orderByMe = function(x) {
		$scope.myOrder = x;
		if ($scope.myflipflop) { $scope.myflipflop=0; } else { $scope.myflipflop=1; }
	}
	$scope.myFilter = function(element) {
		return element.origMsg.match(/mysearch/) ? true : false;
	};
})
.controller('MsgCtrl', function($scope, $stateParams, SingleMsg_data) {
	$scope.msg_DOM =		SingleMsg_data.get(      );
	$scope.agentId_DOM =	$stateParams.agentId_URL;
	$scope.msgId_DOM =		$stateParams.msgId_URL;
    //$scope.agent_DOM = My_data.get({agentId_data: $stateParams.agentId_URL});
})
;