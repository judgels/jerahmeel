package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CourseSessionModel.class)
public abstract class CourseSessionModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<CourseSessionModel, Long> id;
	public static volatile SingularAttribute<CourseSessionModel, String> courseJid;
	public static volatile SingularAttribute<CourseSessionModel, String> sessionJid;
	public static volatile SingularAttribute<CourseSessionModel, String> alias;
	public static volatile SingularAttribute<CourseSessionModel, Boolean> completeable;
}
