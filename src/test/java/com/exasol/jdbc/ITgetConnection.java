package com.exasol.jdbc;

import com.exasol.containers.ExasolContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ITgetConnection {

protected final ExasolContainer<? extends ExasolContainer<?>> database = new ExasolContainer<>().withRequiredServices().withReuse(true);

@BeforeAll
void startContainer() {
    database.start();
}

@AfterAll
void stopContainer() {
    database.stop();
}

@Test
void testLoginGood() {
    Connection c = assertDoesNotThrow( () -> JdbcConnectionFactory
            .getConnection( database.getJdbcUrl(), "sys", "exasol" ) );
    assertDoesNotThrow( c::close );
}

@Test
void testLoginBad() {
    assertThrows( SQLException.class, () -> JdbcConnectionFactory
            .getConnection( database.getJdbcUrl(), "sys", "exa sol" ) );
}

@Test
void testLoginNonDefault() {
    assertDoesNotThrow( () -> {
                try (
                        Connection c = JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "sys", "exasol" );
                        Statement s = c.createStatement()
                ) {
                    s.executeUpdate( "create user X identified by \"abC\"" );
                    // done with autocommit

                    SQLInvalidAuthorizationSpecException err = assertThrows( SQLInvalidAuthorizationSpecException.class, () -> JdbcConnectionFactory
                            .getConnection( database.getJdbcUrl(), "x", "abC" ) );
                    assertTrue( err.getMessage().contains( "insufficient privileges: CREATE SESSION" ) );
                }
            }
    );
}

@Test
void testBrokenURL() {
    SQLException err = assertThrows( SQLException.class, () -> JdbcConnectionFactory
            .getConnection( "https://www.exasol.com", "sys", "exasol" ) );
    assertTrue( err.getMessage().startsWith( "No suitable driver found" ) );
}

@Test
void testNoHost() {
    ConnectFailed err = assertThrows( ConnectFailed.class, () -> JdbcConnectionFactory
            .getConnection( "jdbc:exa:www.exasol.com:8563", "sys", "exasol" ) );
    assertEquals( "java.net.SocketTimeoutException: connect timed out", err.getMessage() );
}

@Test
void testDefaultClient() {
    String name = JdbcConnectionFactory.getClientName();
    assertNotNull( name );
    System.out.println( name );


    String version = JdbcConnectionFactory.getClientVersion();
    assertNotNull( version );
    System.out.println( version );

    try (
            Connection c = JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "sys", "exasol" );
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery( "select * from exa_user_sessions where session_id = current_session" )
    ) {
        assertTrue( rs.next() );
        assertEquals( name + " " + version, rs.getString( "CLIENT" ) );
    }
    catch( SQLException exception ) {
        fail( exception );
    }
}

@Test
void testClientName() {
    JdbcConnectionFactory.setClientData( "testclient", "0.0.0.1" );
    assertEquals( "testclient", JdbcConnectionFactory.getClientName() );
    assertEquals( "0.0.0.1", JdbcConnectionFactory.getClientVersion() );

    try (
            Connection c = JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "sys", "exasol" );
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery( "select * from exa_user_sessions where session_id = current_session" )
    ) {
        assertTrue( rs.next() );
        assertEquals( "testclient 0.0.0.1", rs.getString( "CLIENT" ) );
    }
    catch( SQLException exception ) {
        fail( exception );
    }
}

}
