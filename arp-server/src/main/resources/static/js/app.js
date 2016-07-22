// https://ccoenraets.github.io/ionic-tutorial/angular-ui-router.html
angular.module('app', ['ionic', 'ctrls']) // Ionic Starter App

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() { // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard for form inputs)
    if (window.cordova && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
      cordova.plugins.Keyboard.disableScroll(true);
    }
    if (window.StatusBar) { // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }
  });
})
.config(function($stateProvider, $urlRouterProvider) {
  $stateProvider
    .state('ArpWatch', {
    url: '/AW',
    abstract: true,
    templateUrl: 'templates/menu.html',
    controller: 'AppCtrl'
  })
  .state('ArpWatch.agents_state', {
    url: "/agents",
    views: {
        'menuContent': {
            templateUrl: "templates/agents.html",
            controller: 'MasterCtrl'
        }
    }
  })
  .state('ArpWatch.agent_state', {
      url: "/agents/:agentId_URL",
      views: {
          'menuContent': {
            templateUrl: "templates/agent.html",
            controller: 'AgentCtrl'
        }
      }
  })
  .state('ArpWatch.msg_state', {
      url: "/agents/:agentId_URL/:msgId_URL",
      views: {
          'menuContent': {
            templateUrl: "templates/msg.html",
            controller: 'MsgCtrl'
        }
      }
  })
  ;
  // if none of the above states are matched, use this as the fallback
  $urlRouterProvider.otherwise('/AW/agents');
});