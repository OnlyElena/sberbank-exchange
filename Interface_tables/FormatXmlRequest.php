<?php


class FormatXmlRequest
{

	private $common;

	private $definition;

	private $dbf;

	private $current = 0;

	private $records = 0;

	private $file = "";

	private $xml;

	public function __construct($config = array(), $filename = "")
	{
		$this->common = $config['common'];
		$this->file = $filename;
		//$this->request = $config['request'];
		//$this->matches = $config['matches'];
		$this->xml = new XMLWriter();
	}

	private function setFile($file)
	{
		if (!$this->xml->openURI($file))
		{
			throw new Exception("Cannot open/create file " . $file);
			return false;
		}
		echo $file;
		return true;
	}

	private function close()
	{
	}

	private function write(Request $request)
	{
		$xml = $this->xml;
		$xml->startElement('Zapros');

		$xml->writeElement('File_Name', $request->File_Name);
		$xml->writeElement('Req_ID', substr($request->Req_ID, 4)); //10 символов длина
		$xml->writeElement('User_ID', $request->User_ID);
		$xml->writeElement('Req_Date', $request->Req_Date);
		$xml->writeElement('Req_Time', $request->Req_Time);
		$xml->writeElement('Req_Type', $request->Req_Type);
		$xml->writeElement('OSB_List', $request->OSB_List);
		$xml->writeElement('Prs_Dep', $request->Prs_Dep);
		$xml->writeElement('FIO_SPI', iconv("cp1251", "utf-8", $request->FIO_SPI));
		$xml->writeElement('H_PRISTAV', iconv("cp1251", "utf-8", $request->H_PRISTAV));
		$xml->writeElement('Isp_Num', $request->Isp_Num);
		$xml->writeElement('Isp_Sum', sprintf("%.2f", $request->Isp_Sum));
		$xml->writeElement('Isp_Num', iconv("cp1251", "utf-8", $request->Id_Num));
		$xml->writeElement('Req_Date', $request->Id_Req_Date);
		$xml->writeElement('Dolg_Surname', iconv("cp1251", "utf-8", $request->Dolg_Surname));
		$xml->writeElement('Dolg_Name', iconv("cp1251", "utf-8", $request->Dolg_Name));
		$xml->writeElement('Dolg_Secondname', iconv("cp1251", "utf-8", $request->Dolg_Secondname));
		$xml->writeElement('Dolg_Birth_Year', $request->Dolg_Birth_Year);
		$xml->writeElement('Dolg_Addr', iconv("cp1251", "utf-8", $request->Dolg_Addr));
		$xml->writeElement('Dolg_Birth_Day', $request->Dolg_Birth_Day);
		$xml->writeElement('Dolg_Place_Birth', iconv("cp1251", "utf-8", $request->Dolg_Place_Birth));

		$xml->endElement();
	}

