<?php

class MarkClient {
  private $url = "http://127.0.0.1:8080/";

  public function status() {
    return $this->exec("status", array());
  }

  public function url() {
    return $this->url;
  }

  /**
   *
   * @return String[]
   */
  public function getLabels() {
    $status = $this->status();
    $activation = $status->activation;

    $labels = array();
    foreach ($activation as $agent) {
      $labels[] = $agent->label;
    }

    return $labels;
  }

  /**
   *
   * @param String $label
   * @return \Evidence[]
   */
  public function findEvidence($label) {
    $results = $this->exec("findEvidence", array($label));

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

    $evidences = array();
    foreach ($results as $line) {
      $evidences[] = new Evidence($line);
    }

    usort($evidences, function(Evidence $e1, Evidence $e2) {
      return $e1->score < $e2->score ? 1 : -1;
    });

    return $evidences;
  }

  /**
   *
   * @param type $id
   * @return Evidence
   */
  public function findEvidenceById($id) {
    // current will return first element in the array
    return $this->exec("findEvidenceById", array($id));
  }

  public function igniteStatus() {
    return $this->exec("igniteStatus", array());
  }

  /**
   *
   * @param String $method
   * @param [] $params
   * @return type
   */
  private function exec($method, $params) {
    $curl = curl_init();
    curl_setopt_array($curl, array(
      //CURLOPT_PORT => "8080",
      CURLOPT_URL => $this->url,
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
        "method": "' . $method . '",
        "params": ' . json_encode($params) . ',
        "id": 123
    }');

    $response = curl_exec($curl);
    $err = curl_error($curl);

    curl_close($curl);

    if ($err) {
      echo "cURL Error #:" . $err;
      return;
    }

    return json_decode($response)->result;

  }
}

class Evidence {
  public $label;
  public $time;
  public $score;
  public $subject;
  public $report;
  public $id;

  public function __construct(stdClass $obj) {
    $this->id = $obj->id;
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