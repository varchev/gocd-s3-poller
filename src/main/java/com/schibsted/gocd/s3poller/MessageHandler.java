package com.schibsted.gocd.s3poller;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public interface MessageHandler {
    GoPluginApiResponse handle(GoPluginApiRequest request);
}
