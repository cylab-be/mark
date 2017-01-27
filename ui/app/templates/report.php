<?php
// Show a single evidence report
$this->layout('template', ['title' => 'Evidence report ' . $evidence_id]);

require_once "MarkClient.php";
$mark = new MarkClient();
$evidence = $mark->findEvidenceById($evidence_id);

?>
<h1><?= $evidence->subject->client ?></h1>

<p>Evidence id: <?= $evidence->id ?></p>
<p>Label: <?= $evidence->label ?></p>
<p>Score: <?= $evidence->score ?></p>
<p>Time: <?= date("Y-m-d H:i:s", $evidence->time) ?> (<?= $evidence->time ?>)</p>
<p>Server: <?= $evidence->subject->server ?></p>

<h2>Report</h2>
<p><?= $evidence->report ?></p>

