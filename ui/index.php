<?php
error_reporting(E_ALL);
ini_set('display_errors', 'On');
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

$results  = json_decode($response)->result;

class Evidence {
  public $label;
  public $time;
  public $score;
  public $subject;
  public $report;

  public function __construct(stdClass $obj) {
    $this->label = $obj->label;
    $this->time = $obj->time;
    $this->score = $obj->score;
    $this->subject = new Link($obj->subject);
    $this->report = $obj->report;
  }
}

class Link {
  public $client;
  public $server;

  public function __construct(stdClass $obj) {
    $this->client = $obj->client;
    $this->server = $obj->server;
  }

  public function __toString() {
    return $this->client . " <=> " . $this->server;
  }
}

$evidences = array();
foreach ($results as $line) {
  $evidences[] = new Evidence($line);
}
$results = null;

usort($evidences, function(Evidence $e1, Evidence $e2){
  return $e1->score < $e2->score ? 1 : -1;
});

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

