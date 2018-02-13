package com.ethercis.vehr;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ethercis.vehr.RestAPIBackgroundSteps.CODE4HEALTH_OPT_DIR;
import static com.ethercis.vehr.RestAPIBackgroundSteps.CODE4HEALTH_TEST_DATA_DIR;
import static com.ethercis.vehr.RestAPIBackgroundSteps.STATUS_CODE_OK;
import static org.junit.Assert.*;

public class AqlFeaturesSteps {
    private final String ARCHETYPE_NODE_ID_AND_NAME_PATTERN = "\\[at\\d{4} *, *\\'[\\w\\s]*\\'\\]";
    private final String SELECT_COMPLETE_COMPOSITION_AQL = "select_complete_composition.aql";
    private final String SELECT_DATA_ITEM_NODE_ID_NAME_AQL = "select_data_item_node_id_and_name.aql";
    private final String EHR_COMPOSITION_INSTRUCTION_AQL = "ehr-composition-instruction.aql";
    private final String EHR_ID_PLACEHOLDER = "{{ehrId}}";
    private final String COMPOSITION_ARCH_ID_PLACEHOLDER = "{{compositionArchetypeIdId}}";
    private final String COMPOSITION_ARCH_ID = "openEHR-EHR-COMPOSITION.medication_list.v0";
    private final String INSTRUCTION_ARCH_ID = "openEHR-EHR-INSTRUCTION.medication_order.v1";
    private final String COMPOSITION_NAME_PLACEHOLDER = "{{compositionName}}";
    private final String COMPOSITION_NAME = "Medication statement list";
    private final String COMPOSITION_INSTRUCTION_ARCH_ID_PLACEHOLDER = "{{instructionArchetypeId}}";
    private final RestAPIBackgroundSteps bg;
    private final String CODE4HEALTH_QUERY_DIR = CODE4HEALTH_TEST_DATA_DIR + "/queries/";

    private String _aqlQuery;

    private List<String> _code4HealthTemplateIds;
    private List<Map<String, String>> _aqlResultSet;

    public AqlFeaturesSteps(RestAPIBackgroundSteps backgroundSteps) {
        bg = backgroundSteps;
    }

    @When("^A an AQL query that describes an instruction under a composition is created$")
    public void aAnAQLQueryThatDescribesAnInstructionUnderACompositionIsCreated() throws Throwable {
        String queryFile =
            bg.resourcesRootPath +
                CODE4HEALTH_QUERY_DIR +
                EHR_COMPOSITION_INSTRUCTION_AQL;

        _aqlQuery =
            new Scanner(
                new ByteArrayInputStream(
                    Files.readAllBytes(Paths.get(queryFile))))
            .useDelimiter("\\A")
            .next();
    }

    @And("^The query contains EHR id criteria$")
    public void theQueryContainsEHRIdCriteria() throws Throwable {
        _aqlQuery = _aqlQuery.replace(EHR_ID_PLACEHOLDER, bg.ehrId.toString());
    }

    @And("^Composition archetype id criteria$")
    public void compositionArchetypeIdCriteria() throws Throwable {
        _aqlQuery =
            _aqlQuery
                .replace(COMPOSITION_ARCH_ID_PLACEHOLDER,
                    COMPOSITION_ARCH_ID);
    }

    @And("^Composition name criteria using WHERE clause$")
    public void compositionNameCriteriaUsingWHEREClause() throws Throwable {
        _aqlQuery = _aqlQuery.replace(COMPOSITION_NAME_PLACEHOLDER, COMPOSITION_NAME);
    }

    @And("^Instruction archetype id criteria$")
    public void instructionArchetypeIdCriteria() throws Throwable {
        _aqlQuery =
            _aqlQuery
                .replace(COMPOSITION_INSTRUCTION_ARCH_ID_PLACEHOLDER,
                    INSTRUCTION_ARCH_ID);
    }

    @Then("^The following data items should be available in query results:$")
    public void theFollowingDataItemsShouldBeAvailableInQueryResults(DataTable dataItems) throws Throwable {
        Response aqlResponse = bg.getAqlResponse(_aqlQuery);
        assertEquals(aqlResponse.statusCode(), STATUS_CODE_OK);

        _aqlResultSet = bg.extractAqlResults(aqlResponse);
        _aqlResultSet
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
        assertExpectedDataItem(resultRow, dataItem);
    }

    private void assertExpectedDataItem(Map<String, String> resultRow, String dataItem) {
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

    @When("^An AQL query that selects composition uids and data items is created$")
    public void anAQLQueryThatSelectsCompositionUidsAndDataItemsIsCreated() throws Throwable {
        String fullFilepath = bg.resourcesRootPath + CODE4HEALTH_QUERY_DIR + SELECT_DATA_ITEM_NODE_ID_NAME_AQL;
        _aqlQuery = readFile(fullFilepath);
        //set variables so that the query would work
        theQueryContainsEHRIdCriteria();
        compositionArchetypeIdCriteria();
        compositionNameCriteriaUsingWHEREClause();
        instructionArchetypeIdCriteria();
    }

    @And("^The data items are selected based on both archetype node id and name$")
    public void theDataItemsAreSelectedBasedOnBothArchetypeNodeIdAndName() throws Throwable {
        Pattern nodeCriteriaPattern = Pattern.compile(ARCHETYPE_NODE_ID_AND_NAME_PATTERN);
        Matcher matcher = nodeCriteriaPattern.matcher(_aqlQuery);
        //look for two nodes with nodeId+name pattern, hence two matches
        assertTrue(matcher.find() && matcher.find());

        _aqlResultSet = bg.extractAqlResults(bg.getAqlResponse(_aqlQuery));
    }

    @Then("^Data items with same node id should have different values if they have different names$")
    public void dataItemsWithSameNodeIdShouldHaveDifferentValuesIfTheyHaveDifferentNames() throws Throwable {
        _aqlResultSet.forEach(
            map -> assertNotEquals(map.get("dose_amount"), map.get("dose_timing")));
    }

    @When("^A an AQL query that describes a composition under an EHR is created$")
    public void aAnAQLQueryThatDescribesAnCompositionUnderAnEHRIsCreated() throws Throwable {
        String fullFilepath = bg.resourcesRootPath + CODE4HEALTH_QUERY_DIR + SELECT_COMPLETE_COMPOSITION_AQL;
        _aqlQuery = readFile(fullFilepath);
        //set ehr id
        theQueryContainsEHRIdCriteria();
    }

    @Then("^The results should be composition instances$")
    public void theResultsShouldBeCompositionInstances() throws Throwable {
        _aqlResultSet = bg.extractAqlResults(bg.getAqlResponse(_aqlQuery));
        assertTrue(_aqlResultSet.size() > 0);
    }

    private String readFile(String fullFilePath) throws FileNotFoundException {
        return new Scanner(new FileInputStream(fullFilePath))
            .useDelimiter("\\A")
            .next();
    }
}
