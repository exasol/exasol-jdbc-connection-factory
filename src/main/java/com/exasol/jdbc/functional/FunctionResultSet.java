package com.exasol.jdbc.functional;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple interface declaration to implement typed closures
 */
@FunctionalInterface
public interface FunctionResultSet {

/**
 * Callback function that uses the given {@link ResultSet}.
 * @param resultSet the {@link ResultSet} to process
 * @throws SQLException if processing fails
 */
void apply( ResultSet resultSet ) throws SQLException;

}
