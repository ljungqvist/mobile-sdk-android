/*
 *    Copyright 2018 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdk;


import com.appnexus.opensdk.shadows.ShadowAsyncTaskNoExecutor;
import com.appnexus.opensdk.shadows.ShadowCustomWebView;
import com.appnexus.opensdk.shadows.ShadowSettings;
import com.appnexus.opensdk.shadows.ShadowWebSettings;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.Assert.assertTrue;

@Config(constants = BuildConfig.class, sdk = 21,
        shadows = {ShadowAsyncTaskNoExecutor.class,
                ShadowCustomWebView.class, ShadowWebSettings.class, ShadowSettings.class, ShadowLog.class})
@RunWith(RobolectricTestRunner.class)
public class VideoImplementationTest extends BaseViewAdTest {

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        ShadowCustomWebView.simulateVideoError = false; // Reset the value if not next test case will fail Global instance.
        ShadowCustomWebView.simulateDelayedVideoError = false; // Reset the value if not next test case will fail Global instance.
    }

     //This test Succes onAdLoaded for Video
     //Also test the Adfetcher state is Stopped for Video on Succesful AdLoad
    @Test
    public void testVideoImplementationAdLoadSuccess() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.rtbVASTVideo()));
        executeBannerRequest();
        assertCallbacks(true);
        assertTrue(bannerAdView.mAdFetcher.getState() == AdFetcher.STATE.STOPPED);
    }




    // This Tests onAdFailed is called succesfully
    // Also tests AutoRefresh is on for onAdFailed
    @Test
    public void testVideoImplementationAdLoadFailure() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.rtbVASTVideo()));
        ShadowCustomWebView.simulateVideoError = true;

        executeBannerRequest();
        assertCallbacks(false);
        assertTrue(bannerAdView.mAdFetcher.getState() == AdFetcher.STATE.AUTO_REFRESH);
    }


    // After we receive adReady we fire AdLoaded and stop autorefresh
    // If we receive video-error after adReady but before video-complete then we should turn on Autorefresh again
    // This tests that scenario
    @Test
    public void testToggleAutoRefresh() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResponsesUT.rtbVASTVideo()));
        ShadowCustomWebView.simulateDelayedVideoError = true;
        executeBannerRequest();
        assertCallbacks(true);
        assertTrue(bannerAdView.mAdFetcher.getState() == AdFetcher.STATE.STOPPED);

        waitForTasks();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        assertTrue(bannerAdView.mAdFetcher.getState() == AdFetcher.STATE.AUTO_REFRESH);

    }




    private void executeBannerRequest(){
        bannerAdView.setAutoRefreshInterval(30);
        bannerAdView.loadAdOffscreen();

        waitForTasks();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        waitForTasks();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }


}
