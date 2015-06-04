<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="tweetstreamer.TweetStreamer"%>

<%
    // Start Twitter streamer upon running.
    TweetStreamer.main(new String[] {});
%>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta http-equiv="Content-type" content="text/html; charset=utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>-- Now I See You, Twitters! --</title>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
        <link rel="stylesheet" href="styles/style.css">
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
        <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization"></script>
        <script src="https://www.google.com/jsapi"></script>
    </head>
    <body>
        <div class="container">
            <div class="page-header">
                <h1>Now I See You, Twitters!</h1>
            </div>
            <form class="navbar-form navbar-left" role="search">
                <div class="form-group">
                    <input type="text" class="form-control" id="keyword" name="keyword" placeholder="Keyword">
                    <select class="form-control" id="language" name="language">
                        <option value="default">Language</option>
                        <option value="fr">French</option>
                        <option value="en">English</option>
                        <option value="ar">Arabic</option>
                        <option value="ja">Japanese</option>
                        <option value="es">Spanish</option>
                        <option value="de">German</option>
                        <option value="it">Italian</option>
                        <option value="id">Indonesian</option>
                        <option value="pt">Portuguese</option>
                        <option value="ko">Korean</option>
                        <option value="tr">Turkish</option>
                        <option value="ru">Russian</option>
                        <option value="nl">Dutch</option>
                        <option value="fil">Filipino</option>
                        <option value="msa">Malay</option>
                        <option value="zh-tw">Traditional Chinese</option>
                        <option value="zh-cn">Simplified Chinese</option>
                        <option value="hi">Hindi</option>
                        <option value="no">Norwegian</option>
                        <option value="sv">Swedish</option>
                        <option value="fi">Finnish</option>
                        <option value="da">Danish</option>
                        <option value="pl">Polish</option>
                        <option value="hu">Hungarian</option>
                        <option value="fa">Persian</option>
                        <option value="he">Hebrew</option>
                        <option value="th">Thai</option>
                        <option value="uk">Ukrainian</option>
                        <option value="cs">Czech</option>
                        <option value="ro">Romanian</option>
                        <option value="en-gb">British English</option>
                        <option value="vi">Vietnamese</option>
                        <option value="bn">Bengali</option>
                    </select>
                    <input type="text" class="form-control" id="startDate" placeholder="Start Date" onfocus="(this.type='date')" onblur="(this.type='text')">
                    <input type="text" class="form-control" id="endDate" placeholder="End Date" onfocus="(this.type='date')" onblur="(this.type='text')">
                </div>
                <button type="submit" class="btn btn-primary" id="input">Submit</button>
                <div class="alert alert-danger" role="alert" id="alert">Invalid Dates</div>
            </form>
            <div class="panel panel-success" id="chart-panel">
                <div class="panel-heading">
                    <h3 class="panel-title">Sentiment Trend</h3>
                </div>
                <div class="panel-body">
                    <div id="piechart"></div>
                </div>
            </div>
            <div class="panel panel-success" id="map-panel">
                <div class="panel-heading">
                    <h3 class="panel-title">Heat Map</h3>
                </div>
                <div class="panel-body">
                    <div id="map-canvas"></div>
                </div>
            </div>
        </div>
        <script>
            var timer, trend;
            var submit = document.getElementById("input");
            submit.addEventListener("click", function(event) {
                event.preventDefault();
                time();
                plot();
            });
            
            function areValidDates(startDate, endDate) {
                var today = new Date();
                var start = new Date(startDate);
                var end = new Date(endDate);
                if (startDate.length > 0) {
                    if (endDate.length > 0) {
                        return start < end && end < today;
                    }
                    return start < today;
                }
                if (endDate.length > 0) {
                    return end < today;
                }
                return true;
            }
            
            function reqListener() {
                var response = JSON.parse(this.responseText);
                if (response.status == 200) {
                    var positiveTweets = [], negativeTweets = [], countNeutral = 0;
                    response.tweets.forEach(function(tweet) {
                        var sentiment = parseFloat(tweet.sentiment);
                        if (sentiment > 0) {
                            positiveTweets.push(new google.maps.LatLng(tweet.latitude, tweet.longitude))
                        } else if (sentiment < 0) {
                            negativeTweets.push(new google.maps.LatLng(tweet.latitude, tweet.longitude))
                        } else {
                            countNeutral++;
                        }
                    });
                    trend = google.visualization.arrayToDataTable([
                        ['Sentiment', 'Number of Tweets'],
                        ['Positive', positiveTweets.length],
                        ['Negative', negativeTweets.length],
                        ['Neutral', countNeutral]
                    ]);
                    chart.draw(trend);
                    positive.setData(new google.maps.MVCArray(positiveTweets));
                    negative.setData(new google.maps.MVCArray(negativeTweets));
                } else {
                    console.log(response.message);
                }
            };

            function plot() {
                console.log(new Date());
                var keyword = document.getElementById("keyword").value;
                var language = document.getElementById("language").value;
                var startDate = document.getElementById("startDate").value;
                var endDate = document.getElementById("endDate").value;
                if (areValidDates(startDate, endDate)) {
                    document.getElementById("alert").style.display = "none";
                    var request = new XMLHttpRequest();
                    request.onload = reqListener;
                    request.open("GET", "Tweets?keyword=" + encodeURI(keyword)
                        + "&language=" + encodeURI(language)
                        + "&startDate=" + encodeURI(startDate)
                        + "&endDate=" + encodeURI(endDate), true);
                    request.send();
                } else {
                    document.getElementById("alert").style.display = "block";
                    trend = google.visualization.arrayToDataTable([
                        ['Sentiment', 'Number of Tweets'],
                        ['Positive', 0],
                        ['Negative', 0],
                        ['Neutral', 0]
                    ]);
                    chart.draw(trend);
                    positive.setData(new google.maps.MVCArray([]));
                    negative.setData(new google.maps.MVCArray([]));
                }
            };

            function time() {
                clearInterval(timer);
                timer = setInterval(function() {
                    plot();
                }, 15000);
            }

            time();
        </script>
        <script>
            var map, positive, negative;
            function initialize() {
                var mapOptions = {
                    zoom: 1,
                    center: new google.maps.LatLng(27.52, 34.34),
                    mapTypeId: google.maps.MapTypeId.SATELLITE
                };
                map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
                positive = new google.maps.visualization.HeatmapLayer();
                negative = new google.maps.visualization.HeatmapLayer();
                positive.setMap(map);
                negative.setMap(map);
                var gradient = [
                    'rgba(0, 255, 255, 0)',
                    'rgba(0, 255, 255, 1)',
                    'rgba(0, 191, 255, 1)',
                    'rgba(0, 127, 255, 1)',
                    'rgba(0, 63, 255, 1)',
                    'rgba(0, 0, 255, 1)',
                    'rgba(0, 0, 223, 1)',
                    'rgba(0, 0, 191, 1)',
                    'rgba(0, 0, 159, 1)',
                    'rgba(0, 0, 127, 1)',
                    'rgba(63, 0, 91, 1)',
                    'rgba(127, 0, 63, 1)',
                    'rgba(191, 0, 31, 1)',
                    'rgba(255, 0, 0, 1)'
                ]
                negative.set('gradient', negative.get('gradient') ? null : gradient);
            }
            google.maps.event.addDomListener(window, 'load', initialize);

            var chart;
            google.load("visualization", "1", {packages:["corechart"]});
            google.setOnLoadCallback(drawChart);
            
            function drawChart() {
                chart = new google.visualization.PieChart(document.getElementById("piechart"));
                plot();
            }
        </script>
        <footer class="footer">
            <div class="container">
                <p class="text-muted">Copyright © 2015 Yi Guo Programmed.</p>
            </div>
        </footer>
    </body>
</html>