<?php

require_once 'response.php';

class FormatDBFResponse
{

	private $definition;

	private $common;

	private $dbf; //указатель на DBF файл

	private $current = 0; //для считывания по одной записи

	private $records = 0; //количество записей в файле

	private $file; //имя файла

	private $infoHeader;

	public function __construct($config = array(), $file)
	{
		$this->common 		= $config['common'];
		$this->definition 	= $config['response'];
		$this->request 		= $config['matches'];
		if ($file)
		{
			$this->setFile($file);
		}
	}

	public function setFile($file)
	{
		if (is_file($file))
		{
			$this->file = $file;

			$this->dbf = dbase_open($this->file, 0);
			if ($this->dbf)
			{
				$this->records = dbase_numrecords($this->dbf);
				$this->infoHeader = dbase_get_header_info($this->dbf);
				foreach ($this->infoHeader as $header)
				{
					$header['type'] = strtoupper(substr($header['type'], 0, 1));
					unset($header['format']);
					unset($header['offset']);
				}
				$diff1 = array_diff($this->infoHeader, $this->definition);
				$diff2 = array_diff($this->definition, $this->infoHeader);
				if (empty($diff1) && empty($diff2))
					return;
				else
					throw new Exception("File has wrong format");
			}
			else
			{
				throw new Exception("Can't open file");
			}
		}
		else
		{
			throw new Exception("This is not a file " . $file);
			return false;
		}
	}

