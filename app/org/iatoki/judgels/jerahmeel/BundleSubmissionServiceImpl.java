package org.iatoki.judgels.jerahmeel;

import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleGradingDao;
import org.iatoki.judgels.jerahmeel.models.daos.interfaces.BundleSubmissionDao;
import org.iatoki.judgels.jerahmeel.models.domains.BundleGradingModel;
import org.iatoki.judgels.jerahmeel.models.domains.BundleSubmissionModel;
import org.iatoki.judgels.sandalphon.commons.AbstractBundleSubmissionServiceImpl;
import org.iatoki.judgels.sandalphon.commons.BundleProblemGrader;

public final class BundleSubmissionServiceImpl extends AbstractBundleSubmissionServiceImpl<BundleSubmissionModel, BundleGradingModel> {
    public BundleSubmissionServiceImpl(BundleSubmissionDao submissionDao, BundleGradingDao gradingDao, BundleProblemGrader bundleProblemGrader) {
        super(submissionDao, gradingDao, bundleProblemGrader);
    }
}
