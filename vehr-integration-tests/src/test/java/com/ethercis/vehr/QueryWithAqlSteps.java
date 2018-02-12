package com.ethercis.vehr;

import com.jayway.restassured.response.Response;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import java.util.List;
import java.util.Map;

import static com.ethercis.vehr.RestAPIBackgroundSteps.TEST_DATA_DIR;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryWithAqlSteps {

    private String testCompositionPath;
    private RestAPIBackgroundSteps bg;

    public QueryWithAqlSteps(RestAPIBackgroundSteps pBackgroundSteps){
        bg = pBackgroundSteps;
        testCompositionPath = bg.resourcesRootPath + TEST_DATA_DIR + "/Prescription.xml";
    }

    public QueryWithAqlSteps() {
    }

    @After
    public void cleanUp() throws Exception {
        bg.launcher.stop();
    }

    @And("^A composition is persisted under the EHR$")
    public void aCompositionIsPersistedUnderTheEHR() throws Throwable {
        bg.postXMLComposition(true, testCompositionPath, CompositionFormat.XML);
    }

    @Then("^An AQL query should return data from the composition in the EHR$")
    public void anAQLQueryShouldReturnDataPersistedIntoTheComposition() throws Throwable {
        Response response = bg.getAqlResponse(buildAqlQuery());
        assertEquals(response.statusCode(), bg.STATUS_CODE_OK);

        List<Map<String, String>> queryResults = bg.extractAqlResults(response);

        assertNotNull(queryResults);
        assertTrue(queryResults.size() == 1);
        for(Map<String,String> row:queryResults){
            assertTrue(row.keySet().size() == 1 && row.keySet().contains("uid"));
        }
    }

    private String buildAqlQuery(){
        return "select a/uid/value " +
            "from EHR e [ehr_id/value='" + bg.ehrId.toString() + "']" +
            "contains COMPOSITION a[openEHR-EHR-COMPOSITION.prescription.v1] ";
    }

    @And("^A composition is persisted under the EHR without an EHR identifier$")
    public void aCompositionIsPersistedUnderTheEHRWithoutAnEHRIdentifier() throws Throwable {
        bg.postXMLComposition(false, testCompositionPath, CompositionFormat.XML);
    }
}
