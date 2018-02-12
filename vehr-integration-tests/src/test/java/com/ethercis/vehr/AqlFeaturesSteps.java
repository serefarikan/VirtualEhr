package com.ethercis.vehr;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.ethercis.vehr.RestAPIBackgroundSteps.CODE4HEALTH_OPT_DIR;
import static com.ethercis.vehr.RestAPIBackgroundSteps.CODE4HEALTH_TEST_DATA_DIR;
import static com.ethercis.vehr.RestAPIBackgroundSteps.STATUS_CODE_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AqlFeaturesSteps {

    private final String EHR_COMPOSITION_INSTRUCTION_AQL = "ehr-composition-instruction.aql";
    private final String EHR_ID_PLACEHOLDER = "{{ehrId}}";
    private final String COMPOSITION_ARCH_ID_PLACEHOLDER = "{{compositionArchetypeIdId}}";
    private final String MEDICATION_LIST_ARCH_ID = "openEHR-EHR-COMPOSITION.medication_list.v0";
    private final String MEDICATION_ORDER_ARCH_ID = "openEHR-EHR-INSTRUCTION.medication_order.v1";
    private final String COMPOSITION_NAME_PLACEHOLDER = "{{compositionName}}";
    private final String COMPOSITION_NAME = "Medication statement list";
    private final String COMPOSITION_INSTRUCTION_ARCH_ID_PLACEHOLDER = "{{instructionArchetypeId}}";
    private final RestAPIBackgroundSteps bg;
    private final String CODE4HEALTH_QUERY_DIR = CODE4HEALTH_TEST_DATA_DIR + "/queries/";

    private String queryText;

    private List<String> _code4HealthTemplateIds;

    public AqlFeaturesSteps(RestAPIBackgroundSteps backgroundSteps) {
        bg = backgroundSteps;
    }

    @When("^A an AQL query that describes an instruction under a composition is created$")
    public void aAnAQLQueryThatDescribesAnInstructionUnderACompositionIsCreated() throws Throwable {
        String queryFile =
            bg.resourcesRootPath +
                CODE4HEALTH_QUERY_DIR +
                EHR_COMPOSITION_INSTRUCTION_AQL;

        queryText =
            new Scanner(
                new ByteArrayInputStream(
                    Files.readAllBytes(Paths.get(queryFile))))
            .useDelimiter("\\A")
            .next();
    }

    @And("^The query contains EHR id criteria$")
    public void theQueryContainsEHRIdCriteria() throws Throwable {
        queryText = queryText.replace(EHR_ID_PLACEHOLDER, bg.ehrId.toString());
    }

    @And("^Composition archetype id criteria$")
    public void compositionArchetypeIdCriteria() throws Throwable {
        queryText =
            queryText
                .replace(COMPOSITION_ARCH_ID_PLACEHOLDER,
                    MEDICATION_LIST_ARCH_ID);
    }

    @And("^Composition name criteria using WHERE clause$")
    public void compositionNameCriteriaUsingWHEREClause() throws Throwable {
        queryText = queryText.replace(COMPOSITION_NAME_PLACEHOLDER, COMPOSITION_NAME);
    }

    @And("^Instruction archetype id criteria$")
    public void instructionArchetypeIdCriteria() throws Throwable {
        queryText =
            queryText
                .replace(COMPOSITION_INSTRUCTION_ARCH_ID_PLACEHOLDER,
                    MEDICATION_ORDER_ARCH_ID);
    }

    @Then("^The following data items should be available in query results:$")
    public void theFollowingDataItemsShouldBeAvailableInQueryResults(DataTable dataItems) throws Throwable {
        Response aqlResponse = bg.getAqlResponse(queryText);
        assertEquals(aqlResponse.statusCode(), STATUS_CODE_OK);

        List<Map<String, String>> aqlResultSet = bg.extractAqlResults(aqlResponse);
        aqlResultSet
            .forEach(resultRow ->
                dataItems
                    .getGherkinRows()
                    .forEach(gherkinRow -> scanRowForExpectedDataItems(resultRow, gherkinRow))
            );
    }

    @And("^The templates with following ids are available to the server:$")
    public void theTemplatesWithFollowingIdsAreAvailableToTheServer(List<String> pTemplateIds) throws Throwable {
        _code4HealthTemplateIds = pTemplateIds;
        pTemplateIds.forEach(x -> bg.postTemplateToServer(CODE4HEALTH_OPT_DIR,x + ".opt"));
    }

    private void scanRowForExpectedDataItems(Map<String, String> resultRow, DataTableRow gherkinRow) {
        String dataItem = gherkinRow.getCells().get(0);
        switch (dataItem) {
            case "composition_uid":
                String uid = resultRow.get("uid");
                assertNotNull(uid);
                break;
            case "composition_composer":
                String composer = resultRow.get("author");
                assertNotNull(composer);
                break;
            case "context_start_time":
                String startTime = resultRow.get("start_date");
                assertNotNull(startTime);
                break;
            case "activity_data":
                String[] dataColumns = {"dose_timing",
                    "route",
                    "medication_name_code",
                    "date_created",
                    "dose_amount",
                    "dose_directions",
                    "medication_name"};
                Arrays.stream(dataColumns).forEach(
                    column -> assertNotNull(resultRow.get(column))
                );
                break;
                default: throw new RuntimeException("This data item is not mapped to any columns: " + dataItem);
        }
    }
}
