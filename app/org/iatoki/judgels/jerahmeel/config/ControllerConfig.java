package org.iatoki.judgels.jerahmeel.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.iatoki.judgels.jerahmeel.controllers",
        "org.iatoki.judgels.jophiel.controllers"
})
public class ControllerConfig {

}