	public function saveToDb($db)
	{
		$db->beginTransaction();
		foreach ($this->readAll() as $response)
		{
			try {
				$genid = $db->fetchOne("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB\$DATABASE");
				$genid = $genid['GEN_ID'];
				$genuuid = $db->fetchOne("SELECT GEN_UUID() FROM RDB\$DATABASE");
				$genuuid = $genuuid['GEN_UUID'];

				$response->RESULT = ($response->RESULT == 1) ? iconv('utf-8', 'cp1251', 'Сведений нет') : iconv('utf-8', 'cp1251', 'Имеются сведения (приложены)');
				//$response->RESULT .= "\r\n" . iconv('cp866', 'cp1251', $response->TEXT);

				$response->ID_ZAPR = "81" . $db->code . $response->ID_ZAPR;

				$response->DTRES = substr($response->DTRES, 6, 2) . "." . substr($response->DTRES, 4, 2) . "." . substr($response->DTRES, 0, 4);

				$fields = array();

				foreach ($this->request['fields'] as $field)
				{
					$fields[] = $field['match'];
				}
				$fields = implode(", ", $fields);

				$query = "SELECT " . $fields . ", ID_DBTR_BORN, EXT_REQUEST.*, DBTR_BORN_YEAR FROM "
		    			."DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID "
		    			."INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID "
		    			."INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID "
		    			."INNER JOIN NSI_COUNTERPARTY_CLASS ON DOC_IP_DOC.ID_DBTR_ENTID = NSI_COUNTERPARTY_CLASS.NCC_ID "
		    			."WHERE REQ_ID = {$response->ID_ZAPR}";

		    	$require = $db->fetchOne($query);

		    	$require['DBTR_BORN_YEAR'] = $require['DBTR_BORN_YEAR'] ? $require['DBTR_BORN_YEAR'] : date("Y", $require['DATER']);
		    	$require['DATER'] = date("d.m.Y", strtotime($require['ID_DBTR_BORN']));
		    	//var_dump($require);

				$query1 = "INSERT INTO ".
							"EXT_INPUT_HEADER " .
							"(ID," .
							"PACK_NUMBER," .
							"PROCEED," .
							"AGENT_CODE," .
							"AGENT_DEPT_CODE," .
							"AGENT_AGREEMENT_CODE," .
							"EXTERNAL_KEY," .
							"METAOBJECTNAME," .
							"DATE_IMPORT," .
							"SOURCE_BARCODE" .
							") VALUES (" .
							"?," .
							"?," .
							"0," .
							"?," .
							"?," .
							"?," .
							"?," .
							"'EXT_RESPONSE'," .
							"CAST('NOW' AS DATE)," .
							"''" .
							")";

				$db->query(
						$query1,
						$genid,
						$require['PACK_NUMBER'],
						iconv('utf-8', 'cp1251', $this->common['agent_code']),
						iconv('utf-8', 'cp1251', $this->common['agent_dept_code']),
						iconv('utf-8', 'cp1251', $this->common['agreement_code']),
						$genuuid);
				$query2 = "INSERT INTO " .
						"EXT_RESPONSE (" .
							"ID," .
							"RESPONSE_DATE," .
							"ENTITY_NAME," .
							"ENTITY_BIRTHYEAR," .
							"ENTITY_BIRTHDATE," .
							"ENTITY_INN," .
							"ID_NUM," .
							"IP_NUM," .
							"REQUEST_NUM," .
							"REQUEST_ID," .
							"DATA_STR" .
						") VALUES (" .
							"{$genid}," .
							"'{$response->DTRES}'," .
							"'{$require['FIOORG']}'," .
							"{$require['DBTR_BORN_YEAR']}," .
							"'{$require['DATER']}'," .
							"'{$require['INN']}'," .
							"'{$require['ID_NUMBER']}'," .
							"'{$require['NUMIP']}'," .
							"'{$require['REQ_NUMBER']}'," .
							"{$response->ID_ZAPR}," .
							"'{$response->RESULT}'" .
						");";

				$db->query($query2);

				$text = $response->TEXT;

				$text = explode(";", $text);

				$accounts = array();

				foreach ($text as $acc)
				{
					if (strlen(trim($acc)))
					{
						preg_match("/^[^0-9]*(\d{10,})\s.*$/i", $acc, $m);
						$accnum = $m[1];
						$accounts[$accnum] = trim($acc);
					}
				}

				//var_dump($accounts);

				foreach ($accounts as $accnum => $accinfo)
				{

					$extinfoid = $db->fetchOne("SELECT NEXT VALUE FOR EXT_INFORMATION FROM RDB\$DATABASE");
					$extinfoid = $extinfoid['GEN_ID'];

					//$extkey = $db->fetchOne("SELECT GEN_UUID() FROM RDB\$DATABASE");
					//$extkey = $extkey['GEN_UUID'];

					$query3 = "INSERT INTO " . PHP_EOL .
							"EXT_INFORMATION (" . PHP_EOL .
								"ID," .PHP_EOL .
								"ACT_DATE," .PHP_EOL .
								"KIND_DATA_TYPE," .PHP_EOL .
								"ENTITY_NAME," .PHP_EOL .
								"EXTERNAL_KEY," .PHP_EOL .
								"ENTITY_BIRTHDATE," .PHP_EOL .
								"ENTITY_BIRTHYEAR," .PHP_EOL .
								"PROCEED," .PHP_EOL .
								"DOCUMENT_KEY," .PHP_EOL .
								"ENTITY_INN" .PHP_EOL .
							") VALUES (" .PHP_EOL .
								"{$extinfoid}," .PHP_EOL .
								"'{$response->DTRES}'," .PHP_EOL .
								"'09'," .PHP_EOL .
								"'{$require['FIOORG']}'," .PHP_EOL .
								"?," .PHP_EOL .
								"'{$require['DATER']}'," .PHP_EOL .
								"{$require['DBTR_BORN_YEAR']}," .PHP_EOL .
								"0," .PHP_EOL .
								"?," . PHP_EOL .
								"'{$require['INN']}'" .PHP_EOL .
							")";

					$db->query($query3, $genuuid, $genuuid);

					$bik = isset($this->common['bik']) ? $this->common['bik'] : $response->BIK;
					$cur_info = isset($response->CURRENCY) ? $response->CURRENCY : '';
					$bank_name = isset($this->common['bank_name']) ? iconv("utf-8", "cp1251", $this->common['bank_name']) : '';
					$summa = isset($response->SUMMA) ? $response->SUMMA : 0;
					$dept_code = isset($response->DEPT_CODE) ? $response->DEPT_CODE : NULL;
					$acc = iconv('cp866', 'cp1251', $accinfo);

					$query4 = "INSERT INTO " .PHP_EOL .
							"EXT_AVAILABILITY_ACC_DATA (" .PHP_EOL .
								"ID," .PHP_EOL .
								"BIC_BANK," .PHP_EOL .
								"CURRENCY_CODE," .PHP_EOL .
								"ACC," .PHP_EOL .
								"BANK_NAME," .PHP_EOL .
								"SUMMA," .PHP_EOL .
								"DEPT_CODE," .PHP_EOL .
								"SUMMA_INFO" .PHP_EOL .
							") VALUES (" .PHP_EOL .
							"{$extinfoid}," .PHP_EOL .
							"'{$bik}'," .PHP_EOL .
							"'{$cur_info}'," .PHP_EOL .
							"'{$accnum}'," .PHP_EOL .
							"'{$bank_name}'," .PHP_EOL .
							"{$summa}," .PHP_EOL .
							"'{$dept_code}'," .PHP_EOL .
							"'{$acc}'" .PHP_EOL .
							")";

					$db->query($query4);

				}
			} catch (Exception $e) {
				$db->rollback();
				echo "File: " 		. $e->getFile() 	. PHP_EOL
				   . "Line: " 		. $e->getLine() 	. PHP_EOL
				   . "Message: " 	. $e->getMessage()	. PHP_EOL;
				exit;
			}
		}
		$db->commit();
	}

