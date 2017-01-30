<?php
$this->layout('template', ['title' => 'Home']);

/*
import netrank.LinkAdapter;
$linkadapter = new LinkAdapter();
echo $linkadapter->getInstance()->getClass();
*/

require_once "MarkClient.php";
$mark = new MarkClient();
?>

<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d H:i:s", time()) ?></p>

<?php
$evidences = $mark->findEvidence("detection.readwrite");
?>

<table class="table">
  <tr>
    <th>Subject</th>
    <th>Score</th>
    <th>Time</th>
  </tr>

  <?php foreach ($evidences as $evidence) : ?>
  <tr>
    <td>
      <a href="/report/<?= $evidence->id ?>">
        <?= $evidence->subject ?>
      </a>
    </td>
    <td><?= $evidence->score ?></td>
    <td><?= date("Y-m-d H:i:s", $evidence->time) ?></td>
  </tr>
  <?php endforeach; ?>
</table>
