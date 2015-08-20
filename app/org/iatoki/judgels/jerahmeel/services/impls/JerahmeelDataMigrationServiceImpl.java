package org.iatoki.judgels.jerahmeel.services.impls;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class JerahmeelDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 1;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if (databaseVersion < 2) {
            migrateV1toV2();
        }
    }

    private void migrateV1toV2() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String programmingSubmissionTable = "jerahmeel_programming_submission";
        Statement statement = connection.createStatement();

        statement.execute("ALTER TABLE " + programmingSubmissionTable + " DROP containerJid;");
        statement.execute("ALTER TABLE " + programmingSubmissionTable + " CHANGE contestJid containerJid VARCHAR(255);");
    }
}
