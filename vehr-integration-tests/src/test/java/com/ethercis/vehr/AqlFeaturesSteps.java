package com.ethercis.vehr;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
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
    private static final String SELECT_COMPLETE_EVALUATION_AQL = "population_composition_evaluation.aql";
    private final String SELECT_COMPLETE_INSTRUCTION_AQL = "select_complete_instruction.aql";
    private final String ARCHETYPE_NODE_ID_AND_NAME_PATTERN = "\\[at\\d{4} *, *\\'[\\w\\s]*\\'\\]";
    private final String SELECT_COMPLETE_COMPOSITION_AQL = "select_complete_composition.aql";
    private final String SELECT_DATA_ITEM_NODE_ID_NAME_AQL = "select_data_item_node_id_and_name.aql";
    private final String EHR_COMPOSITION_INSTRUCTION_AQL = "ehr-composition-instruction.aql";
    private final String EHR_ID_PLACEHOLDER = "{{ehrId}}";
    private final String COMPOSITION_ARCH_ID_PLACEHOLDER = "{{compositionArchetypeId}}";
    private final String EVALUATION_ARCH_ID_PLACEHOLDER = "{{evaluationArchetypeId}}";
    private final String MEDICATION_LIST_ARCH_ID = "openEHR-EHR-COMPOSITION.medication_list.v0";
    private final String MEDICATION_ORDER_ARCH_ID = "openEHR-EHR-INSTRUCTION.medication_order.v1";
    private final String COMPOSITION_NAME_PLACEHOLDER = "{{compositionName}}";
    private final String COMPOSITION_NAME_MED_STATEMENT = "Medication statement list";
    private final String COMPOSITION_INSTRUCTION_ARCH_ID_PLACEHOLDER = "{{instructionArchetypeId}}";
    private final RestAPIBackgroundSteps bg;
    private final String CODE4HEALTH_QUERY_DIR = CODE4HEALTH_TEST_DATA_DIR + "/queries/";

    private String _aqlQuery;

    private List<String> _code4HealthTemplateIds;
    private List<Map<String, String>> _aqlResultSet;
    private String _instructionArchetypeNodeId;

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

    @And("^Composition archetype id is (openEHR-EHR-COMPOSITION\\.[\\w_]*\\.v\\d+)$")
    public void compositionArchetypeIdCriteria(String compositionArchetypeId) throws Throwable {
        _aqlQuery =
            _aqlQuery
                .replace(COMPOSITION_ARCH_ID_PLACEHOLDER, compositionArchetypeId);
    }

    @And("^Composition name criteria using WHERE clause$")
    public void compositionNameCriteriaUsingWHEREClause(String compositionName) throws Throwable {
        _aqlQuery = _aqlQuery.replace(COMPOSITION_NAME_PLACEHOLDER, compositionName);
    }

    @And("^Instruction archetype id is (openEHR-EHR-INSTRUCTION\\.[\\w]*\\.v\\d+)$")
    public void instructionArchetypeIdCriteria(String archetypeId) throws Throwable {
        _aqlQuery =
            _aqlQuery
                .replace(COMPOSITION_INSTRUCTION_ARCH_ID_PLACEHOLDER, archetypeId);
    }

    public void evaluationArchetypeIdCriteria(String archetypeId) throws Throwable {
        _aqlQuery =
            _aqlQuery
                .replace(EVALUATION_ARCH_ID_PLACEHOLDER,archetypeId);
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
        _aqlQuery = readAqlFile(SELECT_DATA_ITEM_NODE_ID_NAME_AQL);
        //set variables so that the query would work
        theQueryContainsEHRIdCriteria();
        compositionArchetypeIdCriteria(MEDICATION_LIST_ARCH_ID);
        compositionNameCriteriaUsingWHEREClause(COMPOSITION_NAME_MED_STATEMENT);
        instructionArchetypeIdCriteria(MEDICATION_ORDER_ARCH_ID);
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
        _aqlQuery = readAqlFile(SELECT_COMPLETE_COMPOSITION_AQL);
    }

    @Then("^The results should be composition instances$")
    public void theResultsShouldBeCompositionInstances() throws Throwable {
        _aqlResultSet = bg.extractAqlResults(bg.getAqlResponse(_aqlQuery));
        assertTrue(_aqlResultSet.size() > 0);

        _aqlResultSet.forEach(
            map -> assertArchetypeNodeId(map, "composition", MEDICATION_LIST_ARCH_ID));
    }

    private String readFile(String fullFilePath) throws FileNotFoundException {
        return new Scanner(new FileInputStream(fullFilePath))
            .useDelimiter("\\A")
            .next();
    }

    private String readAqlFile(String aqlFileName) throws FileNotFoundException {
        String fullFilePath = bg.resourcesRootPath + CODE4HEALTH_QUERY_DIR + aqlFileName;
        return new Scanner(new FileInputStream(fullFilePath))
            .useDelimiter("\\A")
            .next();
    }

    @Then("^The results should be instruction instances$")
    public void theResultsShouldBeInstructionInstances() throws Throwable {
        _aqlResultSet = bg.extractAqlResults(bg.getAqlResponse(_aqlQuery));
        assertTrue(_aqlResultSet.size() > 0);

        _aqlResultSet.forEach(
            map -> assertArchetypeNodeId(map, "instruction", MEDICATION_ORDER_ARCH_ID));
    }

    private void assertArchetypeNodeId(Map<String,String> map,
                                       String nodeAliasInAqlSelectClause,
                                       String archetypeNodeId){
        String instructionJson = map.get(nodeAliasInAqlSelectClause);
        JsonPath path = new JsonPath(instructionJson);
        String node_id = path.getString("archetype_node_id");
        assertEquals(archetypeNodeId, node_id);
    }

    @When("^A an AQL query that describes an instruction under an EHR is created$")
    public void aAnAQLQueryThatDescribesAnInstructionUnderAnEHRIsCreated() throws Throwable {
        _aqlQuery = readAqlFile(SELECT_COMPLETE_INSTRUCTION_AQL);
    }

    @When("^A an AQL query that describes an evalution under an EHR is created$")
    public void aAnAQLQueryThatDescribesAnEvalutionUnderAnEHRIsCreated() throws Throwable {
        _aqlQuery = readAqlFile(SELECT_COMPLETE_EVALUATION_AQL);
    }

    @And("^The Composition archetype id is (openEHR-EHR-COMPOSITION\\.[\\w_]*\\.v\\d+)$")
    public void theCompositionArchetypeIdIsOpenEHREHRCOMPOSITIONAdverse_reaction_listV(String archetypeId) throws Throwable {
        compositionArchetypeIdCriteria(archetypeId);
    }

    @And("^Composition name criteria using WHERE clause is ([\\w\\s]*)$")
    public void compositionNameCriteriaUsingWHEREClauseIsAdverseReactionList(String compositionName) throws Throwable {
        compositionNameCriteriaUsingWHEREClause(compositionName);
    }

    @And("^Evaluation archetype id is (openEHR-EHR-EVALUATION\\.[\\w]*\\.v\\d+)$")
    public void evaluationArchetypeIdIsOpenEHREHREVALUATIONAdverse_reaction_riskV(String evaluationId) throws Throwable {
        _instructionArchetypeNodeId = evaluationId;
        evaluationArchetypeIdCriteria(evaluationId);
    }

    @Then("^The results should be evaluation instances$")
    public void theResultsShouldBeEvaluationInstances() throws Throwable {
        _aqlResultSet = bg.extractAqlResults(bg.getAqlResponse(_aqlQuery));
        assertTrue(_aqlResultSet.size() > 0);

        _aqlResultSet.forEach(
            map -> assertArchetypeNodeId(map, "evaluation", _instructionArchetypeNodeId)
        );
    }
}
