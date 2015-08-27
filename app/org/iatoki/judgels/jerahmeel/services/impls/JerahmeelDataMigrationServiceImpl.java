package org.iatoki.judgels.jerahmeel.services.impls;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class JerahmeelDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 3;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if (databaseVersion < 2) {
            migrateV1toV2();
        }
        if (databaseVersion < 3) {
            migrateV2toV3();
        }
    }

    private void migrateV2toV3() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String jidCacheTable = "jerahmeel_jid_cache";
        Statement statement = connection.createStatement();
        String jidCacheQuery = "SELECT * FROM " + jidCacheTable + ";";
        ResultSet resultSet = statement.executeQuery(jidCacheQuery);

        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            String jid = resultSet.getString("jid");
            String displayName = resultSet.getString("displayName");

            if (jid.startsWith("JIDUSER")) {
                if (displayName.contains("(")) {
                    displayName = displayName.substring(0, displayName.indexOf("(") - 1);

                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + jidCacheTable + " SET displayName= ? WHERE id=" + id + ";");
                    preparedStatement.setString(1, displayName);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    private void migrateV1toV2() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String programmingSubmissionTable = "jerahmeel_programming_submission";
        String bundleSubmissionTable = "jerahmeel_bundle_submission";
        Statement statement = connection.createStatement();

        statement.execute("ALTER TABLE " + bundleSubmissionTable + " DROP containerJid;");
        statement.execute("ALTER TABLE " + bundleSubmissionTable + " CHANGE contestJid containerJid VARCHAR(255);");
        statement.execute("ALTER TABLE " + programmingSubmissionTable + " DROP containerJid;");
        statement.execute("ALTER TABLE " + programmingSubmissionTable + " CHANGE contestJid containerJid VARCHAR(255);");
    }
}
