<?php

require_once 'request.php';

require_once 'response.php';

class ExportImport
{

	private $instance = null;

	private function __construct()
	{}

	public static function getInstance()
	{
		if ($this->instance == null)
			$this->instance = new ExportImport();

		return $this->instance;
	}

	/* берет новые данные из интерфейсных таблиц в соответствии с форматом */
	public static function readRequest($db, $config, $filename)
	{
		switch ($config['common']['format'][0]['name'])
		{
			case "dbf":
				$format = new FormatDBFRequest($config, $filename);
				break;
			case "xml":
				$format = new FormatXmlRequest($config, $filename);
				break;
			default:
				echo PHP_EOL. "Format is undefined" . PHP_EOL;
				return false;
		}
		$format->save($db);
	}

	public static function writeResponse($db, $config, $filename)
	{
		switch ($config['common']['format'][0]['name'])
		{
			case "dbf":
				$format = new FormatDBFResponse($config, $filename);
				break;
			case "xml":
				$format = new FormatXmlResponse($config, $filename);
				break;
			default:
				echo PHP_EOL. "Format is undefined" . PHP_EOL;
				return false;
		}

		echo "Importing to database" . PHP_EOL;

		return $format->saveToDb($db);

	}
}