<?php
$this->layout('template', ['title' => 'Home']);

/*
import netrank.LinkAdapter;
$linkadapter = new LinkAdapter();
echo $linkadapter->getInstance()->getClass();
*/

$agents = array("detection.readwrite", "detection.dummy");
$agent = $agents[0];

if(isset($_GET["agent"])) {
  $agent = $_GET["agent"];
}

require_once "MarkClient.php";
$mark = new MarkClient();
?>

<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d H:i:s", time()) ?></p>

<form style="margin-bottom: 10px" method="GET" class="form-inline">
  <div class="form-group">
    <label for="agent">Detection agent: </label>
    <select class="form-control" id="agent" name="agent">
      <?php foreach ($agents as $cur_agent) : ?>
      <option <?= ($agent == $cur_agent) ? "selected" : "" ?> ><?= $cur_agent ?></option>
      <?php endforeach ?>
    </select>
  </div>
  <button type="submit" class="btn btn-primary">Apply</button>
</form>

<?php
$evidences = $mark->findEvidence($agent);
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
