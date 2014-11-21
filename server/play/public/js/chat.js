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