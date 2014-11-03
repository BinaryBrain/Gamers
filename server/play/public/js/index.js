'use strict';

var App = angular.module('App', ['angularMoment']);

// Show complete error messages in console
window.onerror = function (errorMsg, url, lineNumber, columnNumber, errorObject) {
	if (errorObject && /<omitted>/.test(errorMsg)) {
		console.error('Full exception message: ' + errorObject.message);
	}
}

App.controller('mainCtrl', [function () {}]);

App.controller('peopleCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	wsFactory.
		then(function (ws) {
			ws.send('{ "cmd": "get-people" }');
		});

	// dummy data
	$rootScope.me = {
		id: 42,
		name: "Binary Brain"
	}

	$scope.newChat = function (participant) {
		$rootScope.$broadcast('newChat', [ participant, $rootScope.me ]);
	}
}]);

App.controller('chatCtrl', function ($scope, $rootScope, wsFactory) {
	$scope.newMessages = [];

	$rootScope.$on('newChat', function (event, participants) {
		$scope.chats.push({
			id: Math.round(Math.random() * 10000), // TODO real ids without possible collision
			participants: participants,
			messages: []
		})
	})

	$scope.sendMessage = function (room, message) {
		room.messages.push({
			from: $rootScope.me.id,
			time: new Date(),
			type: "text",
			content: message
		});

		$scope.newMessages[room.id] = "";
	}

	$scope.close = function (room) {
		for (var i in $scope.chats) {
			if ($scope.chats[i].id === room.id) {
				$scope.chats.splice(i, 1);
			}
		}
	}

	wsFactory.
		then(function (ws) {
			ws.send('{ "cmd": "get-chats" }');
		});
});

App.filter('listNames', function() {
	return function(people) {
		people = people || [];
		
		var out = people.map(function (p) {
			return p.name.replace(/ /g, '\u00A0'); // &nbsp;
		});

		return out.join(', ');
	};
});

App.filter('removeMe', function($rootScope) {
	return function(people) {
		people = people || [];
		
		for (var i in people) {
			if (people[i].id === $rootScope.me.id) {
				people.splice(i, 1);
			}
		}

		return people;
	};
});

App.factory('wsFactory', function ($q, $rootScope) {
	return $q(function (resolve, reject) {
		var ws = new WebSocket(document.location.origin.replace(/^http/, "ws") + "/ws");

		ws.onopen = function (event) {
			console.log("WebSocket open");
			resolve(ws);
		}

		ws.onmessage = function (event) {
			var data = angular.fromJson(event.data);
			
			switch (data.cmd) {
				case 'people-update':
					$rootScope.$apply(function () {
						$rootScope.people = data.content;
					});
					break;

				case 'chats-update':
					$rootScope.$apply(function () {
						$rootScope.chats = data.content;
					});
					break;
				
				default:
					console.warn("Unhandled message recieved:", data);
					break;
			}
		}

		ws.onclose = function (event) {
			// TODO handle me!
			console.warn("WebSocket closed:", event);
		}

		ws.onerror = function (event) {
			// TODO handle me!
			console.error("WebSocket error:", event);
		}
	});
})
