package org.iatoki.judgels.jerahmeel.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.play.JudgelsProperties;
import org.iatoki.judgels.jerahmeel.JerahmeelProperties;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

public final class JerahmeelApplicationLoader extends GuiceApplicationLoader {

    @Override
    public GuiceApplicationBuilder builder(Context context) {
        org.iatoki.judgels.jerahmeel.BuildInfo$ buildInfo = org.iatoki.judgels.jerahmeel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());

        Config config = ConfigFactory.load();
        JerahmeelProperties.buildInstance(config);

        return super.builder(context);
    }
}
