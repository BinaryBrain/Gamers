function onload() {
  ping();
}

window.onmessage = function(e) {
  var msg = e.data;
  if (msg.cmd === 'pong') {
    console.log("pong");
  }
}

function ping() {
  console.log('send ping')
  var msg = { cmd: "ping" }
  childFrame.contentWindow.postMessage(msg, '*');
}