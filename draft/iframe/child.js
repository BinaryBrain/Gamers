window.onmessage = function (e) {
  var msg = e.data;
  if (msg.cmd == 'ping') {
    console.log("ping")
    pong();
  }
}

function pong() {
  console.log('send pong')
  var res = { cmd: 'pong' }
  //console.log(window.)
  window.postMessage(res, '*');
  window.top.postMessage(res, '*');
  
}