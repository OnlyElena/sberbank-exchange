<?php

$connection = ibase_connect("localhost/3052:ncore-fssp", "SYSDBA", "masterkey");
var_dump((boolean) $connection);
