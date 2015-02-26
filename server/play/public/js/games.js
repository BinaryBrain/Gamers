App.controller('gamesCtrl', function ($scope, $rootScope, wsFactory) {
	// TODO this array is a stub
	$scope.games = [{
		name: "Pong",
		description: "lol I'm a pong",
		players: "2", // May change for a better strcture: [[4], [2, 2], [1, 1, 1, 1], [3, 1]] (Mario Party)
		image: "what to do...", // Find a clever way to manage this. Maybe in the game folder
	},
	{
		name: "Bomberman",
		description: "Drop bombs and don't explode.\nTry to explode others.\nExploding others is always fun in video games.\nDon't try it at home. :)",
		players: "2-8",
		image: "what to do...",
	},
	{
		name: "The 3rd game",
		description: "I'm mysterious.",
		players: "1",
		image: "what to do...",
	}];

	// TODO implement 'get-games' on the server
	/*
	$rootScope.$on('logged', function (event) {
		wsFactory.then(function (ws) {
			ws.send({ cmd: 'get-games' });
		});
	})
	*/

	$rootScope.$on('', function (event, participants) {
		console.warn("Not implemented!");
	})
});

/* TODO
Find a way to keep the database up to date with every game folders.
If there is any database.
*/
