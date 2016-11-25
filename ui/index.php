<?php
error_reporting(E_ALL);
ini_set('display_errors','On');
?>



<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d", time()) ?></p>


<?php
$curl = curl_init();



curl_setopt_array($curl, array(
  CURLOPT_PORT => "8080",
  CURLOPT_URL => "http://127.0.0.1:8080/",
  CURLOPT_RETURNTRANSFER => true,
  CURLOPT_ENCODING => "",
  CURLOPT_MAXREDIRS => 10,
  CURLOPT_TIMEOUT => 30,
  CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
  CURLOPT_CUSTOMREQUEST => "POST",
  CURLOPT_HTTPHEADER => array(
    "cache-control: no-cache",
    "content-type: application/json"
  ),
));

curl_setopt($curl, CURLOPT_POSTFIELDS, '{
    "jsonrpc": "2.0",
    "method": "status",
    "params": { },
    "id": 123
}');

$response = curl_exec($curl);
$err = curl_error($curl);

curl_close($curl);

if ($err) {
  echo "cURL Error #:" . $err;
} else {
  $state = json_decode($response)->result;
}
?>

<p>Server state: <?= $state->state ?></p>

<pre>
<?php var_dump($state) ?>
</pre>

