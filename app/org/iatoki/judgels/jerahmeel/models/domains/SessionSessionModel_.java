package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SessionSessionModel.class)
public abstract class SessionSessionModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<SessionSessionModel, Long> id;
	public static volatile SingularAttribute<SessionSessionModel, String> sessionJid;
	public static volatile SingularAttribute<SessionSessionModel, String> dependedSessionJid;

}

