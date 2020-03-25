package com.exasol.jdbc;

import com.exasol.containers.ExasolContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ITgetConnection {

protected final ExasolContainer<? extends ExasolContainer<?>> database = new ExasolContainer<>().withRequiredServices();

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
    Connection c = assertDoesNotThrow( () -> JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "sys", "exasol") );
    assertDoesNotThrow( c::close );
}

@Test
void testLoginBad() {
    assertThrows( SQLException.class, () -> JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "sys", "exa sol") );
}

@Test
void testLoginNonDefault() {
    assertDoesNotThrow( () -> {
        try (
                Connection c = JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "sys", "exasol");
                Statement s = c.createStatement()
        ) {
            s.executeUpdate( "create user X identified by \"abC\"" );
            // done with autocommit

            LoginRefused err = assertThrows(LoginRefused.class, () -> JdbcConnectionFactory.getConnection( database.getJdbcUrl(), "x", "abC") );
            assertTrue( err.getMessage().contains( "insufficient privileges: CREATE SESSION" ) );
        }
    }
    );
}

@Test
void testBrokenURL() {
    SQLException err = assertThrows(SQLException.class, () -> JdbcConnectionFactory.getConnection( "https://www.exasol.com", "sys", "exasol" ) );
    assertTrue( err.getMessage().startsWith( "No suitable driver found" ));
}

@Test
void testNoHost() {
    ConnectFailed err = assertThrows(ConnectFailed.class, () -> JdbcConnectionFactory.getConnection( "jdbc:exa:www.exasol.com:8563", "sys", "exasol" ) );
    assertEquals( "connect timed out", err.getMessage() );
}


}