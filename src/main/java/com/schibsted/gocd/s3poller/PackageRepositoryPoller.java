package com.schibsted.gocd.s3poller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.schibsted.gocd.s3poller.message.CheckConnectionResultMessage;
import com.schibsted.gocd.s3poller.message.PackageMaterialProperties;
import com.schibsted.gocd.s3poller.message.PackageRevisionMessage;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import static java.util.Arrays.asList;

public class PackageRepositoryPoller {

    private PackageRepositoryConfigurationProvider configurationProvider;
    private AmazonS3Client client;

    private final Logger log = Logger.getLoggerFor(this.getClass());

    public PackageRepositoryPoller(PackageRepositoryConfigurationProvider configurationProvider, AmazonS3Client client) {
        this.configurationProvider = configurationProvider;
        this.client = client;
    }

    public CheckConnectionResultMessage checkConnectionToRepository(PackageMaterialProperties repositoryConfiguration) {
        String bucketName = repositoryConfiguration.getProperty(Constants.S3_BUCKET).value();
        Boolean bucketExists = false;
        try {
            bucketExists = client.doesBucketExist(bucketName);
        } catch (Exception ex) {
            return new CheckConnectionResultMessage(
                CheckConnectionResultMessage.STATUS.FAILURE,
                asList("Could not find bucket. [" + ex.getMessage() + "]"));
        }
        if (bucketExists) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, asList("Bucket found"));
        }
        return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, asList("Bucket not found"));
    }

    public CheckConnectionResultMessage checkConnectionToPackage(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        String bucketName = repositoryConfiguration.getProperty(Constants.S3_BUCKET).value();
        String path = packageConfiguration.getProperty(Constants.S3_PATH).value();
        ObjectListing listing;
        try {
            listing = client.listObjects(bucketName, path);
        } catch (Exception ex) {
            return new CheckConnectionResultMessage(
                CheckConnectionResultMessage.STATUS.FAILURE,
                asList("Could not find path '" + path + "' in bucket '" + bucketName + "'. [" + ex.getMessage() + "]"));
        }
        if (!listing.getObjectSummaries().isEmpty()) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, asList("Objects found on path"));
        }
        return new CheckConnectionResultMessage(
            CheckConnectionResultMessage.STATUS.FAILURE,
            asList("Could not find objects in path. Folder can't be empty."));
    }

    public PackageRevisionMessage getLatestRevision(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        String bucketName = repositoryConfiguration.getProperty(Constants.S3_BUCKET).value();
        String path = packageConfiguration.getProperty(Constants.S3_PATH).value();
        ObjectListing listing;
        try {
            listing = client.listObjects(bucketName, path);
        } catch (Exception ex) {
            log.error("error getting object list", ex);
            return new PackageRevisionMessage();
        }
        List<S3ObjectSummary> s3Objects = listing.getObjectSummaries();
        if (s3Objects.isEmpty()) {
            log.error("empty object summaries");
            return new PackageRevisionMessage();
        }
        S3ObjectSummary latest = getLatest(s3Objects,null);

        while(listing.isTruncated()){
            listing = client.listNextBatchOfObjects(listing);
            latest = getLatest(s3Objects, latest);
        }

        String filename = latest.getKey().substring(path.length());
        ObjectMetadata obj = client.getObjectMetadata(bucketName, latest.getKey());

        String version = obj.getUserMetaDataOf("version");
        String sha = obj.getUserMetaDataOf("sha");
        String buildUrl = obj.getUserMetaDataOf("build");

        // String revision, Date timestamp, String user, String revisionComment, String trackbackUrl
        return new PackageRevisionMessage(
            version,
            latest.getLastModified(),
            "S3",
            "Object at " + latest.getKey() + " with date " + latest.getLastModified().toString() + " located at " +
            client.getUrl(bucketName, latest.getKey()).toString() + " for SHA " + sha ,
            buildUrl
        );
    }

    private S3ObjectSummary getLatest(List<S3ObjectSummary> s3Objects, S3ObjectSummary latest ) {
        if(latest == null){
            latest = s3Objects.get(0);
        }
        for (S3ObjectSummary s3Object : s3Objects) {
            if (s3Object.getLastModified().after(latest.getLastModified())) {
                latest = s3Object;
            }
        }
        return latest;
    }

    public PackageRevisionMessage getLatestRevisionSince(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration, PackageRevisionMessage previousPackageRevision) {
        PackageRevisionMessage prm = getLatestRevision(packageConfiguration, repositoryConfiguration);

        if(prm == null){
            log.info("latest revision returned null.");
            return null;
        }

        if(previousPackageRevision == null){
            log.info("previous revision passed null.");
            return null;
        }

        if(prm.getTimestamp() == null){
            log.info("latest revision timestamp is null.");
            return null;
        }

        if (prm.getTimestamp().after(previousPackageRevision.getTimestamp())) {
            return prm;
        }
        return null;

    }
}
