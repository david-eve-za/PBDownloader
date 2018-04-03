package gon.cue.model.util;

import static org.jooq.impl.DSL.constraint;
import gon.cue.model.ddl.Public;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jooq.Constraint;
import org.jooq.DSLContext;
import org.jooq.ForeignKey;
import org.jooq.SQLDialect;
import org.jooq.UniqueKey;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

public class JOOQUtil {

	private Connection conn;
	protected DSLContext create;

	public JOOQUtil(String DBName, String User, String Pass) {
	    
	    try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

		StringBuilder strConnect = new StringBuilder();

		strConnect.append("jdbc:hsqldb:file:./").append(DBName);

		Settings settings = new Settings();

		settings.withExecuteLogging(true);

		initializeLog4j();

		try {

			conn = DriverManager.getConnection(strConnect.toString(), User, Pass);

			create = DSL.using(conn, SQLDialect.HSQLDB, settings);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void initializeLog4j() {
		ConsoleAppender console = new ConsoleAppender(); // create appender
		// configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.ALL);
		console.activateOptions();
		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);

		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile("INFO.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.ALL);
		fa.setAppend(true);
		fa.activateOptions();

		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(fa);
		// repeat with all other desired appenders
	}

	protected void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void createSchema() {

//		System.out.println();

		Public.PUBLIC.getSequences().forEach(item -> {
//			System.out.println("Execute query: \n" + create.createSequenceIfNotExists(item));
			create.createSequenceIfNotExists(item).execute();
		});

//		System.out.println();

		Public.PUBLIC.getTables().forEach(item -> {
			List<Constraint> constraints = new ArrayList<Constraint>();

			if (!item.getKeys().isEmpty()) {
				for (UniqueKey<?> key : item.getKeys()) {
					if (key.isPrimary()) {
 						constraints.add(constraint(key.getName()).primaryKey(key.getFieldsArray()));
					}
					if (!key.isPrimary()) {
						constraints.add(constraint(key.getName()).unique(key.getFieldsArray()));
					}
				}
			}

			if (!item.getReferences().isEmpty()) {
				for (ForeignKey<?, ?> key : item.getReferences()) {
					key.getFields().stream().forEach(action -> {
						System.out.println(action.getName());
					});

					constraints.add(constraint(key.getName()).foreignKey(key.getFieldsArray())
							.references(key.getKey().getTable(), key.getKey().getFieldsArray()));
				}
			}

//			System.out.println("Execute query: \n"
//					+ create.createTableIfNotExists(item).columns(item.fields()).constraints(constraints));
			create.createTableIfNotExists(item).columns(item.fields()).constraints(constraints).execute();

			System.out.println();
		});
	}
}
