package com.schibsted.gocd.s3poller;


import com.schibsted.gocd.s3poller.message.PackageMaterialProperties;
import com.schibsted.gocd.s3poller.message.PackageMaterialProperty;
import com.schibsted.gocd.s3poller.message.ValidationResultMessage;


public class PackageRepositoryConfigurationProvider {

    public PackageMaterialProperties repositoryConfiguration() {
        PackageMaterialProperties repositoryConfigurationResponse = new PackageMaterialProperties();
        repositoryConfigurationResponse.addPackageMaterialProperty(
            Constants.S3_BUCKET,
            new PackageMaterialProperty().withDisplayName("S3 Bucket").withDisplayOrder("0"));
        return repositoryConfigurationResponse;
    }

    public PackageMaterialProperties packageConfiguration() {
        PackageMaterialProperties packageConfigurationResponse = new PackageMaterialProperties();
        packageConfigurationResponse.addPackageMaterialProperty(
                Constants.S3_PATH,
                new PackageMaterialProperty().withDisplayName("S3 Path").withDisplayOrder("0"));
        return packageConfigurationResponse;
    }

    public ValidationResultMessage validateRepositoryConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        return new ValidationResultMessage();
    }

    public ValidationResultMessage validatePackageConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        return new ValidationResultMessage();
    }

}
