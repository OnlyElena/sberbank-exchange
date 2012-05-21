<?php

require_once 'response.php';

class FormatXmlResponse
{
	private $definition;

	private $common;

	private $xml; //указатель на XML файл

	private $current = 0; //для считывания по одной записи

	private $records = 0; //количество записей в файле

	private $file; //имя файла

	private $infoHeader;

	public function __construct($config = array(), $file = "")
	{
		$this->common 		= $config['common'];
		//$this->definition 	= $config['response'];
		//$this->request 		= $config['matches'];
		$this->xml = new XMLReader();
		if ($file !== "")
		{
			$this->setFile($file);
		}
	}

	public function setFile($file)
	{
		if (is_file($file))
		{
			$this->file = $file;

			if (!$this->xml->open($this->file))
			{
				throw new Exception("Can't open file");
				return false;
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
		foreach ($this->readAll() as $id => $response)
		{
			try {
				$genid = $db->fetchOne("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB\$DATABASE");
				$genid = $genid['GEN_ID'];
				$genuuid = $db->fetchOne("SELECT GEN_UUID() FROM RDB\$DATABASE");
				$genuuid = $genuuid['GEN_UUID'];

				//echo PHP_EOL. current($response)->Account . PHP_EOL;

				$result_str = (current($response)->Account == "") ? iconv('utf-8', 'cp1251', 'Сведений нет') : iconv('utf-8', 'cp1251', 'Имеются сведения (приложены)');

				$id = "81" . $db->code . $id;

				$req_date = current($response)->Req_Date;

				foreach ($response as $resp)
				{
					$resp->Req_Date = $resp->Req_Date
									? substr($resp->Req_Date, 8, 2) . "." . substr($resp->Req_Date, 5, 2) . "." . substr($resp->Req_Date, 0, 4)
									: null;
				}

				$query =
						"SELECT " .
							" REQ_ID," .
							" REQ_DATE," .
							" FIO_SPI," .
							" SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," .
							" IP_NUM," .
							" IP_SUM," .
							" ID_NUMBER," .
							" EXT_REQUEST.ID_DATE," .
							" DEBTOR_NAME," .
							" DBTR_BORN_YEAR," .
							" DEBTOR_ADDRESS," .
							" DEBTOR_BIRTHDATE," .
							" DEBTOR_BIRTHPLACE," .
							" PACK_NUMBER," .
							" DEBTOR_INN," .
							" REQ_NUMBER" .
						" FROM " .
						" DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID " .
						" INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID " .
						" INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID " .
						" INNER JOIN NSI_COUNTERPARTY_CLASS ON DOC_IP_DOC.ID_DBTR_ENTID = NSI_COUNTERPARTY_CLASS.NCC_ID " .
						" WHERE REQ_ID = {$id}";

		    	$require = $db->fetchOne($query);

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

				$require['DBTR_BORN_YEAR'] = $require['DBTR_BORN_YEAR'] ? $require['DBTR_BORN_YEAR'] : "NULL";
				$require['DEBTOR_BIRTHDATE'] = $require['DEBTOR_BIRTHDATE'] ? $require['DEBTOR_BIRTHDATE'] : NULL;

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
							"'{$req_date}'," .
							"'{$require['DEBTOR_NAME']}'," .
							"{$require['DBTR_BORN_YEAR']}," .
							"?," .
							"'{$require['DEBTOR_INN']}'," .
							"'{$require['ID_NUMBER']}'," .
							"'{$require['IP_NUM']}'," .
							"'{$require['REQ_NUMBER']}'," .
							"{$id}," .
							"'{$result_str}'" .
						");";

				$db->query($query2, $require['DEBTOR_BIRTHDATE']);

				echo "Saved response id ", $genid, " request id ", $id, PHP_EOL;

				$current = reset($response);

				if ($current->Account !== "") {

					foreach ($response as $account => $info) {

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
									"?," .PHP_EOL .
									"'09'," .PHP_EOL .
									"'{$require['DEBTOR_NAME']}'," .PHP_EOL .
									"?," .PHP_EOL .
									"?," .PHP_EOL .
									"{$require['DBTR_BORN_YEAR']}," .PHP_EOL .
									"0," .PHP_EOL .
									"?," . PHP_EOL .
									"'{$require['DEBTOR_INN']}'" .PHP_EOL .
								")";

						$db->query($query3, $info->Req_Date, $genuuid, $require['DEBTOR_BIRTHDATE'], $genuuid);

						$bic = $info->BIC;
						$cur_info = isset($info->CURRENCY) ? $info->CURRENCY : '';
						$bank_name = isset($this->common['bank_name']) ? iconv("utf-8", "cp1251", $this->common['bank_name']) : iconv("utf-8", "cp1251", $info->OSB_Name);
						$bank_name = substr($bank_name, 0, 254);
						$summa = isset($info->Balance) ? $info->Balance : 0;
						$dept_code = isset($info->OSB_Num) ? $info->OSB_Num : NULL;
						$summ_info = iconv(
										"utf-8",
										"cp1251",
										"Остаток на "
								   	  . $info->Req_Date
								   	  . " "
								   	  . $info->Balance);
						$summ_info = substr($summ_info, 0, 99);

						$accnum = str_replace(".", "", $info->Account);

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
									"'{$bic}'," .PHP_EOL .
									"'{$cur_info}'," .PHP_EOL .
									"'{$accnum}'," .PHP_EOL .
									"'{$bank_name}'," .PHP_EOL .
									"{$summa}," .PHP_EOL .
									"'{$dept_code}'," .PHP_EOL .
									"'{$summ_info}'" .PHP_EOL .
								")";

						$db->query($query4);
					}
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
		return true;
	}

	/*
	 * читает данные из файла ответа и возвращает в виде массива
	 *
	 * */
	public function readAll()
	{
		$result = array();
		while ($response = $this->read())
		{
			$result[$response->Req_ID][] = $response;
		}
		return $result;
	}

	/**
	 * считывает один ответ из файла
	 */

	private function read()
	{
		$response = new Response();
		$xml = $this->xml;
		while ($xml->read())
		{
			if ($xml->name == "otvet" && $xml->nodeType == XMLReader::END_ELEMENT)
			{
				return $response;
			}
			else if ($xml->name == "otvet" && $xml->nodeType == XMLReader::ELEMENT)
			{
				continue;
			}
			else if ($xml->name == "RESULT" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->RESULT = $xml->value;
			}
			else if ($xml->name == "File_Name" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->File_Name = $xml->value;
			}
			else if ($xml->name == "Req_ID" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Req_ID = $xml->value;
			}
			else if ($xml->name == "User_ID" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->User_ID = $xml->value;
			}
			else if ($xml->name == "Req_Type" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Req_Type = $xml->value;
			}
			else if ($xml->name == "File_Exp_Name" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->File_Exp_Name = $xml->value;
			}
			else if ($xml->name == "Resp_ID" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Resp_ID = $xml->value;
			}
			else if ($xml->name == "Req_Date" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Req_Date = $xml->value;
			}
			else if ($xml->name == "Req_Time" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Req_Time = $xml->value;
			}
			else if ($xml->name == "OSB_Name" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->OSB_Name = $xml->value;
			}
			else if ($xml->name == "OSB_Addr" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->OSB_Addr = $xml->value;
			}
			else if ($xml->name == "OSB_Num" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->OSB_Num = $xml->value;
			}
			else if ($xml->name == "OSB_Tel" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->OSB_Tel = $xml->value;
			}
			else if ($xml->name == "INN" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->INN = $xml->value;
			}
			else if ($xml->name == "BIC" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->BIC = $xml->value;
			}
			else if ($xml->name == "Account" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Account = $xml->value;
			}
			else if ($xml->name == "Op_Date" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Op_Date = $xml->value;
			}
			else if ($xml->name == "Balance" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Balance = $xml->value;
			}
			else if ($xml->name == "Vid_vkl" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Vid_vkl = $xml->value;
			}
			else if ($xml->name == "Val_vkl" && $xml->nodeType == XMLReader::ELEMENT)
			{
				$xml->read();
				$response->Val_vkl = $xml->value;
			}
		}

		return false;
	}

}