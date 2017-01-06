// jscs:disable
function draw_graph(json_data) {
	var data = json_data;
	var links = []; // dict = {source: , target: , value: ,}
	var target;
	for (var n = 0; n < data.length; n++) {
		var activation = data[n];
		var trigger_label = activation.trigger_label;
		var label = activation.label;
		var class_name = activation.class_name;
		links.push({"source": trigger_label, "target": label, "class": class_name});
	}

	var nodes = {};
		
	// Compute the distinct nodes from the links.
	links.forEach(function(link) {
		link.source = nodes[link.source] || 
			(nodes[link.source] = {name: link.source});
		link.target = nodes[link.target] || 
			(nodes[link.target] = {name: link.target, class: link.class});
	});

	console.log(nodes);
	console.log(links);

	var width = window.innerWidth; 
	var	height = window.innerHeight; 
	var height_panels = 150;
	var side_bar_height = document.getElementById('side_bar').clientHeight;
	var graph_width = document.getElementById('graph').clientWidth;

	var force = d3.layout.force()
		.nodes(d3.values(nodes))
		.links(links)
		.size([width / 2 , height/ 1.5])
		.linkDistance(300)
		.charge(-1000)
		.on("tick", tick)
		.start();

	// remove if anything was already drawn on the screen
	d3.select("body").select("#container").select("#parent").select("#graph").select("svg").remove();
	// draw new graph

	var svg = d3.select("body").select("#container").select("#parent").select("#graph").append("svg")
		.attr("width", graph_width)
		.attr("height", side_bar_height);

	// build the arrow.
	svg.append("svg:defs").selectAll("marker")
		.data(["end"])      // Different link/path types can be defined here
	.enter().append("svg:marker")    // This section adds in the arrows
		.attr("id", String)
		.attr("viewBox", "0 -5 10 10")
		.attr("refX", 13)
		.attr("refY", -1)
		.attr("fill", "black")
		.attr("markerWidth", 10)
		.attr("markerHeight", 6)
		.attr("orient", "auto")
	.append("svg:path")
		.attr("d", "M0,-5L10,0L0,5");
/*eslint-disable no-unused-vars*/
	// add the links and the arrows
	var path_index = 0;
	var path = svg.append("svg:g").selectAll("path")
		.data(force.links())
	.enter().append("svg:path")
		.attr("class", function(d) { return "link " + d.type; })
		.attr("class", "link")
		.attr("id", function(){
			path_index = path_index + 1;
			return path_index;
		} )
		.on("mouseover", function(d){
			var g = d3.select(this); // The node
			// The class is used to remove the additional text later
	//			if (d3.select(this).select('text.info')[0][0] == null){
			var info = g.append('text')
				.classed('info', true)
	//				.attr('x', 20)
	//				.attr('y', 10)
				.attr("font-size","30px")
				.append("textPath")
				.attr("xlink:href", function (d,i) {
					var path_id = g[0][0].id;
					return path_id; })
				.text(function(d) { 
					return d.value; });
		})
		.on("mouseout", function() {
		// Remove the info text on mouse out.
			//d3.select(this).select('text.info').remove();
		})
		.attr("marker-end", function(d) {if (d.value === 0){
											return "";
										} else {
											return "url(#end)";}});

	// define the nodes
	var node = svg.selectAll(".node")
		.data(force.nodes())
	.enter().append("g")
		.attr("class", "node")
		.on("click", function(d) {
			var g = d3.select(this); // The node
			// The class is used to remove the additional text later
			if (d3.select(this).select('text.info')[0][0] === null){
				var info = g.append('text')
					.classed('info', true)
					.attr('x', 0)
					.attr('y', -10)
					.attr("font-size","30px")
					.text(function(d) { console.log(d); });
			} else {
				d3.select(this).select('text.info').remove();	
			}
		})
	/*		.on("mouseout", function() {
			// Remove the info text on mouse out.
			d3.select(this).select('text.info').remove();
		})*/
		.call(force.drag);

	// add the nodes
	node.append("rect")
		.attr("x", 0)
		.attr("y", -10)
		.attr("width", function(d){
			return 300;})
		.attr("height", function(d){
			return 50;});
/*	node.append("circle")
		.attr("r", 5);*/
/*eslint-enable no-unused-vars*/
	// add the text 
	node.append("foreignObject")
		.attr("x", 12)
		.attr("dy", ".35em")
		.attr("font-size", "10xp")
		.html(function(d) { 
			var name;
			if (d.class){
				var first_line = "<font color=\"red\">Agent:</font>" + d.class;
				var second_line = "<font color=\"red\">Label:</font>" + d.name;
				name = first_line + "\n" + second_line;
			} else {
				name = "<font color=\"red\">Agent:</font>" + "\n" + "<font color=\"red\">Label:</font>" + d.name;
			}
			console.log(name);
			return name;
		});

	resize();
	d3.select(window).on("resize", resize);
	// add the curvy lines
	function tick() {
		path.attr("d", function(d) {
			var dx = d.target.x - d.source.x,
				dy = d.target.y - d.source.y,
				dr = Math.sqrt(dx * dx + dy * dy);
			return "M" + 
				d.source.x + "," + 
				d.source.y + "A" + 
				dr + "," + dr + " 0 0,1 " + 
				d.target.x + "," + 
				d.target.y;
		});

		node
			.attr("transform", function(d) { 
			return "translate(" + d.x + "," + d.y + ")"; });
	}

	function resize(){
		var graph_width = document.getElementById('graph').clientWidth;
		svg.attr("width", graph_width).attr("height", height - height_panels);
		force.size([width / 2, height / 1.5]).resume();
	}

}

// Returns a list of all nodes under the root.
function flatten(root) {
  var nodes = [], i = 0;

  function recurse(node) {
    if (node.children) node.children.forEach(recurse);
    if (!node.id) node.id = ++i;
    nodes.push(node);
  }

  recurse(root);
  return nodes;
}
