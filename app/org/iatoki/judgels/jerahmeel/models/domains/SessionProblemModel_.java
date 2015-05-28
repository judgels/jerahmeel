package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.commons.models.domains.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SessionProblemModel.class)
public abstract class SessionProblemModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<SessionProblemModel, Long> id;
	public static volatile SingularAttribute<SessionProblemModel, String> sessionJid;
	public static volatile SingularAttribute<SessionProblemModel, String> problemJid;
	public static volatile SingularAttribute<SessionProblemModel, String> problemSecret;
	public static volatile SingularAttribute<SessionProblemModel, String> alias;
	public static volatile SingularAttribute<SessionProblemModel, String> type;
	public static volatile SingularAttribute<SessionProblemModel, String> status;

}

