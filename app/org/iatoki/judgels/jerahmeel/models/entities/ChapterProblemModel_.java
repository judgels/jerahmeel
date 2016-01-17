package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ChapterProblemModel.class)
public abstract class ChapterProblemModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ChapterProblemModel, Long> id;
	public static volatile SingularAttribute<ChapterProblemModel, String> chapterJid;
	public static volatile SingularAttribute<ChapterProblemModel, String> problemJid;
	public static volatile SingularAttribute<ChapterProblemModel, String> problemSecret;
	public static volatile SingularAttribute<ChapterProblemModel, String> alias;
	public static volatile SingularAttribute<ChapterProblemModel, String> type;
	public static volatile SingularAttribute<ChapterProblemModel, String> status;
}
