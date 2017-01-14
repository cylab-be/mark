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
        function draw(detection_agents) {

            // each detection agent has following attributes:
            // - label
            // - trigger_label
            // - class_name

            // Describe the graph using graphviz dot notation
            var graph_src = 'digraph G { node [fontsize=10, shape=box]; ';
            detection_agents.forEach(function(detection_agent){
                console.log(detection_agent);
                graph_src += '"' + detection_agent.trigger_label + '" -> "'
                        + detection_agent.label + '"; ';
                graph_src += '"' + detection_agent.label +  '" [label="'
                        + detection_agent.class_name + '\n'
                        + detection_agent.label + '"]; ';
            });

            graph_src += '}';

            console.log(graph_src);

            // let graphviz compute the graph representation
            var graph_graphviz = Viz(graph_src);

            // inject the graph svg in the page
            var parser = new DOMParser();
            var graph_svg = parser.parseFromString(graph_graphviz, "image/svg+xml");
            document.querySelector("#activation-graph")
                    .appendChild(graph_svg.documentElement);
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
