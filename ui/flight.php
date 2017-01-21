<?php
require_once "./libs/flight/Flight.php";


spl_autoload_register(function ($class) {

    // base directory for the namespace prefix
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


Flight::register('view', 'League\Plates\Engine', array('./app/templates'));

Flight::map('render', function($template, $data){
    echo Flight::view()->render($template, $data);
});

Flight::route('/*', function(){
    // Render a template
    Flight::render('profile', ['name' => 'Tibo']);
});

Flight::start();
