package regalowl.simpledatalib.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import regalowl.simpledatalib.SimpleDataLib;


public class BasicStatement {

	protected String statement;
	protected SimpleDataLib sdl;
	protected ArrayList<Object> parameters = new ArrayList<Object>();
	
	public BasicStatement(String statement, SimpleDataLib sdl) {
		this.statement = statement;
		this.sdl = sdl;
	}
	public String getStatement() {
		return statement;
	}
	
	public void addParameter(Object param) {
		if (param instanceof String) {
			String s = (String) param;
			if (sdl.getSQLManager().useMySQL()) {
				if (s.contains("datetime('NOW', 'localtime')")) {
					s = s.replace("datetime('NOW', 'localtime')", "NOW()");
				}
			} else {
				if (s.contains("NOW()")) {
					s = s.replace("NOW()", "datetime('NOW', 'localtime')");
				}
			}
			param = s;
		}
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

	public void applyParameters(PreparedStatement preparedStatement) {
		if (!usesParameters()) {return;}
		for (int i = 0; i < parameters.size(); i++) {
			Object paramObject = parameters.get(i);
			try {
				if (paramObject instanceof String) {
					String param = (String)paramObject;
					preparedStatement.setString(i+1, param);
				} else if (paramObject instanceof Integer) {
					Integer param = (Integer)paramObject;
					preparedStatement.setInt(i+1, param);
				} else if (paramObject instanceof Long) {
					Long param = (Long)paramObject;
					preparedStatement.setLong(i+1, param);
				} else if (paramObject instanceof Float) {
					Float param = (Float)paramObject;
					preparedStatement.setFloat(i+1, param);
				} else if (paramObject instanceof Double) {
					Double param = (Double)paramObject;
					preparedStatement.setDouble(i+1, param);
				} else if (paramObject instanceof Boolean) {
					Boolean param = (Boolean)paramObject;
					preparedStatement.setBoolean(i+1, param);
				} else if (paramObject instanceof BigDecimal) {
					BigDecimal param = (BigDecimal)paramObject;
					preparedStatement.setBigDecimal(i+1, param);
				} else if (paramObject instanceof Short) {
					Short param = (Short)paramObject;
					preparedStatement.setShort(i+1, param);
				} else if (paramObject instanceof Byte) {
					Byte param = (Byte)paramObject;
					preparedStatement.setByte(i+1, param);
				} else {
					preparedStatement.setObject(i+1, paramObject);
				}
			} catch (Exception e) {
				sdl.getErrorWriter().writeError(e);
			}
		}
	}

	
}
