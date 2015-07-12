package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SessionLessonModel.class)
public abstract class SessionLessonModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<SessionLessonModel, Long> id;
	public static volatile SingularAttribute<SessionLessonModel, String> sessionJid;
	public static volatile SingularAttribute<SessionLessonModel, String> lessonJid;
	public static volatile SingularAttribute<SessionLessonModel, String> lessonSecret;
	public static volatile SingularAttribute<SessionLessonModel, String> alias;
	public static volatile SingularAttribute<SessionLessonModel, String> status;

}

