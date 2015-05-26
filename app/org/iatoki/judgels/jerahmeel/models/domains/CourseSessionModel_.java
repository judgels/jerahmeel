package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CourseSessionModel.class)
public abstract class CourseSessionModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<CourseSessionModel, Long> id;
	public static volatile SingularAttribute<CourseSessionModel, String> courseJid;
	public static volatile SingularAttribute<CourseSessionModel, String> sessionJid;

}

