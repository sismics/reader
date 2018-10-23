package com.sismics.reader.rest;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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
        assertEquals(1, jobs.length());
        JSONObject job = (JSONObject) jobs.get(0);
        String jobId = job.getString("id");
        assertNotNull(jobId);
        assertEquals("import", job.optString("name"));
        assertNotNull(job.optString("start_date"));
        assertNotNull(job.optString("end_date"));
        assertEquals(4, job.optInt("feed_success"));
        assertEquals(0, job.optInt("feed_failure"));
        assertEquals(4, job.optInt("feed_total"));
        assertEquals(0, job.optInt("starred_success"));
        assertEquals(0, job.optInt("starred_failure"));
        assertEquals(0, job.optInt("starred_total"));

        // User job2 deletes user1's job KO : forbidden
        login("job2");
        DELETE("/job/" + jobId);
        assertIsBadRequest(); // TODO return forbidden status in this case
        json = getJsonResult();
        assertEquals("ForbiddenError", json.getString("type"));

        // User job1 deletes his job
        login("job1");
        DELETE("/job/" + jobId);
        json = getJsonResult();
        assertEquals("ok", json.getString("status"));
        assertIsOk();

        // Check that the job was deleted
        GET("/user");
        assertIsOk();
        json = getJsonResult();
        jobs = json.getJSONArray("jobs");
        assertEquals(0, jobs.length());
    }
}