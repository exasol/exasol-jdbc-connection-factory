package com.exasol.jdbc;


import com.exasol.containers.ExasolContainer;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ITmanagedConnection {

protected final ExasolContainer<? extends ExasolContainer<?>> database;
protected final Connection connection;
protected final ManagedConnection managedConnection;


public ITmanagedConnection() {
    database = new ExasolContainer<>().withRequiredServices();
    database.start();
    connection = assertDoesNotThrow( () -> database.createConnection( "" ) );
    managedConnection = assertDoesNotThrow( () -> new ManagedConnection( connection ) );
}

@AfterAll
void closeAll() {
    if( null != managedConnection ) {
        managedConnection.close();
    }
    if( null != connection ) {
        // managed must close this
        assertTrue( assertDoesNotThrow( connection::isClosed ) );
    }
    database.stop();
}

@Test
void testAutoClose() {
    Connection c = assertDoesNotThrow( () -> database.createConnection( "" ) );
    try (
            ManagedConnection mc = assertDoesNotThrow( () -> new ManagedConnection( c ) )
    ) {
        assertNotNull( mc.getVersion() );
    }
    assertTrue( assertDoesNotThrow( c::isClosed ) );
}

@Test
void testEachRow() {
    final AtomicReference<Long> sessionId = new AtomicReference<>();
    assertDoesNotThrow( () ->
            managedConnection.eachRow(
                    "select CURRENT_SESSION"
                    , ( ResultSet rs ) -> sessionId.set( rs.getLong( 1 ) )
            )
    );

    assertEquals( managedConnection.getSessionId(), sessionId.get() );
}

@Test
void testEachRowThrowSql() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.eachRow(
                    "select syntax_error"
                    , ( ResultSet rs ) -> {
                    }
            )
    );
    assertTrue( err.getMessage().startsWith( "object SYNTAX_ERROR not found" ) );
}

@Test
void testEachRowThrowInner() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.eachRow(
                    "select CURRENT_SESSION"
                    , ( ResultSet rs ) -> rs.getLong( 22 )
            )
    );
    assertTrue( err.getMessage().contains( "ArrayIndexOutOfBoundsException" ) );
}

@Test
void testEachRowPrepared() {
    assertEquals(
            3
            , assertDoesNotThrow( () ->
                    managedConnection.eachRowPrepared(
                            "select ? union select ? union select ?"
                            , new Object[]{1, 2, 3}
                            , ( ResultSet rs ) ->
                                    assertTrue( rs.getInt( 1 ) <= 3 )
                    )
            )
    );
}

@Test
void testEachRowPreparedThrowSQL() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.eachRowPrepared(
                    "select syntax error ?"
                    , new Object[]{1}
                    , rs -> {
                    }
            )
    );
    assertTrue( err.getMessage().contains( "unexpected '?'" ) );
}

@Test
void testEachRowPreparedThrowParam() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.eachRowPrepared(
                    "select ?,?,?"
                    , new Object[]{1, 2}
                    , rs -> {
                    }
            )
    );
    assertTrue( err.getMessage().startsWith( "Too few parameters" ) );
}

@Test
void testEachRowPreparedThrowInner() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.eachRowPrepared(
                    "select ?, ?, ?"
                    , new Object[]{1, 2, 3}
                    , rs -> rs.getLong( 22 )
            )
    );
    assertTrue( err.getMessage().contains( "ArrayIndexOutOfBoundsException" ) );
}


@Test
void textExecuteUpdate() {
    assertEquals(
            0
            , assertDoesNotThrow( () -> managedConnection.executeUpdate( "create schema update_test_1" ) )
    );

    // assert autocommit was on
    assertEquals(
            1
            , assertDoesNotThrow( () ->
                    managedConnection.eachRow(
                            "select * from exa_schemas where schema_name = 'UPDATE_TEST_1'"
                            , rs -> {
                            }
                    )
            )
    );
}

@Test
void testExecuteUpdateThrowSql() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.executeUpdate(
                    "drop schema not there"
            )
    );
    assertTrue( err.getMessage().contains( "unexpected NOT_" ) );
}

@Test
void testExecuteUpdateThrowSelect() {
    SQLException err = assertThrows( SQLException.class, () ->
            managedConnection.executeUpdate( "select 17" )
    );
    assertEquals( "Statement returned resultset.", err.getMessage().trim() );
}

@Test
void testExecuteUpdatePrepared() {
    assertDoesNotThrow( () -> managedConnection.executeUpdate( "create schema update_prepare" ) );
    assertDoesNotThrow( () -> managedConnection
            .executeUpdate( "create table update_prepare.T1(A int, B varchar(10) )" ) );

    assertEquals(
            2 // rows affected
            , assertDoesNotThrow( () -> managedConnection.executeUpdatePrepared(
                    "insert into update_prepare.T1 "
                            + "select ? as A, ? as B "
                            + "union all select ?, ?"
                    , new Object[]{1, "XXX", 2, "YYY"}
                    )
            )
    );

    final AtomicInteger sumA = new AtomicInteger( 0 );
    assertEquals(
            2
            , assertDoesNotThrow( () ->
                    managedConnection.eachRow(
                            "select A from update_prepare.T1"
                            , rs -> sumA.addAndGet( rs.getInt( 1 ) )
                    )
            )
    );
    assertEquals( 3, sumA.get() );
}

@Test
void testExecuteUpdatePreparedThrowSQL() {
    assertDoesNotThrow( () -> managedConnection.executeUpdate( "create schema update_prepare_fail1" ) );
    assertDoesNotThrow( () -> managedConnection
            .executeUpdate( "create table update_prepare_fail1.T1(A int, B varchar(10) )" ) );

    SQLException err = assertThrows( SQLException.class, () -> managedConnection.executeUpdatePrepared(
            "drop schema ?"
            , new Object[]{"XXX"}
            )
    );
    assertTrue( err.getMessage().contains( "unexpected '?'" ) );
}

@Test
void testExecuteUpdatePreparedThrowParam() {
    assertDoesNotThrow( () -> managedConnection.executeUpdate( "create schema update_prepare_fail2" ) );
    assertDoesNotThrow( () -> managedConnection
            .executeUpdate( "create table update_prepare_fail2.T1(A int, B varchar(10) )" ) );

    SQLException err = assertThrows( SQLException.class, () -> managedConnection.executeUpdatePrepared(
            "insert into update_prepare_fail2.T1(A) values ?"
            , new Object[]{"XXX", 5}
            )
    );
    assertTrue(
            err.getMessage().startsWith( "Number of parameters doesn't match." ) );
}

@Test
void testExecuteUpdatePreparedThrowData() {
    assertDoesNotThrow( () -> managedConnection.executeUpdate( "create schema update_prepare_fail3" ) );
    assertDoesNotThrow( () -> managedConnection.executeUpdate( "create table update_prepare_fail3.T1(A int)" ) );

    DataException err = assertThrows( DataException.class, () -> managedConnection.executeUpdatePrepared(
            "insert into update_prepare_fail3.T1 values (?), (?), (?)"
            , new Object[]{1, 2, "XXX"}
            )
    );
    assertTrue( err.getMessage().contains( "cast; Value: 'XXX' in write of column T1.A" ) );
}


}
