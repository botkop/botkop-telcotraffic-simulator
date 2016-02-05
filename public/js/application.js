
/* variables */
var map;
var subscribers = [];
var celltowers = [];
var subscriberToFollow;
var celltowerCluster;
var isRunning = false;

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

var socket;

function startSimulator() {
    sendRequest("start");
    if ($('#showTowers').prop('checked')){
        showTowers();
    }
};

function sendRequest(action) {
    /* force these values to be numeric, otherwise json will contain strings */
    mcc = parseInt($('#mcc').val());
    mnc = parseInt($('#mnc').val());
    numTrips = parseInt($('#numTrips').val());
    velocity = parseInt($('#velocity').val());
    slideSize = parseInt($('#slideSize').val());

    var request = {
        mcc: mcc,
        mnc: mnc,
        numTrips: numTrips,
        velocity: velocity,
        slide: slideSize,
        topic: "request-topic"
    };
    var message = { action: action, request: request };
    var json = JSON.stringify(message);

    console.log("sending request to socket: " + json);
    socket.send(json);
}

function stopSimulator() {
    var message = { action: "stop" };
    socket.send(JSON.stringify(message));
}

/*
Web socket triggers
*/
var socketOnOpen = function(msg) {
    console.log("websocket opened");
    sendRequest("init");
};

var socketOnClose = function(msg) {
    console.log('websocket disconnected - waiting for connection');
    websocketWaiter();
}

var socketOnMessage = function(msg) {
    var event = JSON.parse(msg.data);
    if (event.topic == "subscriber-topic") {
        handleSubscriberEvent(event);
        return;
    }

    if (event.topic == "celltower-topic") {
        handleCelltowerEvent(event);
        return;
    }

    if (event.action != null) {
        handleAction(event);
        return;
    }

    console.log("websocket received unknown message: " + JSON.stringify(msg.data));
};

function websocketWaiter(){
    setTimeout(function(){
        socket = new WebSocket(websocketUrl);
        socket.onopen = socketOnOpen;
        socket.onclose = socketOnClose;
        socket.onmessage = socketOnMessage;
    }, 1000);
}

/* methods */
function unfollow() {
    console.log("unfollow");
    subscriberToFollow = null;
}

function initialize() {
    websocketWaiter();
    map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
    celltowerCluster = new MarkerClusterer(map, [], {gridSize: 50, maxZoom: 10});
    /*
    UI handling
    */
    $( "#startStop" ).button();
    $( "#startStop" ).change(function(){
        if (this.checked) {
            startSimulator();
        }
        else {
            stopSimulator();
        }
    });

    $('#unfollowButton').click(function() {
        unfollow();
    });

    $('#showTowers').change(function() {
        if (this.checked) {
            showTowers();
        }
        else {
            hideTowers();
        }
    });

    $( "#slideSizeSlider" ).slider({
        min: 10,
        max: 10000,
        step: 10,
        value: 500,
        slide: function( event, ui ) {
            $( "#slideSize" ).val( ui.value );
        },
        stop: function( event, ui ) {
            socketUpdateSlide(ui.value);
        }
    });

    $( "#slideSize" ).val( $( "#slideSizeSlider" ).slider( "value" ) );

    $( "#velocitySlider" ).slider({
        min: 0,
        max: 12000,
        step: 10,
        value: 120,
        slide: function( event, ui ) {
            $( "#velocity" ).val( ui.value );
        },
        stop: function( event, ui ) {
            socketUpdateVelocity(ui.value);
        }
    });

    $( "#velocity" ).val( $( "#velocitySlider" ).slider( "value" ) );
}

function socketUpdateSlide(value) {
    var requestUpdate = {
        action: "update",
        request: {
            slide: value,
            topic: "request-topic"
        }
    }
    socket.send(JSON.stringify(requestUpdate));
}

function socketUpdateVelocity(value) {
    var requestUpdate = {
        action: "update",
        request: {
            velocity: value,
            topic: "request-topic"
        }
    }
    socket.send(JSON.stringify(requestUpdate));
}

function handleSubscriberEvent(subscriberEvent) {

    var id = subscriberEvent.subscriber.id;
    var phoneMarker = subscribers[id];

    if (phoneMarker == null) {
        phoneMarker = new google.maps.Marker({
            map: map,
            icon: phoneIcon,
            title: "" + id,
            zIndex: 10
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
        celltowerCluster.redraw();
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
            zIndex: 9,
            map: map
        });
        celltowers[bearerId] = marker;
    }
    else {
        celltower.setPosition(location);
    }

}

function showTowers(){

    mcc = parseInt($('#mcc').val());
    mnc = parseInt($('#mnc').val());

    if (isNaN(mcc) || isNaN(mnc)) {
        console.log("invalid mcc or mnc");
        return;
    }

    var url = "/simulator/rest/celltowers/" + mcc + "/" + mnc;
    $.getJSON(url, function(result){
        var markers = [];
        var bounds = new google.maps.LatLngBounds();
        result.forEach(function (tower) {
            var latlng = new google.maps.LatLng(tower.location.lat, tower.location.lng);
            var marker = new google.maps.Marker({
                position: latlng,
                title: mcc + ':' + mnc + ':' + tower.area + ":" + tower.cell,
                icon: disconnectedCellTowerIcon,
                zIndex: 0
            });
            markers.push(marker);
            bounds.extend(latlng);
        });

        map.fitBounds(bounds);

        celltowerCluster.clearMarkers();
        celltowerCluster.addMarkers(markers);
    });

}

function hideTowers(){
    console.log("clearing celltower cluster")
    celltowerCluster.clearMarkers();
}

function handleAction(event) {
    console.log(event);

    if (event.action == "update") {
        if (event.request.velocity != null) {
            $('#velocitySlider').slider("value", event.request.velocity);
            $( "#velocity" ).val(event.request.velocity);
        }

        if (event.request.slide != null) {
            $('#slideSizeSlider').slider("value", event.request.slide);
            $( "#slideSize" ).val(event.request.slide);
        }
    }

    if (event.action == "stop") {
        for (id in celltowers) {
            var marker = celltowers[id];
            marker.setMap(null);
        }
        celltowers = [];

        for (id in subscribers) {
            var marker = subscribers[id];
            marker.setMap(null);
        }
        subscribers = [];

        $( "#startStop" ).button({label: "Start" });
    }

    if (event.action == "start") {
        updateRequest(event);
        $("#startStop").button({label: "Stop" });
    }

    if (event.action == "current") {
        updateRequest(event);
    }

}

function updateRequest(event) {
    var r = event.request;
    $('#mcc').val(r.mcc);
    $('#mnc').val(r.mnc);
    $('#numTrips').val(r.numTrips);
    $('#velocity').val(r.velocity);
    $('#velocitySlider').slider("value", r.velocity);
    $('#slideSize').val(r.slide);
    $('#slideSizeSlider').slider("value", r.slide);
}

google.maps.event.addDomListener(window, 'load', initialize);

