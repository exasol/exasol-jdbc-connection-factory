package com.exasol.jdbc.functional;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple interface declaration to implement typed closures
 */
@FunctionalInterface
public interface FunctionPreparedStatement {

long apply( PreparedStatement preparedStatement ) throws SQLException;

}
