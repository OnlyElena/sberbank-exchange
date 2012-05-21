<?php

require_once 'request.php';

class FormatDBFRequest
{

	private $common;

	private $definition;

	private $dbf;

	private $current = 0;

	private $records = 0;

	private $file = "";

	public function __construct($config = array(), $file)
	{
		$this->common = $config['common'];
		$this->definition = $config['request'];
		$this->matches = $config['matches'];
		$this->file = $file;
	}

	private function setFile($file)
	{
		$def = array();
		foreach ($this->definition['fields'] as $field)
		{
			//var_dump($field);
			switch ($field['type'])
			{
				case "N":
					$def[] = array($field['name'], $field['type'], $field['length'], $field['precision']);
					break;
				case "D":
					$def[] = array($field['name'], $field['type']);
					break;
				case "C":
					$def[] = array($field['name'], $field['type'], $field['length']);
					break;
				default:
					$def[] = array($field['name'], $field['type'], $field['length'], $field['precision']);
			}
		}
		$dbf = dbase_create($file, $def);
		if ($dbf)
		{
			$this->dbf = $dbf;
			$this->current = 0;
			$this->records = 0;
		}
		else
		{
			throw new Exception("Cannot create file " . $file);
			return false;
		}
		echo $file;
	}

	private function close()
	{
		return dbase_close($this->dbf);
	}

	private function write(Request $request)
	{
		$request->NUM = $request->NUM . "/" . $request->NUM_REQ_IN_PACK;
		unset($request->NUM_REQ_IN_PACK);
		$request->UNICODE = (int) substr($request->UNICODE, 4);
		$request->ID_ZAPR = (int) substr($request->ID_ZAPR, 4);
		$request->SUMM_IP = (float) $request->SUMM_IP;
		$data = array();
		foreach ($request->getData() as $k => $v)
		{
			$data[] = $v;
		}
		if (dbase_add_record($this->dbf, $data))
		{
			$this->records++;
			return true;
		}
		else
		{
			var_dump($data);
			throw new Exception("Fields doesn't match");
		}
		return false;
	}

	public function getNumrecords()
	{
		return $this->records;
	}

	public function readAll()
	{
		$result = array();

		if ($this->records == 0)
		{}
		else
		{
			for ($i = 1; $i <= $this->records; $i++)
			{
				$row = dbase_get_record($this->dbf, $i);
				$request = new Request();
				for ($k = 0; $k < count($this->definition); $k++)
				{
					$name = $this->definition[$k]['name'];
					$request->$name = $row[$k];
				}
				$result[] = $request;
			}
		}

		return $result;
	}

	public function save($db)
	{
		$fields = array();
		$result = array();
		$basename = explode(".", basename($this->file));
		$dirname = dirname($this->file);
		foreach ($this->matches['fields'] as $field)
		{
			//var_dump($field);
			$fields[] = $field['match'];
		}

		$where[] = "MVV_AGREEMENT_CODE = '".iconv("utf-8", "cp1251", $this->common['agreement_code'])."'";
		$where[] = "MVV_AGENT_CODE = '".iconv("utf-8", "cp1251", $this->common['agent_code'])."'";
		$where[] = "MVV_AGENT_DEPT_CODE = '".iconv("utf-8", "cp1251", $this->common['agent_dept_code'])."'";
		$where[] = "PROCESSED = 0";

		$where = " WHERE " . implode(' AND ', $where) . " ";

		$pack = "SELECT PACK_ID "
				."FROM EXT_REQUEST "
				.$where
				."GROUP BY PACK_ID";

		$packs = $db->fetchAll($pack);

		//var_dump($packs);

		$query = "SELECT " . implode(', ', $fields) . " "
				."FROM "
    			."DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID "
    			."INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID "
    			."INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID "
    			."INNER JOIN NSI_COUNTERPARTY_CLASS ON DOC_IP_DOC.ID_DBTR_ENTID = NSI_COUNTERPARTY_CLASS.NCC_ID "
    			.$where;

		$i = 0;

    	foreach ($packs as $pack)
    	{
    		$i++;

    		$file = $dirname .DS. $basename[0] . "-" . $i . "." . $basename[1];

    		$this->setFile($file);

    		$sql = $query . " AND PACK_ID = {$pack['PACK_ID']}";
			$res = $db->fetchOne($sql);
			$j = 0;
			while ($res)
			{
				$j++;
				$request = new Request();
				$request->NUM_REQ_IN_PACK = $j;
				foreach ($res as $k => $v)
				{
					$request->$k = $v;
				}
				$this->write($request);
				$res = $db->fetchOne();
			}
			$this->close();
			$db->beginTransaction();
			if ($db->query("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = {$pack['PACK_ID']}"))
			{
				$db->commit();
				//echo PHP_EOL . $db->rowsAffected() . " rows was affected by update" . PHP_EOL;
			}
			else
				$db->rollback();
    	}
		return true;
	}

}
