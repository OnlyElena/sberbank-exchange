<?php

require_once 'document.php';

class Request extends Document
{

	public function __construct()
	{
		$this->type = 'REQUEST';
	}
}