package com.exasol.jdbc;

import com.exasol.jdbc.functional.FunctionPreparedStatement;
import com.exasol.jdbc.functional.FunctionResultSet;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.sql.*;

public class ManagedConnection implements AutoCloseable {


public enum Feature {
    F_PARTITIONS // table partitions
    , F_KERBEROS_AUTH // user authentication through Kerberos
    , F_PRIORITY_GROUPS // flexible priority groups
    , F_SCHEMA_QUOTA // raw quota on schema level
    , F_PASSWORD_POLICIES // password policies and expiry
    , F_IMPERSONATION // user impersonation
    , F_SNAPSHOT_MODE // EXASOL-2901: System Table Snapshot Mode
    , F_TLS_FINGERPRINT // EXASOL-2936: Fingerprint in DRIVERS
}

// the JDBC connection to be managed
private final Connection m_connection;
// session ID of Exasol connection
private final long m_sessionId;

/**
 * Create a new ManagedConnection on a given Connection.
 * <p>
 * If the given Connection is to Exasol, it will automatically retrieve the current SessionId
 * </p>
 *
 * @param p_connection Existing Connection; preferably to Exasol
 * @throws SQLException if anything goes wrong
 */
public ManagedConnection( final Connection p_connection ) throws SQLException {
    m_connection = p_connection;
    m_dbVersion = new ComparableVersion( p_connection.getMetaData().getDatabaseProductVersion() );
    if( p_connection instanceof AbstractEXAConnection ) {
        m_sessionId = ((AbstractEXAConnection) m_connection).getSessionID();
    } else {
        m_sessionId = -1;
    }
}

/**
 * interface AutoCloseable
 */
@Override
public void close() {
    if( null != m_connection ) {
        try {
            if( !m_connection.isClosed() ) {
                m_connection.close();
            }
        }
        catch( SQLException ignored ) {
        }
    }
}

/**
 * Return the session Id of the Connection.
 *
 * @return -1 if the Connection is not to Exasol
 */
public long getSessionId() {
    return m_sessionId;
}

/**
 * Closure interface for auto-close of ResultSet and Statement.
 *
 * @param sqlText statement text (SELECT) to be executed.
 * @param closure The given closure will be called once per row in the ResultSet; its single argument is the result set.
 *                While it is not forbidden for the closure to change the result sets cursor (calling next/first, ...)
 *                , it is not exactly expected.
 * @return Number of times the closure was called (should be number of result rows)
 * @throws SQLException if executing the query fails
 */
public long eachRow( final String sqlText, FunctionResultSet closure ) throws SQLException {
    long callCounter = 0;
    try (
            Statement stmt = m_connection.createStatement();
            ResultSet rs = stmt.executeQuery( sqlText )
    ) {
        while (rs.next()) {
            callCounter++;
            closure.apply( rs );
        }
        return callCounter;
    }
}

/**
 * Closure interface for auto-close of ResultSet and PreparedStatement.
 *
 * @param sqlText statement text (SELECT) to be executed.
 * @param params  Array of parameter values for the statement. Note that this is a one-dimensional array, the statement is executed only once.
 * @param closure The given closure will be called once per row in the ResultSet; its single argument is the result set.
 *                While it is not forbidden for the closure to change the result set cursor (calling next/first, ...)
 *                , it is not exactly expected.
 * @return Number of times the closure was called (should be number of result rows)
 * @throws SQLException if the operation fails
 */
public long eachRowPrepared( final String sqlText, final Object[] params, FunctionResultSet closure ) throws SQLException {
    long callCounter = 0;
    try (
            PreparedStatement stmt = m_connection.prepareStatement( sqlText )
    ) {
        for (int i = 0; i < params.length; ++i) {
            stmt.setObject( i + 1, params[i] );
        }

        try (
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                callCounter++;
                closure.apply( rs );
            }
        }
        return callCounter;
    }
}

/**
 * Execute SQL statement returning a rowcount (basically anything except SELECT).
 *
 * @param sqlText statement text to execute
 * @return Number of rows affected, or -1 on ignored error
 * @throws SQLException On database error, when not ignored through m_errorMode
 */
public long executeUpdate( final String sqlText ) throws SQLException {
    try (
            Statement stmt = m_connection.createStatement()
    ) {
        // https://www.exasol.com/support/browse/IDEA-426 -- executeLargeUpdate is missing
        return stmt.executeUpdate( sqlText );
    }
}


/**
 * Execute SQL prepared statement returning a rowcount (basically anything except SELECT).
 *
 * @param sqlText statement text to execute
 * @param params  Array of parameters for the statement. Note that this is a one-dimensional array, the statement is executed only once.
 * @return Number of rows affected, or -1 on ignored error
 * @throws SQLException On database error, when not ignored through m_errorMode
 */
