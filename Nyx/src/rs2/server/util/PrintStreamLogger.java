package rs2.server.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author John
 * 
 */
public class PrintStreamLogger extends PrintStream {

	private DateFormat dateFormat = new SimpleDateFormat();
	private Date cachedDate = new Date();
	private SimpleTimer refreshTimer = new SimpleTimer();

	public PrintStreamLogger(OutputStream outStream) {
		super(outStream);
	}

	@Override
	public void print(String string) {
		String formattedString = formatString(string);
		if (string.contains("#err#"))
			formattedString = "[ERROR]" + formattedString;
		if (string.contains("#fatal#"))
			formattedString = "[FATAL]" + formattedString;
		if (string.contains("#pre#"))
			formattedString = "[" + getPrefix() + "] " + formattedString;
		super.print(formattedString);
		if (string.contains("#new#"))
			super.println();
	}
	@Override
	public void println(String string) {
		String formattedString = formatString(string);
		if (string.contains("#err#"))
			formattedString = "[ERROR]" + formattedString;
		if (string.contains("#fatal#"))
			formattedString = "[FATAL]" + formattedString;
		if (string.contains("#pre#"))
			formattedString = "[" + getPrefix() + "] " + formattedString;
		super.print(formattedString);
		super.println();
		if (string.contains("#new#"))
			super.println();
	}
	
	private String formatString(String string) {
		string = string.replaceAll("#new#", "");
		string = string.replaceAll("#pre#", "");
		string = string.replaceAll("#err#", "");
		string = string.replaceAll("#fatal#", "");
		return string;
	}
	
	private String getPrefix() {
		if (refreshTimer.elapsed() > 1000) {
			refreshTimer.reset();
			cachedDate = new Date();
		}
		return dateFormat.format(cachedDate);
	}

}
