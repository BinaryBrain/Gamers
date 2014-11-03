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
			ws.send('{ "cmd": "get-people" }').
				then(function (content) {
					console.log("recieved people:", content)
					$rootScope.people = content;
				});
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
			ws.send('{ "cmd": "get-chats" }').
				then(function (content) {
					console.log("recieved chats:", content)
					$rootScope.chats = content;
				});
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

App.factory('wsFactory', function ($q) {
	return $q(function (resolve, reject) {
		var ws = new WebSocket(document.location.origin.replace(/^http/, "ws") + "/ws");

		ws.onopen = function (event) {
			console.log("WebSocket open");
			resolve(ws);
		}

		/*
				ws.onmessage = function (event) {
					var data = angular.fromJson(event.data);
					
					// console.warn("Unhandled message recieved:", data)
				}
		*/
		ws.onclose = function (event) {
			// TODO handle me!
			console.warn("WebSocket closed:", event);
		}

		ws.onerror = function (event) {
			// TODO handle me!
			console.error("WebSocket error:", event);
		}

		ws._send = ws.send;

		ws.send = function (data) {
			console.log("Request", data)
			return $q(function (resolve, reject) {
				ws.onmessage = function (event) {
					console.log("Response", event)
					var data = angular.fromJson(event.data);
					resolve(data.content);
				}

				ws._send(data);
			});
		}
	});
})
