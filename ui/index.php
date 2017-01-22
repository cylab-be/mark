<?php
error_reporting(E_ALL);
ini_set('display_errors', 'On');

require_once "./libs/flight/Flight.php";

// Allows to load classes from the "libs" folder
spl_autoload_register(function ($class) {

    // base directory
    $base_dir = __DIR__ . '/libs/';

    // replace the namespace prefix with the base directory, replace namespace
    // separators with directory separators in the relative class name, append
    // with .php
    $file = $base_dir . str_replace('\\', '/', $class) . '.php';

    // if the file exists, require it
    if (file_exists($file)) {
        require $file;
    }
});

// Use Plates templates engine to render pages
Flight::register('view', 'League\Plates\Engine', array('./app/templates'));
Flight::map('render', function($template, $data){
    echo Flight::view()->render($template, $data);
});


// Define app routes
// http://flightphp.com/learn/#routing
Flight::route('/', function(){
    // Render a template
    Flight::render('index', []);
});

Flight::route('/status', function() {
    Flight::render('status', []);
});

// Start the framework and process the request...
Flight::start();
