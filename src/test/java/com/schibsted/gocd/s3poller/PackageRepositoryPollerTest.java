package com.schibsted.gocd.s3poller;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.schibsted.gocd.s3poller.message.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PackageRepositoryPollerTest {

    @Mock (answer = Answers.RETURNS_DEEP_STUBS)
    AmazonS3Client client;

    PackageRepositoryConfigurationProvider prcp;
    PackageRepositoryPoller prp;
    PackageMaterialProperties pmp;

    @Before
    public void setUp() {
        prcp = new PackageRepositoryConfigurationProvider();
        prp = new PackageRepositoryPoller(prcp, client);
        pmp = new PackageMaterialProperties();
    }

    @Test
    public void checkConnectionToRepositoryBucketExistTest() {

        when(client.doesBucketExist(anyString())).thenReturn(true);

        pmp.addPackageMaterialProperty(
            Constants.S3_BUCKET,
            new PackageMaterialProperty().withValue("mybucket"));
        CheckConnectionResultMessage ccrm = prp.checkConnectionToRepository(pmp);
        assertTrue(ccrm.success());
    }

    @Test
    public void checkConnectionToRepositoryBucketNonExistTest() {

        when(client.doesBucketExist(anyString())).thenReturn(false);

        pmp.addPackageMaterialProperty(
                Constants.S3_BUCKET,
                new PackageMaterialProperty().withValue("faultybucket"));
        CheckConnectionResultMessage ccrm = prp.checkConnectionToRepository(pmp);
        assertFalse(ccrm.success());
    }

    @Test
    public void checkConnectionToRepositoryFaultyClientTest() {

        when(client.doesBucketExist(anyString())).thenThrow(new AmazonClientException("message"));

        pmp.addPackageMaterialProperty(
                Constants.S3_BUCKET,
                new PackageMaterialProperty().withValue("faultybucket"));
        CheckConnectionResultMessage ccrm = prp.checkConnectionToRepository(pmp);
        assertFalse(ccrm.success());
    }

    @Test
    public void checkConnectionToPackageObjectExistTest() {

        when(client.listObjects("bucket", "path").getObjectSummaries()).thenReturn(getObjectsInBucket(2));

        pmp.addPackageMaterialProperty(
                Constants.S3_BUCKET,
                new PackageMaterialProperty().withValue("bucket"));
        pmp.addPackageMaterialProperty(
                Constants.S3_PATH,
                new PackageMaterialProperty().withValue("path"));
        CheckConnectionResultMessage ccrm = prp.checkConnectionToPackage(pmp, pmp);
        assertTrue(ccrm.success());
    }

    @Test
    public void checkConnectionToPackageObjectNonExistTest() {

        when(client.listObjects("bucket", "path").getObjectSummaries()).thenReturn(getObjectsInBucket(0));

        pmp.addPackageMaterialProperty(
                Constants.S3_BUCKET,
                new PackageMaterialProperty().withValue("bucket"));
        pmp.addPackageMaterialProperty(
                Constants.S3_PATH,
                new PackageMaterialProperty().withValue("path"));
        CheckConnectionResultMessage ccrm = prp.checkConnectionToPackage(pmp, pmp);
        assertFalse(ccrm.success());
    }

    @Test
    public void checkConnectionToPackageObjectFaultyClientTest() {

        when(client.listObjects(anyString(), anyString())).thenThrow(new AmazonClientException("Some message"));

        pmp.addPackageMaterialProperty(
                Constants.S3_BUCKET,
                new PackageMaterialProperty().withValue("bucket"));
        pmp.addPackageMaterialProperty(
                Constants.S3_PATH,
                new PackageMaterialProperty().withValue("path"));
        CheckConnectionResultMessage ccrm = prp.checkConnectionToPackage(pmp, pmp);
        assertFalse(ccrm.success());
    }

    private List<S3ObjectSummary> getObjectsInBucket(Integer count) {
        List<S3ObjectSummary> list = new ArrayList<S3ObjectSummary>();
        for (Integer i = 0; i < count; i++) {
            list.add(new S3ObjectSummary());
        }
        return list;
    }

}
