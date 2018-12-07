package com.exasol.jdbc.functional;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple interface declaration to implement typed closures
 */
@FunctionalInterface
public interface FunctionResultSet {

void apply( ResultSet resultSet ) throws SQLException;

}
