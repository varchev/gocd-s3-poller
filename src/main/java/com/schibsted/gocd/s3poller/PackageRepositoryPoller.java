package com.schibsted.gocd.s3poller;

import com.amazonaws.services.s3.AmazonS3Client;
import com.schibsted.gocd.s3poller.message.CheckConnectionResultMessage;
import com.schibsted.gocd.s3poller.message.PackageMaterialProperties;
import com.schibsted.gocd.s3poller.message.PackageRevisionMessage;

import static java.util.Arrays.asList;

public class PackageRepositoryPoller {

    private PackageRepositoryConfigurationProvider configurationProvider;
    private AmazonS3Client client;

    public PackageRepositoryPoller(PackageRepositoryConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public CheckConnectionResultMessage checkConnectionToRepository(PackageMaterialProperties repositoryConfiguration) {
        // check repository connection here
        return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, asList("success message"));
    }

    public CheckConnectionResultMessage checkConnectionToPackage(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        // check package connection here
        return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, asList("success message"));
    }

    public PackageRevisionMessage getLatestRevision(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        // get latest modification here
        return new PackageRevisionMessage();
    }

    public PackageRevisionMessage getLatestRevisionSince(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration, PackageRevisionMessage previousPackageRevision) {
        // get latest modification since here
        return new PackageRevisionMessage();

    }
}
