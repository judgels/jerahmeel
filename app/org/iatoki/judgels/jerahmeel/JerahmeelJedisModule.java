package org.iatoki.judgels.jerahmeel;

public final class JerahmeelJedisModule extends JerahmeelModule {

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jerahmeel.models.daos.jedishibernate";
    }
}
