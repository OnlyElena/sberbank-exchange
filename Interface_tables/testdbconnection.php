<?php

include 'dummy_connection.php';

require_once 'main.php';

$db = new DummyDbConnection($options[23]);

var_dump($db->fetchOne("SELECT NEXT VALUE FOR EXT_INFORMATION FROM RDB\$DATABASE"));

var_dump($db->fetchOne("SELECT GEN_UUID() FROM RDB\$DATABASE"));