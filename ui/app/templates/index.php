<?php
$this->layout('template', ['title' => 'Home']);

require_once "MarkClient.php";
$client = new MarkClient();
?>

<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d H:i:s", time()) ?></p>

<?php
$evidences = $client->findEvidence("detection.readwrite");
?>

<table class="table">
  <tr>
    <th>Subject</th>
    <th>Score</th>
    <th>Time</th>
  </tr>

  <?php foreach ($evidences as $evidence) : ?>
  <tr>
    <td><?= $evidence->subject ?></td>
    <td><?= $evidence->score ?></td>
    <td><?= date("Y-m-d H:i:s", $evidence->time) ?></td>
  </tr>
  <?php endforeach; ?>
</table>
