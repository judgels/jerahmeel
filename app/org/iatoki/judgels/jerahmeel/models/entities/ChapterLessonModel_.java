package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ChapterLessonModel.class)
public abstract class ChapterLessonModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ChapterLessonModel, Long> id;
	public static volatile SingularAttribute<ChapterLessonModel, String> chapterJid;
	public static volatile SingularAttribute<ChapterLessonModel, String> lessonJid;
	public static volatile SingularAttribute<ChapterLessonModel, String> lessonSecret;
	public static volatile SingularAttribute<ChapterLessonModel, String> alias;
	public static volatile SingularAttribute<ChapterLessonModel, String> status;
}
