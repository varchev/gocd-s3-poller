package com.schibsted.gocd.s3poller.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class RepositoryConnectionMessage {

    @Expose
    @SerializedName("repository-configuration")
    private Map<String,PackageMaterialProperty> repositoryConfiguration;

    public PackageMaterialProperties getRepositoryConfiguration() {
        return new PackageMaterialProperties(repositoryConfiguration);
    }
}
