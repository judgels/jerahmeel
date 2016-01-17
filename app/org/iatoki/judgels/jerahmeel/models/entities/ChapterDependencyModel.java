package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_chapter_dependency")
public final class ChapterDependencyModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String chapterJid;

    public String dependedChapterJid;
}
