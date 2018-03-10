package EZShare;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.*;
import java.util.Date;



public class LogFormatter extends Formatter{
	private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	public String format(LogRecord record){
		StringBuilder builder = new StringBuilder();

		builder.append(df.format(new Date(record.getMillis()))).append(" - [");
		builder.append(record.getSourceClassName()).append(".");
		builder.append(record.getSourceMethodName()).append("] - ");
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(record.getMessage());  // why??
		builder.append('\n');
		
		return builder.toString();
	}
}