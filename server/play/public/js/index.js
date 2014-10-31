'use strict';

var App = angular.module('App', ['angularMoment']);

// Show complete error messages in console
window.onerror = function (errorMsg, url, lineNumber, columnNumber, errorObject) {
	if (errorObject && /<omitted>/.test(errorMsg)) {
		console.error('Full exception message: ' + errorObject.message);
	}
}

App.controller('peopleCtrl', function ($scope, $rootScope) {
	// dummy data
	$rootScope.me = {
		id: 42,
		name: "Binary Brain"
	}

	$scope.people = [
		{
			id: 1,
			name: "Jean-Jean"
		},
		{
			id: 2,
			name: "xXx Dark sombre xXx"
		},
		{
			id: 3,
			name: "Tabi Nah"
		},
		{
			id: 4,
			name: "SuperMool"
		},
		{
			id: 5,
			name: "Jayce"
		},
		{
			id: 7,
			name: "Pascal"
		},
		{
			id: 14,
			name: "octopus82"
		},
		{
			id: 18,
			name: "Slalutrin"
		},
		{
			id: 13,
			name: "YuGuiYooo13"
		},
	];

	$scope.newChat = function (participant) {
		$rootScope.$broadcast('newChat', [ participant, $rootScope.me ]);
	}
});

App.controller('chatCtrl', function ($scope, $rootScope) {
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

	// dummy data
	$scope.chats = [
		{
			id: 1,
			participants: [
				{
					id: 1,
					name: "Jean-Jean"
				},
				{
					id: 2,
					name: "xXx Dark sombre xXx"
				},
				{
					id: 3,
					name: "Tabi Nah"
				},
				$rootScope.me
			],
			messages: [
				{ time: new Date("2014-10-19T17:09:22.695Z"), from: 1, type: "text", content: "Salut!" },
				{ time: new Date("2014-10-20T17:15:21.687Z"), from: 2, type: "text", content: "yop" },
				{ time: new Date("2014-10-20T17:15:36.725Z"), from: 42, type: "text", content: "pouldre" },
				{ time: new Date("2014-10-20T17:15:52.695Z"), from: 3, type: "text", content: "fnu" },
				{ time: new Date("2014-10-20T17:15:57.687Z"), from: 2, type: "text", content: "ça roule?" },
				{ time: new Date("2014-10-19T17:16:22.695Z"), from: 1, type: "text", content: "Voui!" },
				{ time: new Date("2014-10-20T17:16:58.725Z"), from: 42, type: "text", content: "yep" },
			]
		},
		{
			id: 3,
			participants: [
				{
					id: 18,
					name: "Slalutrin"
				},
				$rootScope.me
			],
			messages: [
				{ time: new Date("2014-10-21T14:35:04.850Z"), from: 14, type: "text", content: "pouldre" },
				{ time: new Date("2014-10-21T14:35:14.750Z"), from: 42, type: "text", content: "fnu" }
			]
		}
	]
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