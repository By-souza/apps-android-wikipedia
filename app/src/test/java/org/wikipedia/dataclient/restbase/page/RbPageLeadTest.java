package org.wikipedia.dataclient.restbase.page;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.wikipedia.dataclient.Service;
import org.wikipedia.dataclient.mwapi.page.MwMobileViewPageLead;
import org.wikipedia.dataclient.page.BasePageLeadTest;
import org.wikipedia.dataclient.page.PageClient;
import org.wikipedia.dataclient.page.PageLead;
import org.wikipedia.testlib.TestLatch;

import okhttp3.CacheControl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.wikipedia.json.GsonUnmarshaller.unmarshal;

public class RbPageLeadTest extends BasePageLeadTest {
    private PageClient subject;

    @Before @Override public void setUp() throws Throwable {
        super.setUp();
        subject = new RbPageClient();
    }

    @Test public void testEnglishMainPage() throws Exception {
        RbPageLead props = unmarshal(RbPageLead.class, getEnglishMainPageJson());
        verifyEnglishMainPage(props);
    }

    @Test public void testUnprotectedDisambiguationPage() throws Exception {
        RbPageLead props = unmarshal(RbPageLead.class, getUnprotectedDisambiguationPageJson());
        verifyUnprotectedDisambiguationPage(props);
    }

    /**
     * Custom deserializer; um, yeah /o\.
     * An earlier version had issues with protection settings that don't include "edit" protection.
     */
    @Test public void testProtectedButNoEditProtectionPage() throws Exception {
        RbPageLead props = unmarshal(RbPageLead.class, getProtectedButNoEditProtectionPageJson());
        verifyProtectedNoEditProtectionPage(props);
    }

    @Test @SuppressWarnings("checkstyle:magicnumber") public void testThumbUrls() throws Throwable {
        enqueueFromFile("page_lead_rb.json");
        final TestLatch latch = new TestLatch();
        subject.lead(service(Service.class), CacheControl.FORCE_NETWORK, null, null, "foo", 640)
                .enqueue(new Callback<PageLead>() {
                    @Override
                    public void onResponse(@NonNull Call<PageLead> call, @NonNull Response<PageLead> response) {
                        RbPageLead lead = (RbPageLead) response.body();
                        assertThat(lead.getLeadImageUrl(640).contains("640px"), is(true));
                        assertThat(lead.getThumbUrl().contains(preferredThumbSizeString()), is(true));
                        assertThat(lead.getDescription(), is("Mexican boxer"));
                        assertThat(lead.getDescriptionSource(), is("central"));
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(@NonNull Call<PageLead> call, @NonNull Throwable t) {
                        fail();
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test public void testError() throws Exception {
        MwMobileViewPageLead pageLead = unmarshal(MwMobileViewPageLead.class, getErrorJson());
        MwMobileViewPageLead.Mobileview props = pageLead.getMobileview();
        verifyError(pageLead, props);
    }

    @NonNull @Override protected PageClient subject() {
        return subject;
    }
}
