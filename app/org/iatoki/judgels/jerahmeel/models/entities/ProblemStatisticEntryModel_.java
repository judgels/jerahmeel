package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ProblemStatisticEntryModel.class)
public abstract class ProblemStatisticEntryModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ProblemStatisticEntryModel, Long> id;
	public static volatile SingularAttribute<ProblemStatisticEntryModel, String> problemStatisticJid;
	public static volatile SingularAttribute<ProblemStatisticEntryModel, String> problemJid;
	public static volatile SingularAttribute<ProblemStatisticEntryModel, Long> totalSubmissions;
}