	/*
	 * читает данные из файла ответа и возвращает в виде массива
	 *
	 * */
	public function readAll()
	{
		$result = array();
		for ($i = 1; $i <= $this->records; $i++)
		{
			$response = new Response();
			$row = dbase_get_record($this->dbf, $i);
			for ($k = 0; $k < count($this->infoHeader); $k++)
			{
				//echo $infoHeader[$k]['name'] . " = " . $row[$k] . PHP_EOL;
				$name = $this->infoHeader[$k]['name'];
				$response->$name = $row[$k];
			}
			$result[] = $response;
		}
		return $result;
	}

	/*
	 * return Response $response
	 */
	public function readNext()
	{
		if ($this->current >= $this->records || $this->current < 0)
		{
			return false;
		}
		$response = new Response();
		$this->current++;
		$row = dbase_get_record($this->dbf, $this->current);
		for ($k = 0; $k < count($this->infoHeader); $k++)
		{
			//echo $infoHeader[$k]['name'] . " = " . $row[$k] . PHP_EOL;
			$name = $this->infoHeader[$k]['name'];
			$response->$name = $row[$k];
		}
		return $response;
	}

	public function readCurrent()
	{
		$response = new Response();
		$row = dbase_get_record($this->dbf, $this->current);
		for ($k = 0; $k < count($this->infoHeader); $k++)
		{
			//echo $this->infoHeader[$k]['name'] . " = " . $row[$k] . PHP_EOL;
			$name = $this->infoHeader[$k]['name'];
			$response->$name = $row[$k];
		}
		return $response;
	}

	public function readPrevious()
	{
		if ($this->current < 1)
			return false;
		$response = new Response();
		$this->current--;
		$row = dbase_get_record($this->dbf, $this->current);
		for ($k = 0; $k < count($this->infoHeader); $k++)
		{
			//echo $infoHeader[$k]['name'] . " = " . $row[$k] . PHP_EOL;
			$name = $this->infoHeader[$k]['name'];
			$response->$name = $row[$k];
		}
		return $response;
	}

	public function moveNext()
	{
		if ($this->current >= $this->records || $this->current < 0)
			return false;
		$this->current++;
		return true;
	}

	public function movePrevious()
	{
		if ($this->current <= 0 || $this->current > $this->records)
			return false;
		$this->current--;
		return true;
	}

	public function moveLast()
	{
		$this->current = $this->records;
		return true;
	}

	public function moveFirst()
	{
		$this->current = 0;
		return true;
	}

}