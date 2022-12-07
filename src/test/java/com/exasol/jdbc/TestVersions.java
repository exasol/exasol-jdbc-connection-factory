package com.exasol.jdbc;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class TestVersions {

@Mock
private Connection connectionMock;
@Mock
private DatabaseMetaData metadataMock;

@Test
void testBasic() throws Exception {
    doReturn( metadataMock ).when( this.connectionMock ).getMetaData();
    doReturn( "1.2.3-rc5" ).when( this.metadataMock ).getDatabaseProductVersion();

    ManagedConnection mc = assertDoesNotThrow( () -> new ManagedConnection( this.connectionMock ) );
    assertNotNull( mc );
    assertEquals( -1, mc.getSessionId() );
    assertEquals( new ComparableVersion( "1.2.3-rc5" ), mc.getVersion() );
}


@Test
void test621() throws Exception {
    doReturn( metadataMock ).when( this.connectionMock ).getMetaData();
    doReturn( "6.2.1" ).when( this.metadataMock ).getDatabaseProductVersion();
    ManagedConnection mc = assertDoesNotThrow( () -> new ManagedConnection( this.connectionMock ) );

    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_KERBEROS_AUTH ) );
    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_IMPERSONATION ) );
}

@Test
void test600() throws Exception {
    doReturn( metadataMock ).when( this.connectionMock ).getMetaData();
    doReturn( "6.0.0" ).when( this.metadataMock ).getDatabaseProductVersion();
    ManagedConnection mc = assertDoesNotThrow( () -> new ManagedConnection( this.connectionMock ) );

    assertFalse( mc.hasFeature( ManagedConnection.Feature.F_KERBEROS_AUTH ) );
    assertFalse( mc.hasFeature( ManagedConnection.Feature.F_IMPERSONATION ) );
}

@Test
void test700() throws Exception {
    doReturn( metadataMock ).when( this.connectionMock ).getMetaData();
    doReturn( "7.0.0" ).when( this.metadataMock ).getDatabaseProductVersion();
    ManagedConnection mc = assertDoesNotThrow( () -> new ManagedConnection( this.connectionMock ) );

    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_KERBEROS_AUTH ) );
    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_IMPERSONATION ) );
    assertFalse( mc.hasFeature( ManagedConnection.Feature.F_SNAPSHOT_MODE ) );
}

@Test
void test710() throws Exception {
    doReturn( metadataMock ).when( this.connectionMock ).getMetaData();
    doReturn( "7.1.0" ).when( this.metadataMock ).getDatabaseProductVersion();
    ManagedConnection mc = assertDoesNotThrow( () -> new ManagedConnection( this.connectionMock ) );

    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_KERBEROS_AUTH ) );
    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_IMPERSONATION ) );
    assertTrue( mc.hasFeature( ManagedConnection.Feature.F_SNAPSHOT_MODE ) );
}


@Test
void testNamed() {
    assertTrue( ManagedConnection.hasFeature( "6.2.0", ManagedConnection.Feature.F_PARTITIONS ) );
    assertFalse( ManagedConnection
            .hasFeature( new ComparableVersion( "5.0.18" ), ManagedConnection.Feature.F_PASSWORD_POLICIES ) );

    assertTrue( ManagedConnection.hasFeature( "", ManagedConnection.Feature.F_PASSWORD_POLICIES ) );
}

}
