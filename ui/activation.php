<!doctype html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Activation logic</title>
    <!-- CSS -->
    <link rel="stylesheet" type="text/css" href="css/stylesheet.css">
    <link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
</head>
<body>
    <!-- Js -->
    <script src="js/libs/jquery-3.1.1.min.js"></script>
    <script src="js/libs/bootstrap.min.js"></script>
    <script src="js/libs/viz.js"></script>

    <div class="container-fluid" id="container">
        <div id="activation-graph"></div>
    </div>

    <script>
        function draw(data) {

            var links = []; // dict = {source: , target: , value: ,}
            for (var n = 0; n < data.length; n++) {
                var activation = data[n];
                var trigger_label = activation.trigger_label;
                var label = activation.label;
                var class_name = activation.class_name;
                links.push({"trigger": trigger_label, "label": label, "class": class_name});
            }

            var graphsrc = 'digraph G { node [fontsize=10, shape=box]; ';
            links.forEach(function(link){
                console.log(link);
                graphsrc += '"' + link.trigger + '" -> "' + link.label + '"; ';
                graphsrc += '"' + link.label +  '" [label="' + link.class + '\n' + link.label + '"]; ';
            });

            graphsrc += '}';

            console.log(graphsrc);

            var parser = new DOMParser();
            var result = Viz(graphsrc);
            var graph = document.querySelector("#activation-graph");
            var svg = parser.parseFromString(result, "image/svg+xml");
            graph.appendChild(svg.documentElement);
        }

        var json_request_body = {"jsonrpc": "2.0",
            "method": "status",
            "params": {},
            "id": 123
        };

        var json_request_url = "http://127.0.0.1:8080";

        var request = new XMLHttpRequest();
        request.open('POST', json_request_url, true);
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
        request.addEventListener('load', function() {

            if (request.readyState == 4 && request.status == 200) {
                // Request succeeded => draw graph
                var json_response = JSON.parse(request.responseText);
                draw(json_response.result.activation);
            } else {

            }
        });

        request.send(JSON.stringify(json_request_body));

    </script>
</body>
