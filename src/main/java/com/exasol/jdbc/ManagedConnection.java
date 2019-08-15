package com.exasol.jdbc;

import com.exasol.jdbc.functional.FunctionResultSet;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.sql.*;

public class ManagedConnection implements AutoCloseable {

public enum OperationMode {
    OM_NORMAL // execute statements
    , OM_VERBOSE // execute and print statements
    , OM_DRYRUN // print statements to screen
}

public enum ErrorMode {
    EM_LOG_CONTINUE // print errors but continue
    , EM_IGNORE_CONTINUE // ignore errors and go on
    , EM_THROW // throw errors
}



public enum Feature {
    F_PARTITIONS // table partitions
    , F_KERBEROS_AUTH // user authentication through Kerberos
    , F_PRIORITY_GROUPS // flexible priority groups
    , F_SCHEMA_QUOTA // raw quota on schema level
    , F_PASSWORD_POLICIES // password policies and expiry
    , F_IMPERSONATION // user impersonation
}


private final Connection m_connection;
private OperationMode m_operationMode;
private ErrorMode m_errorMode;
private final long m_sessionId;


public ManagedConnection( final Connection p_connection ) throws SQLException {
    m_connection = p_connection;
    m_operationMode = OperationMode.OM_NORMAL;
    m_errorMode = ErrorMode.EM_THROW;
    m_dbVersion = new ComparableVersion(  p_connection.getMetaData().getDatabaseProductVersion() );
    if( p_connection instanceof  AbstractEXAConnection ) {
        m_sessionId = ((AbstractEXAConnection) m_connection).getSessionID();
    } else {
        m_sessionId = -1;
    }
}

/**
 * interface Autocloseable
 */
@Override
public void close() throws SQLException {
    if( null!=m_connection && !m_connection.isClosed() ) {
        m_connection.close();
    }
}

/**
 * Getter and Setter
 */
public OperationMode getOperationMode() {
    return m_operationMode;
}

public void setOperationMode( OperationMode o ) {
    m_operationMode = o;
}

public ErrorMode getErrorMode() {
    return m_errorMode;
}

public void setErrorMode( ErrorMode errorMode ) {
    this.m_errorMode = errorMode;
}

public long getSessionId() {
    return m_sessionId;
}

/**
 * Closure interface for auto-close of ResultSet and Statement.
 *
 * @param sqlText statement text (SELECT) to be executed.
 * @param closure The given closure will be called once per row in the ResultSet; its single argument is the result set.
 *                While it is not forbidden for the closure to change the result sets cursor (callig next/first, ...)
 *                , it is not exactly expected.
 *
 * @return Number of times the closure was called (should be number of result rows)
 */
public long eachRow( final String sqlText, FunctionResultSet closure ) throws SQLException {
    long callCounter = 0;
    try (
            Statement stmt = m_connection.createStatement();
            ResultSet rs = stmt.executeQuery( sqlText );
    ) {
        while( rs.next() ) {
            callCounter++;
            closure.apply( rs );
        }
        return callCounter;
    }
    catch( SQLException e ) {
        switch( m_errorMode ) {
            case EM_THROW:
                throw e;
            case EM_LOG_CONTINUE:
                System.out.println( String.format( "Error executing query >>%s<<:\n\t%s", sqlText, e.getMessage() ) );
                return callCounter;
            case EM_IGNORE_CONTINUE:
                return callCounter;
        }
        throw new SQLFeatureNotSupportedException( "Unexpected error mode " + m_operationMode );
    }
}

/**
 * Closure interface for auto-close of ResultSet and PreparedStatement.
 *
 * @param sqlText statement text (SELECT) to be executed.
 * @param params Array of parameter values for the statement. Note that this is a one-dimensional array, the statement is executed only once.
 * @param closure The given closure will be called once per row in the ResultSet; its single argument is the result set.
 *                While it is not forbidden for the closure to change the result sets cursor (callig next/first, ...)
 *                , it is not exactly expected.
 * @return Number of times the closure was called (should be number of result rows)
 */
public long eachRowPrepared( final String sqlText, final Object[] params, FunctionResultSet closure ) throws SQLException {
    long callCounter = 0;
    try (
            PreparedStatement stmt = m_connection.prepareStatement( sqlText );
    ) {
        for( int i=0; i<params.length; ++i ) {
            stmt.setObject( i+1, params[i] );
        }

        try (
            ResultSet rs = stmt.executeQuery()
        ) {
            while( rs.next() ) {
                callCounter++;
                closure.apply( rs );
            }
        }
        return callCounter;
    }
    catch( SQLException e ) {
        switch( m_errorMode ) {
            case EM_THROW:
                throw e;
            case EM_LOG_CONTINUE:
                System.out.println( String.format( "Error executing query >>%s<<:\n\t%s", sqlText, e.getMessage() ) );
                return callCounter;
            case EM_IGNORE_CONTINUE:
                return callCounter;
        }
        throw new SQLFeatureNotSupportedException( "Unexpected error mode " + m_operationMode );
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
            Statement stmt = m_connection.createStatement();
    ) {
        switch( m_operationMode ) {
            case OM_DRYRUN:
                System.out.println( "-- DRY_RUN --\n" + sqlText );
                return 0;
            case OM_VERBOSE:
                System.out.println( "-- EXECUTE --\n" + sqlText );
                // fall-through to execution
            case OM_NORMAL:
                // https://www.exasol.com/support/browse/IDEA-426 -- executeLargeUpdate is missing
                return stmt.executeUpdate( sqlText );
        }
        throw new SQLFeatureNotSupportedException( "Unexpected operation mode " + m_operationMode );
    }
    catch( SQLException e ) {
        switch( m_errorMode ) {
            case EM_THROW:
                throw e;
            case EM_LOG_CONTINUE:
                System.out.println( String.format( "Error executing statement >>%s<<:\n\t%s", sqlText, e.getMessage() ) );
            case EM_IGNORE_CONTINUE:
                return -1;
        }
        throw new SQLFeatureNotSupportedException( "Unexpected error mode " + m_operationMode );
    }
}


/**
 * Execute SQL prepared statement returning a rowcount (basically anything except SELECT).
 *
 * @param sqlText statement text to execute
 * @param params Array of parameters for the statement. Note that this is a one-dimensional array, the statement is executed only once.
 * @return Number of rows affected, or -1 on ignored error
 * @throws SQLException On database error, when not ignored through m_errorMode
 */
public long executeUpdatePrepared( final String sqlText, final Object[] params ) throws SQLException {
    try (
            PreparedStatement stmt = m_connection.prepareStatement( sqlText );
    ) {
        for( int i=0; i<params.length; ++i ) {
            stmt.setObject( i+1, params[i] );
        }

        switch( m_operationMode ) {
            case OM_DRYRUN:
                System.out.println( "-- DRY_RUN --\n" + sqlText );
                return 0;
            case OM_VERBOSE:
                System.out.println( "-- EXECUTE --\n" + sqlText );
                // fall-through to execution
            case OM_NORMAL:
                // https://www.exasol.com/support/browse/IDEA-426 -- executeLargeUpdate is missing
                return stmt.executeUpdate();
        }
        throw new SQLFeatureNotSupportedException( "Unexpected operation mode " + m_operationMode );
    }
    catch( SQLException e ) {
        switch( m_errorMode ) {
            case EM_THROW:
                throw e;
            case EM_LOG_CONTINUE:
                System.out.println( String.format( "Error executing statement >>%s<<:\n\t%s", sqlText, e.getMessage() ) );
                return -1;
            case EM_IGNORE_CONTINUE:
                return -1;
        }
        throw new SQLFeatureNotSupportedException( "Unexpected error mode " + m_operationMode );
    }
}


@Deprecated
public Statement createStatement() throws SQLException {
    return m_connection.createStatement();
}

@Deprecated
public PreparedStatement prepareStatement( final String sqlText ) throws SQLException {
    return m_connection.prepareStatement( sqlText );
}


/**
 * Set Autocommit mode of Connection
 *
 * @param mode true to enable autocommit (== default after connect)
 */
public void setAutocommit( boolean mode ) throws SQLException {
    m_connection.setAutoCommit( mode );
}



/**
 *
 * Versioning / Feature management
 *
 */
private final ComparableVersion m_dbVersion;


public boolean hasFeature( Feature feature ) {
    return hasFeature(m_dbVersion, feature);
}


public static boolean hasFeature( String p_version, Feature p_feature ) {
    if( null == p_version || p_version.isEmpty() ) {
        // unspecified version --> all features
        return true;
    }
    return hasFeature( new ComparableVersion( p_version ), p_feature );
}


/**
 * Relevant versions for feature comparisons
 */
private static final ComparableVersion VERSION_6_0_8 = new ComparableVersion( "6.0.8" );
private static final ComparableVersion VERSION_6_1_RC1 = new ComparableVersion( "6.1.rc1" );

public static boolean hasFeature( ComparableVersion p_version, Feature p_feature ) {
    switch( p_feature ) {
        case F_KERBEROS_AUTH:
            return 0 <= p_version.compareTo( VERSION_6_0_8 );
        case F_PARTITIONS:
        case F_SCHEMA_QUOTA:
        case F_PRIORITY_GROUPS:
        case F_PASSWORD_POLICIES:
        case F_IMPERSONATION:
            return 0 <= p_version.compareTo( VERSION_6_1_RC1 );
    }

    // should never happen: switch/case must be feature-complete!
    assert(false);
    return false;
}

public ComparableVersion getVersion() {
    return m_dbVersion;
}


// end of class. Go home.
}

