package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SessionDependencyModel.class)
public abstract class SessionDependencyModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<SessionDependencyModel, Long> id;
	public static volatile SingularAttribute<SessionDependencyModel, String> sessionJid;
	public static volatile SingularAttribute<SessionDependencyModel, String> dependedSessionJid;

}

