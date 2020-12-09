var hash = document.location.hash.split("/");

if (hash.length !== 2) {
    alert("Specify URI with a topic and username. Example http://localhost:8080#/stuff/bob")
}

var webSocket = new WebSocket("ws://localhost:8080/ws/order/" + hash[1]);
webSocket.onmessage = function (msg) { updateChat(msg); };
webSocket.onclose = function () { alert("WebSocket connection closed") };


//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
        id("message").value = "";
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    console.log("message", msg);
    insert("messaje", msg.data);
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", "<p>" + message + "</p>");
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}