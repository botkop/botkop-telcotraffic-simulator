/* variables */
var map;
var subscribers = [];
var celltowers = [];
var subscriberToFollow;

/* constants */
var connectedCellTowerIcon = imageFolder + "/cell-tower-green.png";
var disconnectedCellTowerIcon = imageFolder + "/ext_Cell_Tower_info_048x048.png";
var phoneIcon = imageFolder + "/yellow-phone-small.gif"

var mapOptions = {
    zoom: 3,
    styles: [ {
        "stylers": [ { "invert_lightness": true } ]
    } ],
    backgroundColor: "black",
    center: new google.maps.LatLng(40.69847032728747, -73.9514422416687) // NYC
}

//todo make websocket url dynamic
// var websocketUrl = "@routes.Application.simulatorSocket.webSocketURL()";
var websocketUrl = "ws://localhost:9000/simulator/socket";
var socket = new WebSocket(websocketUrl);

function startSimulator() {
    /* force these values to be numeric, otherwise json will contain strings */
    mcc = parseInt($('#mcc').val());
    mnc = parseInt($('#mnc').val());
    numTrips = parseInt($('#numTrips').val());
    velocity = parseInt($('#velocity').val());
    slideSize = parseInt($('#slideSize').val());
    speedFactor = parseFloat($('#speedFactor').val());

    var request = {
        mcc: mcc,
        mnc: mnc,
        numTrips: numTrips,
        velocity: velocity,
        slide: slideSize,
        speedFactor: speedFactor,
        topic: "request-topic"
    };
    var message = { action: "start", request: request };
    var json = JSON.stringify(message);

    console.log("sending request to socket: " + json);
    socket.send(json);
};

function stopSimulator() {
    var message = { action: "stop" };
    socket.send(JSON.stringify(message));
}

function setSpeedFactor() {
    var sf = parseFloat( $('#speedFactor').val() );
    console.log(sf);
    var message = {action: "setSpeedFactor", request: { speedFactor: sf } };
    socket.send(JSON.stringify(message));
}

/*
Web socket triggers
*/
socket.onopen = function(msg) {
    console.log("web socket opened")
};

socket.onmessage = function(msg) {
    var event = JSON.parse(msg.data);
    if (event.topic == "subscriber-topic") {
        handleSubscriberEvent(event);
        return;
    }

    if (event.topic == "celltower-topic") {
        handleCelltowerEvent(event);
        return;
    }

    console.log("websocket received unkonwn message: " + JSON.stringify(msg.data));
};

socket.onclose = function(msg) {
    console.log('Disconnected - see error log for more information');
};


/* methods */
function unfollow() {
    subscriberToFollow = null;
}

function initialize() {
    map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
    /*
    UI handling
    */
    $('#startButton').click(function() {
        startSimulator();
    })

    $('#stopButton').click(function() {
        stopSimulator();
    })

    $('#speedFactor').change(function() {
        setSpeedFactor();
    })

}

function handleSubscriberEvent(subscriberEvent) {

    var id = subscriberEvent.subscriber.id;
    var phoneMarker = subscribers[id];

    if (phoneMarker == null) {
        phoneMarker = new google.maps.Marker({
            map: map,
            icon: phoneIcon,
            title: "" + id
        });
        subscribers[id] = phoneMarker;

        google.maps.event.addListener(phoneMarker, 'click', function() {
            map.setZoom(12);
            subscriberToFollow = phoneMarker;
        });
    }
    var location = new google.maps.LatLng(subscriberEvent.location.lat, subscriberEvent.location.lng);
    phoneMarker.setPosition(location);
    if (subscriberToFollow == phoneMarker) {
        map.panTo(location)
    }

}

function handleCelltowerEvent(celltowerEvent) {

    var bearerId = celltowerEvent.bearerId;
    var celltower = celltowers[bearerId];
    var location = new google.maps.LatLng(celltowerEvent.celltower.location.lat, celltowerEvent.celltower.location.lng);

    if (celltower == null) {
        var marker = new google.maps.Marker({
            position: location,
            icon: connectedCellTowerIcon,
            map: map
        });
        celltowers[bearerId] = marker;
    }
    else {
        celltower.setPosition(location);
    }

}

google.maps.event.addDomListener(window, 'load', initialize);

