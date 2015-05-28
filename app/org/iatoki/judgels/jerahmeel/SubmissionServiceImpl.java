package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.sandalphon.commons.AbstractSubmissionServiceImpl;
import org.iatoki.judgels.sealtiel.client.Sealtiel;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.GradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.SubmissionDao;
import org.iatoki.judgels.jerahmeel.models.domains.GradingModel;
import org.iatoki.judgels.jerahmeel.models.domains.SubmissionModel;

public final class SubmissionServiceImpl extends AbstractSubmissionServiceImpl<SubmissionModel, GradingModel> {
    public SubmissionServiceImpl(SubmissionDao submissionDao, GradingDao gradingDao, Sealtiel sealtiel, String gabrielClientJid) {
        super(submissionDao, gradingDao, sealtiel, gabrielClientJid);
    }
}
