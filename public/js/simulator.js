function initWebSocket(url) {
    var websocket;
    try {
        websocket = new WebSocket(url);

        websocket.onopen = function(msg) {
            socketStatus.innerHTML = 'Connected to: ' + event.currentTarget.url;
            socketStatus.className = 'open';
        };

        websocket.onmessage = function(msg) {
            var message = event.data;
            messagesList.innerHTML += '<li class="received"><span>Received:</span>' + message + '</li>';
        };

        websocket.onclose = function(msg) {
            socketStatus.innerHTML = 'Disconnected - see error log for more information';
        };

    } catch(ex) {
        /* must check if console exists, otherwise selenium tests fail */
        if (window.console) {
            console.log(ex);
        }
    }
    return websocket;
}

function startSimulator(websocket) {
    messagesList.innerHTML = '';
    /* force these values to be numeric, otherwise json will contain strings */
    var mcc = Number(document.getElementById("mcc").value);
    var mnc = Number(document.getElementById("mnc").value);
    var numTrips = Number(document.getElementById("numTrips").value);
    var json = { "mcc": mcc, "mnc": mnc, "numTrips": numTrips };
    websocket.send(JSON.stringify(json));
};

function stopSimluator() {

}
