var source = new EventSource('/tweets');

var worldmap = new Datamap({
    scope: 'world',
    title: 'Sentiment',
    projection: 'equirectangular',
    element: document.getElementById("worldmap"),
    geographyConfig: {
        popupOnHover: false,
        highlightOnHover: false
    },
    bubblesConfig: {
        radius: 7,
        exitDelay: 30000 // Milliseconds
    },
    responsive: true,
    done: function(datamap) {
        datamap.svg.call(d3.behavior.zoom().on("zoom", redraw));
        //$("#resetZoom").on("click", function(){ resetZoom(); })
        function redraw() {
            datamap.svg.selectAll("g").attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        }

        function resetZoom() {
            datamap.svg.selectAll("g").attr("transform", "translate(0,0)scale(1.0)");
        }
    },
    fills: {
        defaultFill: '#5d91ff',
        "0": 'blue',
        "-1": 'red',
        "1": 'green'
    }
});

d3.select(window).on('resize', function() {
    worldmap.resize();
});

function determineColor(sentiment) {
    return sentiment == 0 ? "blue" : (sentiment == -1 ? "red" : "green");
}

function determineEmoji(sentiment) {
    return sentiment == 0 ? "&#x1F44C;" : (sentiment == -1 ? "&#x1F44E;" : "&#128077;");
}

var displayTweet = function (geo, data) {
    var tip = "<div><h3><span style='vertical-align:middle'>@" + data.name + '</span><img style="vertical-align:middle" height="70" width="70" src="' + data.pic + '"></h3></div>';
    tip += "<h6>" + data.date + "</h6>";
    tip += "<h4>" + data.text + "</h4>";
    return "<div class='hoverinfo tooltip'>" + tip + '</div>';
}


function updateFeed(predictedStatus) {
    var tweetLayout = "";
    switch (predictedStatus.sentiment) {
        case -1:
            tweetLayout =getLayout("glyphicon-thumbs-down", "danger", predictedStatus.status.user.name, predictedStatus.status.text);
            break;
        case 0:
            tweetLayout = getLayout("glyphicon-globe", "warning", predictedStatus.status.user.name, predictedStatus.status.text);
            break;
        case 1:
            tweetLayout = getLayout("glyphicon-thumbs-up", "success", predictedStatus.status.user.name, predictedStatus.status.text);
            break;
    }

    var feed = $("#feed");
    if (feed.children().length > 4)
        feed.children().last().remove();
    feed.prepend(tweetLayout);
}

function getLayout(icon, sentimentStyle, username, tweet) {
    return '<li class="list-group-item list-group-item-' + sentimentStyle + '">' +
    "<h4>" +
    '<span class="glyphicon ' + icon + '" aria-hidden="true" style="font-size: 20px; float:left; padding-right: 10px;"></span>' +
    username + "</h4>" +
    "<p>" + tweet + "</p>" +
    "</li>";
}

source.onmessage = function(event) {
    console.log(event.data);
    var status = JSON.parse(event.data);
    console.log(status);
    updateFeed(status);
};