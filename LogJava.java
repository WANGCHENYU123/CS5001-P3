import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
/**
 * The class is for logging
 * 
 * @author 180007800
 *
 */
public class LogJava {
	// TODO Auto-generated method stub
	private String method;
	private String rescode;

	public LogJava(String method, String rescode) {
		// TODO Auto-generated method stub
		this.method = method;
		this.rescode = rescode;
	}

	public void logjava() throws IOException {
		// TODO Auto-generated method stub
		Logger log = Logger.getLogger("tesglog");
		log.setLevel(Level.ALL);
		FileHandler fileHandler = new FileHandler("testlog.log");
		fileHandler.setLevel(Level.ALL);
		fileHandler.setFormatter(new LogFormatter());
		log.addHandler(fileHandler);
		log.info(": " + method);
		log.info(": " + rescode);
	}
}

class LogFormatter extends Formatter {
	// TODO Auto-generated method stub
	public String format(LogRecord record) {
		Date date = new Date();
		String sDate = date.toString();
		return "[" + sDate + "]" + "[" + record.getLevel() + "] " + record.getClass() + ": " + record.getMessage()
				+ "\n";
	}
}