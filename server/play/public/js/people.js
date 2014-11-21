App.controller('peopleCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	$rootScope.$on('logged', function (event) {
		wsFactory.then(function (ws) {
			ws.send({ cmd: 'get-people' });
		});
	})

	$scope.newChat = function (partener) {
		$rootScope.$broadcast('newChat', partener.id);
	}
}]);