	public function save($db)
	{
		$where[] = "MVV_AGREEMENT_CODE = '".iconv("utf-8", "cp1251", $this->common['agreement_code'])."'";
		$where[] = "MVV_AGENT_CODE = '".iconv("utf-8", "cp1251", $this->common['agent_code'])."'";
		$where[] = "MVV_AGENT_DEPT_CODE = '".iconv("utf-8", "cp1251", $this->common['agent_dept_code'])."'";
		$where[] = "PROCESSED = 0";
		$where[] = "ENTITY_TYPE IN (2, 71, 95, 96, 97)";

		$where = " WHERE " . implode(' AND ', $where) . " ";

		$pack = "SELECT PACK_ID "
				."FROM EXT_REQUEST "
				.$where
				."GROUP BY PACK_ID";

		$packs = $db->fetchAll($pack);

		//var_dump($packs);

		$query =
				"SELECT " .
					"REQ_ID," .
					"REQ_DATE," .
					"FIO_SPI," .
					"SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," .
					"IP_NUM," .
					"IP_SUM," .
					"ID_NUMBER," .
					"EXT_REQUEST.ID_DATE," .
					"ENTT_SURNAME," .
					"ENTT_FIRSTNAME," .
					"ENTT_PATRONYMIC," .
					"DBTR_BORN_YEAR," .
					"DEBTOR_ADDRESS," .
					"DEBTOR_BIRTHDATE," .
					"DEBTOR_BIRTHPLACE" .
				" FROM" .
				" DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID" .
				" INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID" .
				" INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID" .
				" INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" .
				$where;

		$i = 0;

		echo PHP_EOL . iconv("utf-8", "cp866", "Формируем файлы запросов:") . PHP_EOL;

	    foreach ($packs as $pack)
	    {
    		$letter = array(
				1 	=> "1",
				2 	=> "2",
				3 	=> "3",
				4 	=> "4",
				5 	=> "5",
				6 	=> "6",
				7 	=> "7",
				8 	=> "8",
				9 	=> "9",
				10 	=> "A",
				11 	=> "B",
				12 	=> "C",
				13 	=> "D",
				14 	=> "E",
				15 	=> "F",
				16 	=> "G",
				17 	=> "H",
				18 	=> "I",
				19 	=> "J",
				20 	=> "K",
				21 	=> "L",
				22 	=> "M",
				23 	=> "N",
				24 	=> "O",
				25	=> "P",
				26 	=> "Q",
				27 	=> "R",
				28 	=> "S",
				29 	=> "T",
				30 	=> "U",
				31 	=> "V",
				32 	=> "W",
				33 	=> "X",
				34 	=> "Y",
				35 	=> "Z"
			);

    		$i++;

    		$ext_client = $db->code+48; // 48 - разница между номер ОСП и номером клиента в Сбербанке

    		$ext_filenum = $letter[$i];

    		$now = time();
			$day = date("d", $now);

			$req_time = date("H:i", $now);

			$month = dechex(date("m", $now));
			$date_part = "r" . $day . $month . "0018";

			$filename = $date_part . "." . $ext_filenum . $ext_client;

    		$file = dirname($this->file) . DS . $filename;

    		try
    		{

	    		$this->setFile($file);
	    		$this->xml->setIndent(true);
				$this->xml->startDocument('1.0', 'UTF-8');
				$this->xml->startElement('Request');

				echo PHP_EOL . iconv("utf-8", "cp866", "Пишем пакет " . $pack['PACK_ID']) . PHP_EOL;

	    		$sql = $query . " AND PACK_ID = {$pack['PACK_ID']}";
				$res = $db->fetchOne($sql);

				$j = 0;
				while ($res)
				{
					echo ".";
					$j++;
					$request = new Request();
					$request->File_Name = $filename;
					$request->User_ID = '9999';
					$request->Req_Time = $req_time;
					$request->Req_Type = 1;
					$request->OSB_List = "0018";
					$request->Prs_Dep = $db->code;
					$request->Req_ID = $res['REQ_ID'];
					$request->Req_Date = date("d.m.Y", strtotime($res['REQ_DATE']));
					$request->FIO_SPI = $res['FIO_SPI'];
					$request->H_PRISTAV = $res['H_PRISTAV'];
					$request->Isp_Num = $res['IP_NUM'];
					$request->Isp_Sum = $res['IP_SUM'];
					$request->Id_Num = $res['ID_NUMBER'];
					$request->Id_Req_Date = date("d.m.Y", strtotime($res['ID_DATE']));
					$request->Dolg_Surname = $res['ENTT_SURNAME'];
					$request->Dolg_Name = $res['ENTT_FIRSTNAME'];
					$request->Dolg_Secondname = $res['ENTT_PATRONYMIC'];
					$request->Dolg_Birth_Year = $res['DBTR_BORN_YEAR'];
					$request->Dolg_Addr = $res['DEBTOR_ADDRESS'];
					$request->Dolg_Birth_Day = date("d.m.Y", strtotime($res['DEBTOR_BIRTHDATE']));
					$request->Dolg_Place_Birth = $res['DEBTOR_BIRTHPLACE'];

					$this->write($request);
					$res = $db->fetchOne();
				}

				echo PHP_EOL;

				$this->xml->endElement();
				$this->xml->endDocument();
    		}
    		catch (Exception $e)
    		{
    			echo "File: " 		. $e->getFile() 	. PHP_EOL
				   . "Line: " 		. $e->getLine() 	. PHP_EOL
				   . "Message: " 	. $e->getMessage()	. PHP_EOL;
    			if (!unlink($file))
    				die("Cannot delete file " . $file. PHP_EOL);
    			else
    				continue;
    		}

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