/**
 * Copyright 2004-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.common.utils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool to run database scripts
 */
public class ScriptRunner {

	public static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);

	private static final String DEFAULT_DELIMITER = ";";

	private Connection connection;

	private boolean stopOnError;
	private boolean autoCommit;

	private String delimiter = DEFAULT_DELIMITER;
	private boolean fullLineDelimiter = false;


	public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
		this.connection = connection;
		this.autoCommit = autoCommit;
		this.stopOnError = stopOnError;
	}


	public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
		this.delimiter = delimiter;
		this.fullLineDelimiter = fullLineDelimiter;
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter)
	 *
	 * @param reader
	 *            - the source of the script
	 * @throws IOException errors
	 * @throws SQLException errors
	 */
	public void runScript(Reader reader) throws IOException, SQLException {
		try {
			boolean originalAutoCommit = connection.getAutoCommit();
			try {
				if (originalAutoCommit != this.autoCommit) {
					connection.setAutoCommit(this.autoCommit);
				}
				runScript(connection, reader);
			} finally {
				connection.setAutoCommit(originalAutoCommit);
			}
		} catch (IOException | SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error running script.  Cause: " + e, e);
		}
	}

	public void runScript(Reader reader, String dbName) throws IOException, SQLException {
		try {
			boolean originalAutoCommit = connection.getAutoCommit();
			try {
				if (originalAutoCommit != this.autoCommit) {
					connection.setAutoCommit(this.autoCommit);
				}
				runScript(connection, reader, dbName);
			} finally {
				connection.setAutoCommit(originalAutoCommit);
			}
		} catch (IOException | SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error running script.  Cause: " + e, e);
		}
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter) using the connection
	 * passed in
	 *
	 * @param conn
	 *            - the connection to use for the script
	 * @param reader
	 *            - the source of the script
	 * @throws SQLException
	 *             if any SQL errors occur
	 * @throws IOException
	 *             if there is an error reading from the Reader
	 */
	private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
		StringBuffer command = null;
		try {
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					logger.info(trimmedLine);
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
					// Do nothing
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
					// Do nothing

				} else if (trimmedLine.startsWith("delimiter")) {
					String newDelimiter = trimmedLine.split(" ")[1];
					this.setDelimiter(newDelimiter, fullLineDelimiter);

				} else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
						|| fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
					command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
					command.append(" ");
                    logger.info("sql: {}", command);

                    try (Statement statement = conn.createStatement()) {
                        statement.execute(command.toString());
                        try (ResultSet rs = statement.getResultSet()) {
                            if (stopOnError && rs != null) {
                                ResultSetMetaData md = rs.getMetaData();
                                int cols = md.getColumnCount();
                                for (int i = 1; i < cols; i++) {
                                    String name = md.getColumnLabel(i);
                                    logger.info("{} \t", name);
                                }
                                logger.info("");
                                while (rs.next()) {
                                    for (int i = 1; i < cols; i++) {
                                        String value = rs.getString(i);
                                        logger.info("{} \t", value);
                                    }
                                    logger.info("");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        logger.error("SQLException", e);
                        throw e;
                    }

					command = null;
					Thread.yield();
				} else {
					command.append(line);
					command.append(" ");
				}
			}

		} catch (SQLException e) {
			logger.error("Error executing: {}", command);
			throw e;
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: {}", command);
			throw e;
		}
	}

	private void runScript(Connection conn, Reader reader , String dbName) throws IOException, SQLException {
		StringBuffer command = null;
		String sql = "";
		String appKey = dbName.substring(dbName.lastIndexOf("_")+1, dbName.length());
		try {
			LineNumberReader lineReader = new LineNumberReader(reader);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				if (command == null) {
					command = new StringBuffer();
				}
				String trimmedLine = line.trim();
				if (trimmedLine.startsWith("--")) {
					logger.info(trimmedLine);
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
					// Do nothing
				} else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
					// Do nothing

				} else if (trimmedLine.startsWith("delimiter")) {
					String newDelimiter = trimmedLine.split(" ")[1];
					this.setDelimiter(newDelimiter, fullLineDelimiter);

				} else if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
						|| fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
					command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
					command.append(" ");
					sql = command.toString().replaceAll("\\{\\{APPDB\\}\\}", dbName);
                    logger.info("sql : {}", sql);

                    try (Statement statement = conn.createStatement()) {
                        statement.execute(sql);
                        try (ResultSet rs = statement.getResultSet()) {
                            if (stopOnError && rs != null) {
                                ResultSetMetaData md = rs.getMetaData();
                                int cols = md.getColumnCount();
                                for (int i = 0; i < cols; i++) {
                                    String name = md.getColumnLabel(i);
                                    logger.info("{} \t", name);
                                }
                                logger.info("");
                                while (rs.next()) {
                                    for (int i = 0; i < cols; i++) {
                                        String value = rs.getString(i);
                                        logger.info("{} \t", value);
                                    }
                                    logger.info("");
                                }
                            }
                        }
                    } catch (SQLException e) {
                        logger.error("SQLException", e);
                        throw e;
                    }

					command = null;
					Thread.yield();
				} else {
					command.append(line);
					command.append(" ");
				}
			}

		} catch (SQLException e) {
			throw e;
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: {}", sql);
			throw e;
		}
	}

	private String getDelimiter() {
		return delimiter;
	}

}