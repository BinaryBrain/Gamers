App.controller('loginCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	wsFactory.then(function (ws) {
		// TODO Real creditentials
		ws.send({ cmd: 'login', content: { email: "sacha.bron@copperflake.com", password: "love" } });
	})
}])
