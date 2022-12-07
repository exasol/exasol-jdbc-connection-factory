package com.exasol.jdbc.functional;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple interface declaration to implement typed closures
 */
@FunctionalInterface
public interface FunctionPreparedStatement {

/**
 * Callback function that uses the given {@link PreparedStatement}.
 * @param preparedStatement the {@link PreparedStatement}
 * @return a value e.g. the row count
 * @throws SQLException if the process failed
 */
long apply( PreparedStatement preparedStatement ) throws SQLException;

}
