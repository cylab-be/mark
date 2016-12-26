<?php
error_reporting(E_ALL);
ini_set('display_errors', 'On');

require_once "MarkClient.php";
$client = new MarkClient();
?>

<h1>Multi Agent Ranking Framework</h1>
<p><?php echo date("Y-m-d H:i:s", time()) ?></p>

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
    "method": "findEvidence",
    "params": ["detection.readwrite"],
    "id": 123
}');

$response = curl_exec($curl);
$err = curl_error($curl);

curl_close($curl);

if ($err) {
  echo "cURL Error #:" . $err;
  return;
}

$evidences  = $client->findEvidence("detection.readwrite");


/* result will look like
 * array(2052) {
  [0]=>
  object(stdClass) (5) {
    ["label"]=>
    string(19) "detection.readwrite"
    ["time"]=>
    int(1472083251)
    ["subject"]=>
    object(stdClass) (2) {
      ["client"]=>
      string(12) "198.36.158.8"
      ["server"]=>
      string(17) "ajdd.rygxzzaid.mk"
    }
    ["score"]=>
    float(0.6)
    ["report"]=>
    NULL
  }
 */
?>

<table>
<?php foreach ($evidences as $evidence) : ?>

  <tr>
    <td><?= $evidence->subject ?></td>
    <td><?= $evidence->score ?></td>
    <td><?= date("Y-m-d H:i:s", $evidence->time) ?></td>
  </tr>
<?php endforeach; ?>
</table>

