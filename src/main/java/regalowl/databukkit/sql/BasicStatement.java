package regalowl.databukkit.sql;

import java.util.ArrayList;

import regalowl.databukkit.DataBukkit;


public class BasicStatement {

	protected String statement;
	protected DataBukkit dab;
	protected ArrayList<Object> parameters = new ArrayList<Object>();
	
	public BasicStatement(String statement, DataBukkit dab) {
		this.statement = statement;
		this.dab = dab;
	}
	public String getStatement() {
		return statement;
	}
	
	public void addParameter(Object param) {
		parameters.add(param);
	}
	
	public ArrayList<Object> getParameters() {
		return parameters;
	}
	
	public boolean usesParameters() {
		if (parameters.size() > 0) {
			return true;
		}
		return false;
	}


	
}
