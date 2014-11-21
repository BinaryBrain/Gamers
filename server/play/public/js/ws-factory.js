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

			if (typeof data.error !== 'undefined') {
				alert(data.error)
				return
			}

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
