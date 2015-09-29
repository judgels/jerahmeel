package org.iatoki.judgels.jerahmeel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.util.Providers;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielFactory;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonClientAPI;
import org.iatoki.judgels.api.sandalphon.SandalphonFactory;
import org.iatoki.judgels.api.sealtiel.SealtielClientAPI;
import org.iatoki.judgels.api.sealtiel.SealtielFactory;
import org.iatoki.judgels.jerahmeel.JerahmeelProperties;
import org.iatoki.judgels.jerahmeel.services.impls.UserServiceImpl;
import org.iatoki.judgels.jophiel.JophielAuthAPI;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.sandalphon.SandalphonBundleProblemGrader;
import org.iatoki.judgels.sandalphon.services.BundleProblemGrader;

public class JerahmeelModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        bind(JophielAuthAPI.class).toInstance(jophielAuthAPI());
        bind(JophielClientAPI.class).toInstance(jophielClientAPI());
        bind(JophielPublicAPI.class).toInstance(jophielPublicAPI());
        bind(SandalphonClientAPI.class).toInstance(sandalphonClientAPI());
        bind(SealtielClientAPI.class).toInstance(sealtielClientAPI());

        bind(FileSystemProvider.class).annotatedWith(BundleSubmissionLocalFileSystemProvider.class).toInstance(bundleSubmissionLocalFileSystemProvider());

        FileSystemProvider bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider();
        if (bundleSubmissionRemoteFileSystemProvider != null) {
            bind(FileSystemProvider.class).annotatedWith(BundleSubmissionRemoteFileSystemProvider.class).toInstance(bundleSubmissionRemoteFileSystemProvider);
        } else {
            bind(FileSystemProvider.class).annotatedWith(BundleSubmissionRemoteFileSystemProvider.class).toProvider(Providers.of(null));
        }

        bind(FileSystemProvider.class).annotatedWith(ProgrammingSubmissionLocalFileSystemProvider.class).toInstance(submissionLocalFileSystemProvider());

        FileSystemProvider submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider();
        if (submissionRemoteFileSystemProvider != null) {
            bind(FileSystemProvider.class).annotatedWith(ProgrammingSubmissionRemoteFileSystemProvider.class).toInstance(submissionRemoteFileSystemProvider);
        } else {
            bind(FileSystemProvider.class).annotatedWith(ProgrammingSubmissionRemoteFileSystemProvider.class).toProvider(Providers.of(null));
        }

        bindConstant().annotatedWith(GabrielClientJid.class).to(gabrielClientJid());
        bind(BaseUserService.class).to(UserServiceImpl.class);
        bind(BundleProblemGrader.class).to(SandalphonBundleProblemGrader.class);
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jerahmeel.models.daos.hibernate";
    }

    @Override
    protected String getServicesImplPackage() {
        return "org.iatoki.judgels.jerahmeel.services.impls";
    }

    private JerahmeelProperties jerahmeelProperties() {
        return JerahmeelProperties.getInstance();
    }

    private JophielAuthAPI jophielAuthAPI() {
        return new JophielAuthAPI(jerahmeelProperties().getJophielBaseUrl(), jerahmeelProperties().getJophielClientJid(), jerahmeelProperties().getJophielClientSecret());
    }

    private JophielClientAPI jophielClientAPI() {
        return JophielFactory.createJophiel(jerahmeelProperties().getJophielBaseUrl()).connectToClientAPI(jerahmeelProperties().getJophielClientJid(), jerahmeelProperties().getJophielClientSecret());
    }

    private JophielPublicAPI jophielPublicAPI() {
        return JophielFactory.createJophiel(jerahmeelProperties().getJophielBaseUrl()).connectToPublicAPI();
    }

    private SandalphonClientAPI sandalphonClientAPI() {
        return SandalphonFactory.createSandalphon(jerahmeelProperties().getSandalphonBaseUrl()).connectToClientAPI(jerahmeelProperties().getSandalphonClientJid(), jerahmeelProperties().getSandalphonClientSecret());
    }

    private SealtielClientAPI sealtielClientAPI() {
        return SealtielFactory.createSealtiel(jerahmeelProperties().getSealtielBaseUrl()).connectToClientAPI(jerahmeelProperties().getSealtielClientJid(), jerahmeelProperties().getSealtielClientSecret());
    }

    private FileSystemProvider bundleSubmissionRemoteFileSystemProvider() {
        FileSystemProvider bundleSubmissionRemoteFileSystemProvider = null;
        if (jerahmeelProperties().isSubmissionUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (jerahmeelProperties().isSubmissionAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(jerahmeelProperties().getSubmissionAWSAccessKey(), jerahmeelProperties().getSubmissionAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            bundleSubmissionRemoteFileSystemProvider = new AWSFileSystemProvider(awsS3Client, jerahmeelProperties().getSubmissionAWSS3BucketName(), jerahmeelProperties().getSubmissionAWSS3BucketRegion());
        }

        return bundleSubmissionRemoteFileSystemProvider;
    }

    private FileSystemProvider bundleSubmissionLocalFileSystemProvider() {
        return new LocalFileSystemProvider(jerahmeelProperties().getSubmissionLocalDir());
    }

    private FileSystemProvider submissionRemoteFileSystemProvider() {
        FileSystemProvider submissionRemoteFileSystemProvider = null;
        if (jerahmeelProperties().isSubmissionUsingAWSS3()) {
            AmazonS3Client awsS3Client;
            if (jerahmeelProperties().isSubmissionAWSUsingKeys()) {
                awsS3Client = new AmazonS3Client(new BasicAWSCredentials(jerahmeelProperties().getSubmissionAWSAccessKey(), jerahmeelProperties().getSubmissionAWSSecretKey()));
            } else {
                awsS3Client = new AmazonS3Client();
            }
            submissionRemoteFileSystemProvider = new AWSFileSystemProvider(awsS3Client, jerahmeelProperties().getSubmissionAWSS3BucketName(), jerahmeelProperties().getSubmissionAWSS3BucketRegion());
        }

        return submissionRemoteFileSystemProvider;
    }

    private FileSystemProvider submissionLocalFileSystemProvider() {
        return new LocalFileSystemProvider(jerahmeelProperties().getSubmissionLocalDir());
    }

    private String gabrielClientJid() {
        return jerahmeelProperties().getSealtielGabrielClientJid();
    }
}
