var websocket = new WebSocket(document.location.origin.replace(/^http/, "ws") + "/socket");

websocket.onopen = function (event) {
	console.log("WebSocket open")
	websocket.send("Hello World!");
}

websocket.onmessage = function (event) {
	console.log("message received: ", event.data);
}