<?php

require_once 'config.php';

require_once 'format_dbf_response.php';

require_once 'format_dbf_request.php';

require_once 'FormatXmlRequest.php';

require_once 'FormatXmlResponse.php';

require_once 'export_import.php';

require_once 'db_connection.php';

require_once 'dummy_connection.php';

class Main
{

	public static function createRequest()

	{
		$config = INI::read('config.ini');

		$dir_name = $config['application']['request']['directory'] . DS . date("d-m-Y");
		if (!file_exists($dir_name))
		{
			if (!mkdir($dir_name))
			{
				die("Unable to create directory where to place request files");
			}
		}
		else if (!is_writable($dir_name))
		{
			die("The directory is not writable. Stopping");
		}


		//print_r($config);

		$files = $config['format']['file'];

		include 'osp.php';

		$db = new DbConnection();

		echo "Starting" . PHP_EOL;

		foreach ($options as $osp)
		{
			try
			{
				foreach ($files as $file)
				{
					echo iconv("utf-8", "cp866", $osp['name']), " starting".PHP_EOL;
					if (!defined("OSP_CODE"))
						define("OSP_CODE", $osp['code']);
					if (!defined("DATE"))
						define("DATE", date("dmY"));
					if ($db->connect($osp)) {

						$format_config = INI::read($file);
						$filename = $dir_name .DS. $format_config['common']['filename'];

						ExportImport::readRequest($db, $format_config, $filename);
					} else {
						echo "Connection failed. Continuing" . PHP_EOL;
					}
				}
			}
			catch (Exception $e)
			{
				echo "File: " 		. $e->getFile() 	. PHP_EOL
				   . "Line: " 		. $e->getLine() 	. PHP_EOL
				   . "Message: " 	. $e->getMessage()	. PHP_EOL;
				continue;
			}
		}
		echo "Finished" . PHP_EOL;

	}

	public static function responseReadWrite()
	{
		include 'osp.php';
		$config = INI::read('config.ini');
		$dir = $config['application']['response']['directory'];
		$configfiles = $config['format']['file'];
		$respfiles = self::readDirContent($dir);
		$notdone = array();
		$db = new DbConnection();
		echo "Starting" . PHP_EOL;
		foreach ($respfiles as $file)
		{
			try
			{
				foreach ($configfiles as $inifile)
				{
					$conf = INI::read($inifile);
					if (preg_match($conf['common']['format'][0]['mask'], $file['filename'], $matches)) {
						echo PHP_EOL . iconv("utf-8", "cp866", "Файл от " . $conf['common']['agent_code']) . PHP_EOL;
						if ($matches[1] >= 50)
							$osp_code = $matches[1] - 48;
						else
							$osp_code = $matches[1];

						$osp = $options[$osp_code];

						echo PHP_EOL . iconv("utf-8", "cp866", "ОСП " . $osp['name'])  . PHP_EOL;

						if ($db->connect($osp)) {
							if (ExportImport::writeResponse($db, $conf, $file['dirname'] .DS. $file['filename']))
								echo "Moving file to uploaded dir" . PHP_EOL;
								rename($file['fullname'], $file['dirname'] . DS . "uploaded" . DS . $file['filename'] . date("_Ydm_Hi"));
						} else {
							echo "Connection failed. Continuing" . PHP_EOL;
						}
					} else {
						continue;
					}
				}
			}
			catch (Exception $e)
			{
				echo PHP_EOL
				   . "File: " 		. $e->getFile() 	. PHP_EOL
				   . "Line: " 		. $e->getLine() 	. PHP_EOL
				   . "Message: " 	. $e->getMessage()	. PHP_EOL;
				continue;
			}
		}
		echo "Finished" . PHP_EOL;

	}

	public static function readDirContent($dir)
	{
		$result = array();
		$dirIter = new DirectoryIterator($dir);
		foreach ($dirIter as $file)
		{
			if ($file->isFile())
			{
				$result[] = array('filename' => $file->getFilename(),
							      'dirname' => $file->getPath(),
							      'fullname' => $file->getPathname());
			}
		}
		return $result;
	}

	public static function parseArgs()
	{
		$options = parseParameters();
		$request = isset($options['z']) || isset($options['request']);
		$response = isset($options['r']) || isset($options['response']);
		if ($response && $request)
		{
			self::createRequest();
			self::responseReadWrite();
		}
		else if ($request)
		{
			self::createRequest();
		}
		else if ($response)
		{
			self::responseReadWrite();
		}
		else
		{
			echo "Valid parameters are:" . PHP_EOL
				."-r, --response - proccess response" . PHP_EOL
				."-z, --request - create request" . PHP_EOL;
		}
	}

}

function parseParameters($noopt = array())
{
    $result = array();
    $params = $GLOBALS['argv'];
    reset($params);
    while (list($tmp, $p) = each($params))
    {
        if ($p{0} == '-')
        {
            $pname = substr($p, 1);
            $value = true;
            if ($pname{0} == '-')
            {
                // long-opt (--<param>)
                $pname = substr($pname, 1);
                if (strpos($p, '=') !== false)
                {
                    // value specified inline (--<param>=<value>)
                    list($pname, $value) = explode('=', substr($p, 2), 2);
                }
            }
            // check if next parameter is a descriptor or a value
            $nextparm = current($params);
            if (!in_array($pname, $noopt) && $value === true && $nextparm !== false && $nextparm{0} != '-')
            	list($tmp, $value) = each($params);
            $result[$pname] = $value;
        }
        else
        {
            // param doesn't belong to any option
            $result[] = $p;
        }
    }
    return $result;
}

defined('APPLICATION_PATH')
    || define('APPLICATION_PATH', realpath(dirname(__FILE__)));

defined('DS')
	|| define('DS', DIRECTORY_SEPARATOR);


Main::parseArgs();
