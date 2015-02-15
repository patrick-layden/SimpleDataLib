package regalowl.simpledatalib.sql;


import java.util.ArrayList;




public class QueryResult {
	
	
	private ArrayList<String> colNames = new ArrayList<String>();
	private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	private Object additionalData;
	private int currentRow;
	
	private boolean successful;
	private Exception sqlError;
	private String failedSQL;

	public QueryResult() {
		this.successful = true;
		currentRow = -1;
	}
	public void addColumnName(String name) {
		colNames.add(name);
		data.add(new ArrayList<String>());
	}
	public void addData(int columnIndex, String object) {
		data.get(columnIndex - 1).add(object);
	}
	public void reset() {
		currentRow = -1;
	}
	public int recordCount() {
		if (data.isEmpty()) return 0;
		return data.get(0).size();
	}
	public boolean next() {
		currentRow++;
		if (data.isEmpty()) {
			return false;
		} else if (data.get(0).size() > currentRow) {
			return true;
		} else {
			return false;
		}
	}
	public void close() {
		colNames.clear();
		data.clear();
	}	
	
	
	
	public int getColumnCount() {
		return colNames.size();
	}
	public ArrayList<String> getColumnNames() {
		return colNames;
	}
	
	
	
	public String getString(String column) {
		if (colNames.indexOf(column) == -1) {
			return null;
		}
		return (String) data.get(colNames.indexOf(column)).get(currentRow);
	}
	public Double getDouble(String column) {
		if (colNames.indexOf(column) == -1) {
			return null;
		}
		String dat = data.get(colNames.indexOf(column)).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return Double.parseDouble(dat);
		}
	}
	public Integer getInt(String column) {
		if (colNames.indexOf(column) == -1) {
			return null;
		}
		String dat = data.get(colNames.indexOf(column)).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return (int)Double.parseDouble(dat);
		}
	}
	public Long getLong(String column) {
		if (colNames.indexOf(column) == -1) {
			return null;
		}
		String dat = data.get(colNames.indexOf(column)).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return (long)Double.parseDouble(dat);
		}
	}
	public Float getFloat(String column) {
		if (colNames.indexOf(column) == -1) {
			return null;
		}
		String dat = data.get(colNames.indexOf(column)).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return Float.parseFloat(dat);
		}
	}
	public Boolean getBoolean(String column) {
		if (colNames.indexOf(column) == -1) {
			return null;
		}
		String dat = data.get(colNames.indexOf(column)).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return Boolean.parseBoolean(dat);
		}
	}
	
	
	
	public String getString(Integer column) {
		return (String) data.get(column - 1).get(currentRow);
	}
	public Double getDouble(Integer column) {
		String dat = data.get(column - 1).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return Double.parseDouble(dat);
		}
	}
	public Integer getInt(Integer column) {
		String dat = data.get(column - 1).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return (int)Double.parseDouble(dat);
		}
	}
	public Long getLong(Integer column) {
		String dat = data.get(column - 1).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return (long)Double.parseDouble(dat);
		}
	}
	public Float getFloat(Integer column) {
		String dat = data.get(column - 1).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return Float.parseFloat(dat);
		}
	}
	public Boolean getBoolean(Integer column) {
		String dat = data.get(column - 1).get(currentRow);
		if (dat == null) {
			return null;
		} else {
			return Boolean.parseBoolean(dat);
		}
	}
	
	public void setAdditionalData(Object o) {
		additionalData = o;
	}
	public Object getAdditionalData() {
		return additionalData;
	}
	
	
	public void setException(Exception e, String failedSQL) {
		this.sqlError = e;
		this.successful = false;
		this.failedSQL = failedSQL;
	}
	public Exception getException() {
		return sqlError;
	}
	public String getFailedSQL() {
		return failedSQL;
	}
	public boolean successful() {
		return successful;
	}
	
	
}
