<?php
/* Запрос и ответ наследуют от документа */
class Document
{

	/* тип документа */
	protected $type;

	/* данные документа */
	protected $data = array();

	public function __construct($type)
	{
		$this->type = $type;
	}

	public function getType()
	{
		return $this->type;
	}

	public function __set($name, $value)
	{
		$this->data[$name] = $value;
	}

	public function getData()
	{
		return $this->data;
	}


	public function __get($name)
	{
		if (array_key_exists($name, $this->data)) {
			return $this->data[$name];
		}

		$trace = debug_backtrace();
		trigger_error(
			'Undefined property via __get(): ' . $name . ' in ' . $trace[0]['file'] . ' on line ' . $trace[0]['line'],
			E_USER_NOTICE
		);
		return null;
	}

	public function __isset($name) {
        return isset($this->data[$name]);
    }

    public function __unset($name) {
        unset($this->data[$name]);
    }

    public function __toString()
    {
    	return $this->type . ": " . implode(";", $this->data);
    }

}