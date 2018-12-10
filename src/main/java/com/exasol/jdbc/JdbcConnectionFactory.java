package com.exasol.jdbc;

import java.awt.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Connection Provider to handle database connectivity.
 *
 * @author Stefan Reich, Exasol (2018-05-23)
 */
public class JdbcConnectionFactory {

    // maximum number of retries for interactive passwords
    private static final int MAX_PASSWORD_ATTEMPTS = 3;



/**
 * Method that will use 'best available' way to get a password from interactive user input
 *
 * @param prompt Text to prompt the user for password
 * @return User input as String
 * @throws IOException on error
 */
public static String readPassword( String prompt ) throws IOException {
    String pwd = null;
    try {
        if( !GraphicsEnvironment.isHeadless() ) {
            System.out.println( "System is not headless... could use popup window for password entry" );
            // TODO Someone with AWT/Swing knowledge could implement this
        }
        Console c = System.console();
        return new String( c.readPassword( prompt ) );
    }
    catch( NullPointerException e ) {
        System.out.print( "\nWARNING --- Password will be echoed --- WARNING\t" );
        System.out.print( prompt + " " );

        InputStreamReader isr = new InputStreamReader( System.in );
        BufferedReader br = new BufferedReader( isr );
        return br.readLine();
    }
}

/**
 * Connect to database using given parameters.
 *
 * @param connectionString JDBC connection string. Preferably an Exasol database (null -> ENV -> localhost)
 * @param userName user name for authentication (null -> ENV -> sys)
 * @param password password for authentication (null -> interactive)
 * @return JDBC Connection object after successful login
 * @throws SQLException On error (connect failed, authentication error, ...)
 */
public static Connection getConnection( String connectionString, String userName, String password ) throws SQLException {
    // find/load driver
    try {
        Class.forName( "com.exasol.jdbc.EXADriver" );
    }
    catch( final ClassNotFoundException e ) {
        throw new SQLException( e.getMessage() );
    }

    if( null==connectionString ) {
        connectionString = System.getenv( "CONNECTIONSTRING" );
        if( null == connectionString ) {
            // localhost
            connectionString = "jdbc:exa:localhost:8563";
        }
    }

    if( null==userName ) {
        // override user name from ENV
        userName = System.getenv( "DB_USER" );
        if( null == userName ) {
            // fallback to sys
            userName = "sys";
        }
    }

    // TODO: get password from cache and connect without asking
    if( null==password ) {
        for( int t = 1; t <= MAX_PASSWORD_ATTEMPTS; ++t ) {
            try {
                password = readPassword( "Password for " + userName + "@" + connectionString + "? " );
                if( null==password || 0==password.length() ) {
                    break;
                }
                Connection c = getConnection( connectionString, userName, password );
                if( null!=c ) {
                    // TODO: store password in cache
                }
                return c;
            }
            catch( IOException e ) {
                throw new SQLException( e.getMessage() );
            }
            catch( SQLException e ) {
                System.out.println( "Error: " + e.getMessage() );
            }
        }
        System.out.println( "Giving up." );
        throw new SQLException( "Failed to connect" );
    }


    return DriverManager.getConnection(
            // TODO: configurable client name
            connectionString + ";clientname=MetaDump",
            userName,
            password
    );
}


/**
 * Connect to database using given user name and cached password.
 * If no password is cached, the user is prompted to enter one. In this case, three attempts are granted.
 *
 * @param connectionString JDBC connection string. Preferable an Exasol database (null -> ENV -> default)
 * @param userName User name for authentication (null -> ENV -> default)
 * @return JDBC Connection object after successful login
 * @throws SQLException When connection fails with cached password, or MAX_PASSWORD_ATTEMPTS interactive attempts failed.
 */
public static Connection getConnection( String connectionString, String userName ) throws SQLException {
    return getConnection(connectionString, userName, null );
}


/**
 * Connect to database using default user name.
 * Default is 'sys' but can be overridden using environment variable "DB_USER".
 * Password will be taken from internal cache or interactively from user.
 *
 * @param connectionString JDBC Connection string, preferably to an Exasol database; (null -> ENV -> default))
 * @return JDBC Connection object after successful login
 * @throws SQLException When login was not possible
 */
public static Connection getConnection( String connectionString ) throws SQLException {
    return getConnection( connectionString, null, null );
}


/**
 * Connect to database using all default values:
 * - Connection String: 'localhost' or environment variable "CONNECTIONSTRING"
 * - User Name: 'sys' or environment variable "DB_USER"
 * - Password: From internal cache or interactive user input
 *
 * @return JDBC Connection object after successful login
 * @throws SQLException When login was not possible
 */
public static Connection getConnection() throws SQLException {
    return getConnection( null, null, null );
}


// end of class
}
