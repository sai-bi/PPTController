/**
 * @overview
 *
 * @author 
 * @version 2014/03/15
 *
 */
function createCORSRequest(method, url) {
    var xhr = new XMLHttpRequest();
    if ("withCredentials" in xhr) {

        // Check if the XMLHttpRequest object has a "withCredentials" property.
        // "withCredentials" only exists on XMLHTTPRequest2 objects.
        xhr.open(method, url, true);

    } else if (typeof XDomainRequest != "undefined") {

        // Otherwise, check if XDomainRequest.
        // XDomainRequest only exists in IE, and is IE's way of making CORS requests.
        xhr = new XDomainRequest();
        xhr.open(method, url);

    } else {

        // Otherwise, CORS is not supported by the browser.
        xhr = null;

    }
    return xhr;
}


$(document).on("pageinit","#homepage",function(){
    $("#preButton").on("tap",function(){
        var url="http://147.8.241.82:5000";
        var xhr = createCORSRequest('POST', url);
        if (!xhr) {
            throw new Error('CORS not supported');
        }
        // xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
        xhr.withCredentials = true;
        xhr.send("0");
        xhr.onreadystatechange = function(){
            if (xhr.readyState == 4 && xhr.status ==200){
                console.log(xhr.responseText);
            }
        }     
    });
    $("#nextButton").on("tap",function(){
        var url="http://147.8.241.82:5000";
        var xhr = createCORSRequest('POST', url);
        if (!xhr) {
            throw new Error('CORS not supported');
        }
        // xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
        xhr.withCredentials = true;
        xhr.send("1");
        xhr.onreadystatechange = function(){
            if (xhr.readyState == 4 && xhr.status ==200){
                console.log(xhr.responseText);
            }
        }     
    });
});
