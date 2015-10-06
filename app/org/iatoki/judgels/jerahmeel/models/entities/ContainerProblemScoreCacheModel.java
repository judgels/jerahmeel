package org.iatoki.judgels.jerahmeel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_container_score_cache")
public final class ContainerProblemScoreCacheModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String containerJid;

    public String problemJid;

    public String userJid;

    public double score;
}
