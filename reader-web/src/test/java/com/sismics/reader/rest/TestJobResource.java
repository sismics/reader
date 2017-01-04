package com.sismics.reader.rest;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Exhaustive test of the job resource.
 * 
 * @author jtremeaux
 */
public class TestJobResource extends BaseJerseyTest {

    /**
     * Test of the job resource.
     * 
     */
    @Test
    public void testJobResource() throws Exception {
        // Create user job1
        createUser("job1");

        // Create user job2
        createUser("job2");

        // Import an OPML file
        login("job1");
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/greader_subscriptions.xml");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        PUT("/subscription/import", form);
        assertIsOk();

        // Check the user's job
        GET("/user");
        assertIsOk();
        JSONObject json = getJsonResult();
        JSONArray jobs = json.getJSONArray("jobs");
        Assert.assertEquals(1, jobs.length());
        JSONObject job = (JSONObject) jobs.get(0);
        String jobId = job.getString("id");
        Assert.assertNotNull(jobId);
        Assert.assertEquals("import", job.optString("name"));
        Assert.assertNotNull(job.optString("start_date"));
        Assert.assertNotNull(job.optString("end_date"));
        Assert.assertEquals(4, job.optInt("feed_success"));
        Assert.assertEquals(0, job.optInt("feed_failure"));
        Assert.assertEquals(4, job.optInt("feed_total"));
        Assert.assertEquals(0, job.optInt("starred_success"));
        Assert.assertEquals(0, job.optInt("starred_failure"));
        Assert.assertEquals(0, job.optInt("starred_total"));

        // User job2 deletes user1's job KO : forbidden
        login("job2");
        DELETE("/job/" + jobId);
        assertIsBadRequest(); // TODO return forbidden status in this case
        json = getJsonResult();
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User job1 deletes his job
        login("job1");
        DELETE("/job/" + jobId);
        json = getJsonResult();
        Assert.assertEquals("ok", json.getString("status"));
        assertIsOk();

        // Check that the job was deleted
        GET("/user");
        assertIsOk();
        json = getJsonResult();
        jobs = json.getJSONArray("jobs");
        Assert.assertEquals(0, jobs.length());
    }
}