'use strict';

var App = angular.module('App', ['angularMoment']);

// Show complete error messages in console
window.onerror = function (errorMsg, url, lineNumber, columnNumber, errorObject) {
	if (errorObject && /<omitted>/.test(errorMsg)) {
		console.error('Full exception message: ' + errorObject.message);
	}
}

App.controller('loginCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	wsFactory.then(function (ws) {
		ws.send({ cmd: 'login' });
	})
}])

App.controller('peopleCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	$rootScope.$on('logged', function (event) {
		wsFactory.then(function (ws) {
			ws.send({ cmd: 'get-people' });
		});
	})

	// dummy data
	$rootScope.me = {
		id: 42,
		name: "Binary Brain"
	}

	$scope.newChat = function (partener) {
		$rootScope.$broadcast('newChat', partener.id);
	}
}]);

App.controller('chatCtrl', function ($scope, $rootScope, $anchorScroll, $timeout, wsFactory) {
	$scope.newMessages = [];

	if (typeof $rootScope.chat === 'undefined') {
		$rootScope.chat = [];
	}

	$rootScope.$on('logged', function (event) {
		wsFactory.then(function (ws) {
			ws.send({ cmd: 'get-chat' });
		});
	})

	$rootScope.$on('newChat', function (event, partenerId) {
		$rootScope.chat.push({
			id: Math.round(Math.random() * 10000), // TODO real ids without possible collision (or id from DB)
			participants: [partenerId],
			messages: []
		})
	})

	// TODO Implement
	$rootScope.$on('newChatGroup', function (event, participants) {
		console.warn("Not implemented!");
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
			ws.send({ cmd: 'new-message', content: { message: newMessage, participants: room.participants } });
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

});

App.filter('getPeople', function($rootScope) {
	return function(ids) {
		ids = ids.slice(0) || [];
		var peopleArr = $rootScope.peopleArray || [];

		var people = [];

		for (var i = 0, l = ids.length; i < l; i++) {
			people.push(peopleArr[ids[i]])
		}

		return people;
	};
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
		people = people.slice(0) || [];
		
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

		ws._send = ws.send;
		ws.send = function (obj) {
			if(typeof $rootScope.token !== 'undefined') {
				obj.auth = $rootScope.token;
			}

			ws._send(angular.toJson(obj));
		}

		ws.onopen = function (event) {
			console.log("WebSocket open");
			resolve(ws);
		}

		ws.onmessage = function (event) {
			var data = angular.fromJson(event.data);

			console.log(data)
				
			switch (data.cmd) {
				case 'people-update':
					$rootScope.$apply(function () {
						var cnt = data.content;
						$rootScope.people = cnt;
						$rootScope.peopleArray = [];

						for (var i = 0, l = cnt.length; i < l; i++) {
							$rootScope.peopleArray[cnt[i].id] = cnt[i]
						}
					});
					break;

				case 'chat-update':
					$rootScope.$apply(function () {
						for (var i in data.content) {
							for (var j in data.content[i].messages) {
								var offset = new Date().getTimezoneOffset();
								data.content[i].messages[j].time = (new Date(data.content[i].messages[j].time) - new Date(-offset*1000*60))
							}
						}
						$rootScope.chat = data.content
					});
					break;
				
				case 'login-success':
					$rootScope.$apply(function () {
						$rootScope.token = data.content;
					});

					$rootScope.$broadcast('logged');
					
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
