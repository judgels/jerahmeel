package org.iatoki.judgels.jerahmeel.models.domains;

import org.iatoki.judgels.gabriel.commons.models.domains.AbstractSubmissionModel;
import org.iatoki.judgels.sandalphon.commons.models.domains.AbstractBundleSubmissionModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "jerahmeel_bundle_submission")
public final class BundleSubmissionModel extends AbstractBundleSubmissionModel {
}
