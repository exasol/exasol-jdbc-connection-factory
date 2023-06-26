package com.exasol.jdbc;

import com.exasol.containers.ExasolContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ITLargeUpdate {

    protected final ExasolContainer<? extends ExasolContainer<?>> database = new ExasolContainer<>().withRequiredServices().withReuse(true);

    @BeforeAll
    void startUp() {
        database.start();

        try (
                Connection c = database.createConnection()
        ) {
            c.createStatement().executeUpdate("create schema row_counter");
            c.createStatement().executeUpdate("create script count_rows(mul1, mul2) returns rowcount as\n" +
                    "d1 = decimal(mul1)\n" +
                    "d2 = decimal(mul2)\n" +
                    "return { rows_affected = d1*d2 }");
        } catch (SQLException e) {
            fail(e);
        }
    }

    @AfterAll
    void cleanUp()
    {
        try (
                Connection c = database.createConnection()
        ) {
            c.createStatement().executeUpdate("drop schema row_counter cascade");
        } catch (SQLException e) {
            fail(e);
        }
        database.stop();
    }

    // Make sure that executeUpdate() returns correct rowcounts within reasonable bounds
    @ParameterizedTest
    @ValueSource(longs = {1, 10, 100, 1000, 10000, 100000, 1000000})
    void testExecuteUpdate(long row_root) {
        try (
                ManagedConnection mc = new ManagedConnection(database.createConnection())
        ) {
            assertEquals(row_root*row_root, mc.executeUpdate(String.format(
                    "execute script row_counter.count_rows(%d, %d)", row_root, row_root
                )));
        } catch (SQLException e) {
            fail(e);
        }
    }

    // Make sure that executeUpdatePrepared() returns correct rowcounts within reasonable bounds
    @ParameterizedTest
    @ValueSource(longs = {1, 10, 100, 1000, 10000, 100000, 1000000})
    void testExecuteUpdatePrepared(long row_root) {
        try (
                ManagedConnection mc = new ManagedConnection(database.createConnection())
        ) {
            assertEquals(row_root*row_root, mc.executeUpdatePrepared(String.format(
                    "execute script row_counter.count_rows(%d, %d)", row_root, row_root
            ), new Object[]{}));
        } catch (SQLException e) {
            fail(e);
        }
    }
}
