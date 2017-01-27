<?php
$this->layout('template', ['title' => 'Status']);

require_once "MarkClient.php";
$mark = new MarkClient();
$state = $mark->status();
?>

<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d", time()) ?></p>
<p>Server state: <?= $state->state ?></p>

<h2>Ignite cluster status</h2>

<?php
$igniteStatus = $mark->igniteStatus();
?>

<p>Nodes: <?= $igniteStatus->totalNodes ?></p>
<p>CPUS: <?= $igniteStatus->totalCpus ?></p>
<p>Average CPU load: <?= round(100 * $igniteStatus->averageCpuLoad, 2) ?>%</p>

<h3>Job statistics</h3>
<p>Current job wait time: <?= $igniteStatus->currentJobWaitTime ?></p>
<p>Average job execute time: <?= round($igniteStatus->averageJobExecuteTime/1000, 2) ?> second</p>
<p>Current active jobs: <?= $igniteStatus->currentActiveJobs ?></p>
<p>Maximum waiting jobs: <?= $igniteStatus->maximumWaitingJobs ?></p>
<p>Current waiting jobs: <?= $igniteStatus->currentWaitingJobs ?></p>
<p>Total executed tasks: <?= $igniteStatus->totalExecutedTasks ?></p>


<h2>Activation cascade</h2>
<div id="activation-graph"></div>

<h2>Server status dump</h2>
<pre>
<?php var_dump($state) ?>
</pre>

<script src="js/libs/viz.js"></script>
<script>
    var mark_url = "<?= $mark->url() ?>";
    var json_request_body = {"jsonrpc": "2.0",
        "method": "status",
        "params": {},
        "id": 123
    };

    var request = new XMLHttpRequest();
    request.open('POST', mark_url, true);
    request.setRequestHeader(
            'Content-Type',
            'application/x-www-form-urlencoded; charset=UTF-8');
    request.addEventListener('load', function() {
        if (request.readyState == 4 && request.status == 200) {
            // Request succeeded => draw graph
            var json_response = JSON.parse(request.responseText);
            draw(json_response.result.activation);
        } else {

        }
    });

    request.send(JSON.stringify(json_request_body));

    function draw(detection_agents) {

        // each detection agent has following attributes:
        // - label
        // - trigger_label
        // - class_name

        // Describe the graph using graphviz dot notation
        // 1. Add the nodes (the agents)
        var graph_src = 'digraph G { rankdir=LR; node [fontsize=13, shape=box]; ';
        detection_agents.forEach(function(detection_agent){
            console.log(detection_agent);
            graph_src += '"' + detection_agent.label +  '" [label="'
                    + detection_agent.class_name + '\n'
                    + detection_agent.label + '"]; ';
        });

        // 2. Add the edges
        detection_agents.forEach(function(detection_agent){
            var matches = 0;
            detection_agents.forEach(function(other){
                if (other.label.startsWith(detection_agent.trigger_label)) {
                    graph_src += '"' + other.label + '" -> "'
                        + detection_agent.label + '"; ';
                    matches++;
                }
            });

            // We found no agent that will trigger this detection agent
            // => must be a data agent
            // => draw manually...
            if (matches === 0) {
               graph_src += '"' + detection_agent.trigger_label + '" -> "'
                    + detection_agent.label + '"; ';
            }

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

</script>
