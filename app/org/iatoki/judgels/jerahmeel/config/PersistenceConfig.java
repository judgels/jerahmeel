package org.iatoki.judgels.jerahmeel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.jerahmeel.JerahmeelProperties;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.sandalphon.Sandalphon;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.iatoki.judgels.jerahmeel.models.daos",
        "org.iatoki.judgels.jerahmeel.services",
})
public class PersistenceConfig {

    @Bean
    public JudgelsProperties judgelsProperties() {
        org.iatoki.judgels.jerahmeel.BuildInfo$ buildInfo = org.iatoki.judgels.jerahmeel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());
        return JudgelsProperties.getInstance();
    }

    @Bean
    public JerahmeelProperties jerahmeelProperties() {
        Config config = ConfigFactory.load();
        JerahmeelProperties.buildInstance(config);
        return JerahmeelProperties.getInstance();
    }

    @Bean
    public Jophiel jophiel() {
        return new Jophiel(jerahmeelProperties().getJophielBaseUrl(), jerahmeelProperties().getJophielClientJid(), jerahmeelProperties().getJophielClientSecret());
    }

    @Bean
    public Sandalphon sandalphon() {
        return new Sandalphon(jerahmeelProperties().getSandalphonBaseUrl(), jerahmeelProperties().getSandalphonClientJid(), jerahmeelProperties().getSandalphonClientSecret());
    }

    @Bean
    public Sealtiel sealtiel() {
        return new Sealtiel(jerahmeelProperties().getSealtielBaseUrl(), jerahmeelProperties().getSealtielClientJid(), jerahmeelProperties().getSealtielClientSecret());
    }

    @Bean
    public FileSystemProvider bundleSubmissionRemoteFileSystemProvider() {
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

    @Bean
    public FileSystemProvider bundleSubmissionLocalFileSystemProvider() {
        return new LocalFileSystemProvider(jerahmeelProperties().getSubmissionLocalDir());
    }


    @Bean
    public FileSystemProvider submissionRemoteFileSystemProvider() {
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

    @Bean
    public FileSystemProvider submissionLocalFileSystemProvider() {
        return new LocalFileSystemProvider(jerahmeelProperties().getSubmissionLocalDir());
    }

    @Bean
    public String gabrielClientJid() {
        return jerahmeelProperties().getSealtielGabrielClientJid();
    }
}
