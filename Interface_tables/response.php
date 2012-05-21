<?php

require_once 'document.php';

class Response extends Document
{

	public function __construct()
	{
		$this->type = 'RESPONSE';
	}

}