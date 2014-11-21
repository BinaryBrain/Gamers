'use strict';

var App = angular.module('App', ['angularMoment']);

// Show complete error messages in console
window.onerror = function (errorMsg, url, lineNumber, columnNumber, errorObject) {
	if (errorObject && /<omitted>/.test(errorMsg)) {
		console.error('Full exception message: ' + errorObject.message);
	}
}

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

