<?php
error_reporting(E_ALL);
ini_set('display_errors','On');

require_once "MarkClient.php";
$client = new MarkClient();
?>

<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d", time()) ?></p>

<?php
$state = $client->status();
?>

<p>Server state: <?= $state->state ?></p>

<pre>
<?php var_dump($state) ?>
</pre>

