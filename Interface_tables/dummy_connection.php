<?php

class DummyDbConnection
{
	private $connection = null;

	private $options = array();

	private $result = null;

	private $trans = null;

	private $error = "";

	private $id = 0;

	public function __construct($connectionOptions = array())
	{
		$this->options = $connectionOptions;
		if (!empty($connectionOptions))
			$this->connect();
	}

	public function __destruct()
	{
		$this->close();
	}

	public function close()
	{
		return fclose($this->connection);
	}

	public function connect($connectionOptions = array())
	{
		if (!empty($connectionOptions))
			$this->options = $connectionOptions;

		$this->connection = fopen('dummy.txt', 'a+');

		return $this;

	}

	public function __set($name, $value)
	{
		$this->options[$name] = $value;
	}

	public function __get($name)
	{
		if (array_key_exists($name, $this->options)) {
			return $this->options[$name];
		}

		$trace = debug_backtrace();
		trigger_error(
			'Undefined property via __get(): ' . $name . ' in ' . $trace[0]['file'] . ' on line ' . $trace[0]['line'],
			E_USER_NOTICE
		);
		return null;
	}

	public function rowsAffected()
	{
		return 0;
	}

	public function beginTransaction($mode = IBASE_DEFAULT)
	{
		return true;
	}

	public function commit($close = true)
	{
		return true;
	}

	public function rollback($close = true)
	{
		return true;
	}

	public function query($query)
	{
		echo $query . PHP_EOL;
		$date = date("Y-m-d H:i:s");
		return fwrite($this->connection, $date . ": " . $query . PHP_EOL) ? true : false;
	}

	public function getMessage()
	{
		return true;
	}

	public function fetchOne($query = "")
	{
		return $this->query($query);
	}

	public function fetchAll($query = "")
	{
		return $this->query($query);
	}

}