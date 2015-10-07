package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ContainerScoreCacheModel.class)
public abstract class ContainerScoreCacheModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ContainerScoreCacheModel, Long> id;
	public static volatile SingularAttribute<ContainerScoreCacheModel, String> containerJid;
	public static volatile SingularAttribute<ContainerScoreCacheModel, String> userJid;
	public static volatile SingularAttribute<ContainerScoreCacheModel, Double> score;
}
