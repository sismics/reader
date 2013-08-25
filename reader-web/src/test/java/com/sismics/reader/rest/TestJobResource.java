package com.sismics.reader.rest;

import com.sismics.reader.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
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
     * @throws Exception
     */
    @Test
    public void testJobResource() throws Exception {
        // Create user job1
        clientUtil.createUser("job1");
        String job1AuthToken = clientUtil.login("job1");

        // Create user job2
        clientUtil.createUser("job2");
        String job2AuthToken = clientUtil.login("job2");

        // Import an OPML file
        WebResource subscriptionResource = resource().path("/subscription/import");
        subscriptionResource.addFilter(new CookieAuthenticationFilter(job1AuthToken));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream track = this.getClass().getResourceAsStream("/import/greader_subscriptions.xml");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(track),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        ClientResponse response = subscriptionResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check the user's job
        WebResource userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(job1AuthToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
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
        WebResource jobResource = resource().path("/job/" + jobId);
        jobResource.addFilter(new CookieAuthenticationFilter(job2AuthToken));
        response = jobResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));// TODO return forbidden status in this case
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User job1 deletes his job
        jobResource = resource().path("/job/" + jobId);
        jobResource.addFilter(new CookieAuthenticationFilter(job1AuthToken));
        response = jobResource.delete(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check that the job was deleted
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(job1AuthToken));
        response = userResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        jobs = json.getJSONArray("jobs");
        Assert.assertEquals(0, jobs.length());
    }
}