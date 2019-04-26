package com.exasol;

import com.exasol.jdbc.JdbcConnectionFactory;
import com.exasol.jdbc.ManagedConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleTest_1 {
    public static void main(String[] args) throws SQLException {
        Connection c = JdbcConnectionFactory.getConnection();
        try (
            ManagedConnection mc = new ManagedConnection( c )
        ) {
            mc.eachRowPrepared(
                    "SELECT * from EXA_DBA_SESSIONS WHERE SESSION_ID > ?"
                    , new Object[] { 100 }
                    , ( ResultSet rs ) -> {
                        System.out.println( rs.getString( "SESSION_ID" ) );
                    }
            );
            System.out.println( "Our session: " + mc.getSessionId() );
        }
    }
}

