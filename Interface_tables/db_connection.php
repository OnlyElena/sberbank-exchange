<?php

class DbConnection
{
	private $connection = null;

	private $options = array();

	private $result = null;

	private $trans = null;

	private $code = 0;

	private $error = "";

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

	public function setHost($host)
	{
		$this->options['host'] = $host;
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

	public function __isset($name) {
        return isset($this->options[$name]);
    }

    public function __unset($name) {
        unset($this->options[$name]);
    }

    public function getConnectionOptions()
    {
    	return $this->options;
    }

	public function setConnectionOptions($connectionOptions)
	{
		$this->options = $connectionOptions;
	}

	public function setUsername($username)
	{
		$this->options['username'] = $username;
	}

	public function setPassword($password)
	{
		$this->options['password'] = $password;
	}

	public function isConnected()
	{
		return (boolean) $this->connection;
	}

	public function close()
	{
		if ($this->isConnected())
		{
			ibase_close($this->connection);
			$this->connection = null;
		}
		return true;
	}

	public function connect($connectionOptions = array())
	{
		if (!empty($connectionOptions))
		{
			$this->options = $connectionOptions;
			$this->close();
		}
		else if ($this->isConnected())
		{
			return $this;
		}
		$connectionString = $this->options['host']
						  . (isset($this->options['port']) ? "/".$this->options['port'] : "")
						  . ":" . $this->options['dbname'];
		$username = isset($this->options['username']) ? $this->options['username'] : null;
		$password = isset($this->options['password']) ? $this->options['password'] : null;
		$charset = isset($this->options['charset']) ? $this->options['charset'] : null;
		//print_r($this->options);
		//echo PHP_EOL. $connectionString;
		$this->connection = ibase_connect($connectionString, $username, $password, $charset);
		if (!$this->isConnected())
		{
			$this->error = ibase_errmsg();
			throw new Exception("Failed to connect to " . iconv("utf-8", "cp866", $this->options['name']) . ": " . PHP_EOL . $this->getMessage());
			return false;
		}

		return $this;

	}

	public function rowsAffected()
	{
		return ($this->trans !== null) ? ibase_affected_rows($this->trans) : ibase_affected_rows($this->connection);
	}

	public function beginTransaction($mode = IBASE_DEFAULT)
	{
		if ($this->isConnected())
		{
			$this->trans = ibase_trans($this->connection, $mode);
			return true;
		}
		else
			throw new Exception("We are not connected to database.");
			return false;
	}

	public function commit($close = true)
	{
		$trans = ($this->trans === null) ? $this->connection : $this->trans;

		if ($close)
		{
			if (!$result = ibase_commit($trans))
			{
				throw new Exception("Transaction commit failed. " .PHP_EOL. ibase_errmsg());
			}
			$this->trans = null;
		}
		else
		{
			if (!$result = ibase_commit_ret($trans))
			{
				throw new Exception("Transaction commit without closing failed. " .PHP_EOL. ibase_errmsg());
			}
		}

		return $result;
	}

	public function rollback($close = true)
	{
		$trans = ($this->trans === null) ? $this->connection : $this->trans;

		if ($close)
		{
			if (!$result = ibase_rollback($trans))
			{
				throw new Exception("Transaction rollback failed. " .PHP_EOL. ibase_errmsg());
			}
			$this->trans = null;
		}
		else
		{
			if (!$result = ibase_rollback_ret($trans))
			{
				throw new Exception("Transaction rollback without closing failed. " .PHP_EOL. ibase_errmsg());
			}
		}

		return $result;
	}

	public function query($query)
	{
		if ($this->trans !== null)
			$conn = $this->trans;
		else
			$conn = $this->connection;

		$args = func_get_args();
		$arguments[] = $conn;
		foreach ($args as $arg)
			$arguments[] = $arg;

		//echo PHP_EOL . $query . PHP_EOL;
		$this->result = call_user_func_array('ibase_query', $arguments);

		//var_dump($arguments);

		if ($this->result == null)
		{
			$this->error = ibase_errmsg();
			$this->code = ibase_errcode();
			throw new Exception("Error occured when trying to execute query." . PHP_EOL
								. "Problem query: " . PHP_EOL
								. $query . PHP_EOL
								. "Error code: " . $this->code . PHP_EOL
								. "OSP " . $this->options['code'] . PHP_EOL
								. $this->getMessage());
		}
		return $this->result;

	}

	public function getMessage()
	{
		return $this->error;
	}

	public function fetchOne($query = "", $mode = IBASE_TEXT)
	{
		if ($query !== "")
		{
			return ibase_fetch_assoc($this->query($query), $mode);
		}
		else if ($this->result !== null)
			return ibase_fetch_assoc($this->result, $mode);
		else
			return null;
	}

	public function fetchAll($query = "", $mode = IBASE_TEXT)
	{
		$rows = array();
		if ($query !== "")
		{
			$this->query($query);
			while ($row = ibase_fetch_assoc($this->result, $mode))
			{
				$rows[] = $row;
			}
			return $rows;
		}
		else if ($this->result !== null)
		{
			while ($row = ibase_fetch_assoc($this->result, $mode))
			{
				$rows[] = $row;
			}
			return $rows;
		}
		else
			return null;
	}

}