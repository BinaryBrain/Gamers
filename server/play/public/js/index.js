'use strict';

var App = angular.module('App', ['angularMoment']);

// Show complete error messages in console
window.onerror = function (errorMsg, url, lineNumber, columnNumber, errorObject) {
	if (errorObject && /<omitted>/.test(errorMsg)) {
		console.error('Full exception message: ' + errorObject.message);
	}
}

App.controller('peopleCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	wsFactory.then(function (ws) {
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

App.controller('chatCtrl', function ($scope, $rootScope, $anchorScroll, $timeout, wsFactory) {
	$scope.newMessages = [];

	if (typeof $rootScope.chat === 'undefined') {
		$rootScope.chat = [];
	}

	$rootScope.$on('newChat', function (event, participants) {
		$rootScope.chat.push({
			id: Math.round(Math.random() * 10000), // TODO real ids without possible collision
			participants: participants,
			messages: []
		})
	})

	$scope.sendMessage = function (room, message) {
		var newMessage = {
			from: $rootScope.me.id,
			time: new Date(),
			type: "text",
			content: message
		}

		room.messages.push(newMessage);

		wsFactory.then(function (ws) {
			ws.send(angular.toJson({ cmd: 'new-message', content: newMessage }));
		});

		$scope.newMessages[room.id] = "";
	}

	$scope.close = function (room) {
		for (var i in $rootScope.chat) {
			if ($rootScope.chat[i].id === room.id) {
				$rootScope.chat.splice(i, 1);
			}
		}
	}

	$scope.$watch('chat', function () {
		$timeout(function () {
			var msgDivs = document.getElementsByClassName("messages");
			
			for (var i = 0, l = msgDivs.length; i < l; i++) {
				msgDivs[i].scrollTop = msgDivs[i].scrollHeight;
			}
		});
	}, true)

	wsFactory.then(function (ws) {
		ws.send('{ "cmd": "get-chat" }');
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

				case 'chat-update':
					$rootScope.$apply(function () {
						$rootScope.chat = data.content;
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
