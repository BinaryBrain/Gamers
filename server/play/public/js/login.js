App.controller('loginCtrl', ['$scope', '$rootScope', 'wsFactory', function ($scope, $rootScope, wsFactory) {
	// TODO remove this temp login
	var email = "sacha.bron@copperflake.com"
	var email = prompt("email address: (jean.jean@example.com,\ndark.sombre@example.com,\nTabi.nah@example.com,\nsupermool@example.com\njayce@example.com,\npascal@example.com,\nyuguiyooo13@example.com,\noctopus82@example.com,\nslalutrin@example.com,\nsacha.bron@copperflake.com)", "sacha.bron@copperflake.com")

	wsFactory.then(function (ws) {
		// TODO Real creditentials
		ws.send({ cmd: 'login', content: { email: email, password: "love" } });
	})
}])
