package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CurriculumCourseModel.class)
public abstract class CurriculumCourseModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<CurriculumCourseModel, Long> id;
	public static volatile SingularAttribute<CurriculumCourseModel, String> curriculumJid;
	public static volatile SingularAttribute<CurriculumCourseModel, String> alias;
	public static volatile SingularAttribute<CurriculumCourseModel, String> courseJid;
	public static volatile SingularAttribute<CurriculumCourseModel, Boolean> completeable;

}

