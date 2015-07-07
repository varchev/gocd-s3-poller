package com.schibsted.gocd.s3poller;

import com.schibsted.gocd.s3poller.message.PackageRevisionMessage;
import junit.framework.TestCase;

public class JsonUtilTest extends TestCase {

    public void testToJsonString() throws Exception {
        PackageRevisionMessage prm = new PackageRevisionMessage();
        assertEquals("{}", JsonUtil.toJsonString(prm));
    }
}
