package org.pentaho.di.trans.steps.wfsinput;

public class Filter {
	private String attribute;
	private String condition;
	private String value;
	private String operator;

	public Filter(String attribute, String condition, String value,
			String operator) {
		this.attribute = attribute;
		this.condition = condition;
		this.value = value;
		this.operator = operator;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getCondition() {
		return condition;
	}

	public String getValue() {
		return value;
	}

	public String getOperator() {
		return operator;
	}
}
