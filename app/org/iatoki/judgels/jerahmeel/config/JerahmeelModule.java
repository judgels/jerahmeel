package org.iatoki.judgels.jerahmeel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.util.Providers;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.play.config.AbstractJudgelsModule;
import org.iatoki.judgels.jerahmeel.JerahmeelProperties;
import org.iatoki.judgels.jerahmeel.services.impls.UserServiceImpl;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sandalphon.services.BundleProblemGrader;
import org.iatoki.judgels.sealtiel.Sealtiel;

public class JerahmeelModule extends AbstractJudgelsModule {

    @Override
    protected void manualBinding() {
        bind(Jophiel.class).toInstance(jophiel());
        bind(Sandalphon.class).toInstance(sandalphon());
        bind(Sealtiel.class).toInstance(sealtiel());

        bind(FileSystemProvider.class).annotatedWith(BundleSubmissionLocalFile.class).toInstance(bundleSubmissionLocalFileSystemProvider());

        FileSystemProvider bundleSubmissionRemoteFileSystemProvider = bundleSubmissionRemoteFileSystemProvider();
        if (bundleSubmissionRemoteFileSystemProvider != null) {
            bind(FileSystemProvider.class).annotatedWith(BundleSubmissionRemoteFile.class).toInstance(bundleSubmissionRemoteFileSystemProvider);
        } else {
            bind(FileSystemProvider.class).annotatedWith(BundleSubmissionRemoteFile.class).toProvider(Providers.of(null));
        }

        bind(FileSystemProvider.class).annotatedWith(SubmissionLocalFile.class).toInstance(submissionLocalFileSystemProvider());

        FileSystemProvider submissionRemoteFileSystemProvider = submissionRemoteFileSystemProvider();
        if (submissionRemoteFileSystemProvider != null) {
            bind(FileSystemProvider.class).annotatedWith(SubmissionRemoteFile.class).toInstance(submissionRemoteFileSystemProvider);
        } else {
            bind(FileSystemProvider.class).annotatedWith(SubmissionRemoteFile.class).toProvider(Providers.of(null));
        }

        bindConstant().annotatedWith(GabrielClientJid.class).to(gabrielClientJid());
        bind(BaseUserService.class).to(UserServiceImpl.class);
        bind(BundleProblemGrader.class).to(Sandalphon.class);
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jerahmeel.models.daos.impls";
    }

    @Override
    protected String getServicesImplPackage() {
        return "org.iatoki.judgels.jerahmeel.services.impls";
    }

    private JerahmeelProperties jerahmeelProperties() {
        return JerahmeelProperties.getInstance();
    }

    private Jophiel jophiel() {
        return new Jophiel(jerahmeelProperties().getJophielBaseUrl(), jerahmeelProperties().getJophielClientJid(), jerahmeelProperties().getJophielClientSecret());
    }

    private Sandalphon sandalphon() {
        return new Sandalphon(jerahmeelProperties().getSandalphonBaseUrl(), jerahmeelProperties().getSandalphonClientJid(), jerahmeelProperties().getSandalphonClientSecret());
    }

    private Sealtiel sealtiel() {
        return new Sealtiel(jerahmeelProperties().getSealtielBaseUrl(), jerahmeelProperties().getSealtielClientJid(), jerahmeelProperties().getSealtielClientSecret());
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
