package com.sismics.util.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.ConnectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

/**
 * A helper to update the database incrementally.
 *
 * @author jtremeaux
 */
public abstract class DbOpenHelper {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DbOpenHelper.class);

    private final ConnectionHelper connectionHelper;
    
    private final SqlStatementLogger sqlStatementLogger;
    
    private final List<Exception> exceptions = new ArrayList<Exception>();

    private Formatter formatter;

    private boolean haltOnError;
    
    private Statement stmt;

    public DbOpenHelper(ServiceRegistry serviceRegistry) throws HibernateException {
        final JdbcServices jdbcServices = serviceRegistry.getService(JdbcServices.class);
        connectionHelper = new SuppliedConnectionProviderConnectionHelper(jdbcServices.getConnectionProvider());

        sqlStatementLogger = jdbcServices.getSqlStatementLogger();
        formatter = (sqlStatementLogger.isFormat() ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
    }

    public void open() {
        log.info("Opening database and executing incremental updates");

        Connection connection = null;
        Writer outputFileWriter = null;

        exceptions.clear();

        try {
            try {
                connectionHelper.prepare(true);
                connection = connectionHelper.getConnection();
            } catch (SQLException sqle) {
                exceptions.add(sqle);
                log.error("Unable to get database metadata", sqle);
                throw sqle;
            }

            // Check if database is already created
            Integer oldVersion = null;
            try {
                stmt = connection.createStatement();
                ResultSet result = stmt.executeQuery("select c.CFG_VALUE_C from T_CONFIG c where c.CFG_ID_C='DB_VERSION'");
                if (result.next()) {
                    String oldVersionStr = result.getString(1);
                    oldVersion = Integer.parseInt(oldVersionStr);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Table not found")) {
                    log.info("Unable to get database version: Table T_CONFIG not found");
                } else {
                    log.error("Unable to get database version", e);
                }
            } finally {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            }

            stmt = connection.createStatement();
            if (oldVersion == null) {
                // Execute creation script
                log.info("Executing initial schema creation script");
                onCreate();
            } else {
                // Execute update script
                log.info(MessageFormat.format("Found database version {0}, executing database incremental update scripts", oldVersion));
                onUpgrade(oldVersion, 42); // TODO complete upgrade scripting
                log.info("Database upgrade complete");
            }
        } catch (Exception e) {
            exceptions.add(e);
            log.error("Unable to complete schema update", e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                connectionHelper.release();
            } catch (Exception e) {
                exceptions.add(e);
                log.error("Unable to close connection", e);
            }
            try {
                if (outputFileWriter != null) {
                    outputFileWriter.close();
                }
            } catch (Exception e) {
                exceptions.add(e);
                log.error("Unable to close connection", e);
            }
        }
    }

    /**
     * Execute a SQL script. All statements must be one line only.
     * 
     * @param inputScript Script to execute
     * @throws IOException
     * @throws SQLException
     */
    protected void executeScript(InputStream inputScript) throws IOException, SQLException {
        List<String> lines = CharStreams.readLines(new InputStreamReader(inputScript));
        
        for (String sql : lines) {
            String formatted = formatter.format(sql);
            try {
                log.debug(formatted);
                stmt.executeUpdate(formatted);
            } catch (SQLException e) {
                if (haltOnError) {
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                    throw new JDBCException("Error during script execution", e);
                }
                exceptions.add(e);
                if (log.isErrorEnabled()) {
                    log.error("Error executing SQL statement: {0}", sql);
                    log.error(e.getMessage());
                }
            }
        }
    }

    public abstract void onCreate() throws Exception;
    
    public abstract void onUpgrade(int oldVersion, int newVersion) throws Exception;
    
    /**
     * Returns a List of all Exceptions which occured during the export.
     *
     * @return A List containig the Exceptions occured during the export
     */
    public List<?> getExceptions() {
        return exceptions;
    }

    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    /**
     * Format the output SQL statements.
     * 
     * @param format True to format
     */
    public void setFormat(boolean format) {
        this.formatter = (format ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
    }
}