public long executeUpdatePrepared( final String sqlText, final Object[] params ) throws SQLException {
    try (
            PreparedStatement stmt = m_connection.prepareStatement( sqlText )
    ) {
        for (int i = 0; i < params.length; ++i) {
            stmt.setObject( i + 1, params[i] );
        }
        // https://www.exasol.com/support/browse/IDEA-426 -- executeLargeUpdate is missing
        return stmt.executeUpdate();
    }
}


/**
 * Get a raw Statement from the Connection.
 * <p>
 * Note that you will have to take care of closing the Statement yourself!
 * </p>
 *
 * @return raw Statement for the connected Connection
 * @throws SQLException If something goes wrong
 * @deprecated If you ever need this method, please create a feature request to implement the missing managed part.
 */
@Deprecated
public Statement createStatement() throws SQLException {
    return m_connection.createStatement();
}

/**
 * Create a PreparedStatement on the Connection.
 *
 * @param sqlText SQL text for the PreparedStatement. use '? for parameter placeholders.
 * @return the prepared Statement
 * @throws SQLException If the prepare fails for some reason
 * @deprecated This method is not in-line with the whole "managed autocloseable" approach.
 * Use {@link #withPrepare(String, FunctionPreparedStatement)} instead!
 */
@Deprecated
public PreparedStatement prepareStatement( final String sqlText ) throws SQLException {
    return m_connection.prepareStatement( sqlText );
}

/**
 * Prepare the given statement text and call the closure with the resulting PreparedStatement.
 * <p>
 * The statement is automatically closed when #closure is done.
 * Note that with Exasol, PreparedStatement.close() is actually executing some stuff and may even throw exceptions!
 * </p>
 *
 * @param sqlText The SQL text to prepare. Use question marks '?' for parameter placeholders
 * @param closure The user code operating on the prepared Statement
 * @return whatever the closure returned
 * @throws SQLException When either closure or PreparedStatement.close throws
 */
public long withPrepare( final String sqlText, FunctionPreparedStatement closure ) throws SQLException {
    try (
            PreparedStatement stmt = m_connection.prepareStatement( sqlText )
    ) {
        return closure.apply( stmt );
    }
}

/**
 * Set Autocommit mode of Connection
 *
 * @param mode true to enable autocommit (== default after connect)
 * @throws SQLException if the operation fails
 */
public void setAutocommit( boolean mode ) throws SQLException {
    m_connection.setAutoCommit( mode );
}


/**
 * versioning / feature management
 */
// the database version retrieved in constructor
private final ComparableVersion m_dbVersion;

/**
 * Check if the current database version supports the given feature.
 * <p>
 * Note that the check is based on database version only, it can not test for specific database settings!
 * </p>
 * @param feature the feature to check
 * @return {@code true} if the current database supports the given feature
 * @see #hasFeature(ComparableVersion, Feature)
 */
public boolean hasFeature( Feature feature ) {
    return hasFeature( m_dbVersion, feature );
}

/**
 * Check if the given Exasol version does support the given feature
 * <p>
 * If the given version is null or empty, this returns <b>true </b>
 * </p>
 *
 * @param p_version Exasol Version string, eg. "6.0.8" or "6.1.rc1"
 * @param p_feature the feature to check
 * @return {@code true} if the database supports the given feature
 * @see #hasFeature(ComparableVersion, Feature)
 */
public static boolean hasFeature( String p_version, Feature p_feature ) {
    if( null == p_version || p_version.isEmpty() ) {
        // unspecified version --> all features
        return true;
    }
    return hasFeature( new ComparableVersion( p_version ), p_feature );
}


/**
 * Relevant fixed versions for feature comparisons
 */
private static final ComparableVersion VERSION_6_0_8 = new ComparableVersion( "6.0.8" );
private static final ComparableVersion VERSION_6_1_RC1 = new ComparableVersion( "6.1.rc1" );
private static final ComparableVersion VERSION_7_1_0 = new ComparableVersion( "7.1.0" );

/**
 * Check if the given Exasol database version supports the given feature.
 *
 * @param p_version Exasol version number, like "6.1.0" or "7.0.alpha-1"
 * @param p_feature The Feature to check for
 * @return True if the given version does support the given feature.
 */
public static boolean hasFeature( ComparableVersion p_version, Feature p_feature ) {
    switch (p_feature) {
        case F_KERBEROS_AUTH:
            return 0 <= p_version.compareTo( VERSION_6_0_8 );
        case F_PARTITIONS:
        case F_SCHEMA_QUOTA:
        case F_PRIORITY_GROUPS:
        case F_PASSWORD_POLICIES:
        case F_IMPERSONATION:
            return 0 <= p_version.compareTo( VERSION_6_1_RC1 );
        case F_SNAPSHOT_MODE:
        case F_TLS_FINGERPRINT:
            return 0 <= p_version.compareTo( VERSION_7_1_0 );
    }

    // should never happen: switch/case must be feature-complete!
    assert (false);
    return false;
}

/**
 * Return the version of the Exasol database behind this Connection.
 * @return the Exasol database version
 */
public ComparableVersion getVersion() {
    return m_dbVersion;
}


// end of class. Go home.
}

