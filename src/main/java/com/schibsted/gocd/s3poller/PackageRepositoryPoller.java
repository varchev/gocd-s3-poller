package com.schibsted.gocd.s3poller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.schibsted.gocd.s3poller.message.CheckConnectionResultMessage;
import com.schibsted.gocd.s3poller.message.PackageMaterialProperties;
import com.schibsted.gocd.s3poller.message.PackageRevisionMessage;

import java.util.List;

import static java.util.Arrays.asList;

public class PackageRepositoryPoller {

    private PackageRepositoryConfigurationProvider configurationProvider;
    private AmazonS3Client client;

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
            return new PackageRevisionMessage();
        }
        List<S3ObjectSummary> s3Objects = listing.getObjectSummaries();
        if (s3Objects.isEmpty()) {
            return new PackageRevisionMessage();
        }
        S3ObjectSummary latest = s3Objects.get(0);
        for (S3ObjectSummary s3Object : s3Objects) {
            if (s3Object.getLastModified().after(latest.getLastModified())) {
                latest = s3Object;
            }
        }
        // String revision, Date timestamp, String user, String revisionComment, String trackbackUrl
        return new PackageRevisionMessage(
            latest.getKey(),
            latest.getLastModified(),
            latest.getOwner().getDisplayName(),
            "Object at " + latest.getKey() + " with date " + latest.getLastModified().toString(),
            client.getUrl(bucketName, latest.getKey()).toString()
        );
    }

    public PackageRevisionMessage getLatestRevisionSince(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration, PackageRevisionMessage previousPackageRevision) {
        PackageRevisionMessage prm = getLatestRevision(packageConfiguration, repositoryConfiguration);
        if (prm.getTimestamp().after(previousPackageRevision.getTimestamp())) {
            return prm;
        }
        return null;

    }
}
