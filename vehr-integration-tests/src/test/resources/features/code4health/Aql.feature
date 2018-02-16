Feature: Support for openEHR Archetype Query Language
  In order to access openEHR based data
  As A clinical informatics actor
  I want to use openEHR AQL queries to query clinical data

  Background:
  The server is ready, an EHR and required templates is in place and the user logged in and created data.

    Given The server is running
    And The client system is logged into a server session
    And The templates with following ids are available to the server:
      | IDCR - Adverse Reaction List.v1           |
      | IDCR - End of Life Patient Preferences.v0 |
      | IDCR - Immunisation summary.v0            |
      | IDCR - Laboratory Order.v0                |
      | IDCR - Laboratory Test Report.v0          |
      | IDCR - Medication Statement List.v0       |
      | IDCR - Minimal MDT Output Report.v0       |
      | IDCR - Problem List.v1                    |
      | IDCR - Procedures List.v1                 |
      | IDCR - Relevant contacts.v0               |
      | IDCR - Service Request.v0                 |
      | IDCR - Service tracker.v0                 |
      | IDCR - Vital Signs Encounter.v1           |
      | RIPPLE - Clinical Notes.v1                |
      | RIPPLE - Height_Weight.v1                 |
      | RIPPLE - Personal Notes.v1                |
      | Smart Growth Chart Data.v0                |
    And An EHR is created
    And The following compositions exists under the EHR:
      | Composition_IDCR_Adverse_Reaction_List.v1.0.json           | IDCR - Adverse Reaction List.v1           |
      | Composition_IDCR_Adverse_Reaction_List.v1.1.json           | IDCR - Adverse Reaction List.v1           |
      | Composition_IDCR_End_of_Life_Patient_Preferences.v0.0.json | IDCR - End of Life Patient Preferences.v0 |
      | Composition_IDCR_Immunisation_summary.v0.0.json            | IDCR - Immunisation summary.v0            |
      | Composition_IDCR_Immunisation_summary.v0.1.json            | IDCR - Immunisation summary.v0            |
      | Composition_IDCR_Laboratory_Order.v0.0.json                | IDCR - Laboratory Order.v0                |
      | Composition_IDCR_Laboratory_Order.v0.1.json                | IDCR - Laboratory Order.v0                |
      | Composition_IDCR_Laboratory_Order.v0.2.json                | IDCR - Laboratory Order.v0                |
      | Composition_IDCR_Laboratory_Test_Report.v0.0.json          | IDCR - Laboratory Test Report.v0          |
      | Composition_IDCR_Laboratory_Test_Report.v0.1.json          | IDCR - Laboratory Test Report.v0          |
      | Composition_IDCR_Laboratory_Test_Report.v0.2.json          | IDCR - Laboratory Test Report.v0          |
      | Composition_IDCR_Medication_Statement_List.v0.0.json       | IDCR - Medication Statement List.v0       |
      | Composition_IDCR_Medication_Statement_List.v0.1.json       | IDCR - Medication Statement List.v0       |
      | Composition_IDCR_Medication_Statement_List.v0.2.json       | IDCR - Medication Statement List.v0       |
      | Composition_IDCR__Minimal_MDT_Output_Report.v0.0.json      | IDCR - Minimal MDT Output Report.v0       |
      | Composition_IDCR__Minimal_MDT_Output_Report.v0.1.json      | IDCR - Minimal MDT Output Report.v0       |
      | Composition_IDCR_Problem_List.v1.0.json                    | IDCR - Problem List.v1                    |
      | Composition_IDCR_Problem_List.v1.1.json                    | IDCR - Problem List.v1                    |
      | Composition_IDCR_Problem_List.v1.2.json                    | IDCR - Problem List.v1                    |
      | Composition_IDCR_Problem_List.v1.3.json                    | IDCR - Problem List.v1                    |
      | Composition_IDCR_Problem_List.v1.4.json                    | IDCR - Problem List.v1                    |
      | Composition_IDCR_Problem_List.v1.5.json                    | IDCR - Problem List.v1                    |
      | Composition_IDCR_Procedures_List.v1.0.json                 | IDCR - Procedures List.v1                 |
      | Composition_IDCR_Procedures_List.v1.1.json                 | IDCR - Procedures List.v1                 |
      | Composition_IDCR_Procedures_List.v1.2.json                 | IDCR - Procedures List.v1                 |
      | Composition_IDCR_Procedures_List.v1.3.json                 | IDCR - Procedures List.v1                 |
      | Composition_IDCR_Procedures_List.v1.4.json                 | IDCR - Procedures List.v1                 |
      | Composition_IDCR_Relevant_contacts.v0.0.json               | IDCR - Relevant contacts.v0               |
      | Composition_IDCR_Relevant_contacts.v0.1.json               | IDCR - Relevant contacts.v0               |
      | Composition_IDCR_Relevant_contacts.v0.2.json               | IDCR - Relevant contacts.v0               |
      | Composition_IDCR_Service_Request.v0.0.json                 | IDCR - Service Request.v0                 |
      | Composition_IDCR_Service_Request.v0.1.json                 | IDCR - Service Request.v0                 |
      | Composition_IDCR_Service_Request.v0.2.json                 | IDCR - Service Request.v0                 |
      | Composition_IDCR_Service_tracker.v0.0.json                 | IDCR - Service tracker.v0                 |
      | Composition_IDCR_Service_tracker.v0.1.json                 | IDCR - Service tracker.v0                 |
      | Composition_IDCR_Service_tracker.v0.2.json                 | IDCR - Service tracker.v0                 |
      | Composition_IDCR_Service_tracker.v0.3.json                 | IDCR - Service tracker.v0                 |
      | Composition_IDCR_Vital_Signs_Encounter.v1.0.json           | IDCR - Vital Signs Encounter.v1           |
      | Composition_IDCR_Vital_Signs_Encounter.v1.1.json           | IDCR - Vital Signs Encounter.v1           |
      | Composition_IDCR_Vital_Signs_Encounter.v1.2.json           | IDCR - Vital Signs Encounter.v1           |
      | Composition_IDCR_Vital_Signs_Encounter.v1.3.json           | IDCR - Vital Signs Encounter.v1           |
      | Composition_RIPPLE_Clinical_Notes.v1.0.json                | RIPPLE - Clinical Notes.v1                |
      | Composition_RIPPLE_Clinical_Notes.v1.1.json                | RIPPLE - Clinical Notes.v1                |
      | Composition_RIPPLE_Clinical_Notes.v1.2.json                | RIPPLE - Clinical Notes.v1                |
      | Composition_RIPPLE_Height_Weight.v1.0.json                 | RIPPLE - Height_Weight.v1                 |
      | Composition_RIPPLE_Height_Weight.v1.1.json                 | RIPPLE - Height_Weight.v1                 |
      | Composition_RIPPLE_Height_Weight.v1.2.json                 | RIPPLE - Height_Weight.v1                 |
      | Composition_RIPPLE_Personal_Notes.v1.0.json                | RIPPLE - Personal Notes.v1                |
      | Composition_RIPPLE_Personal_Notes.v1.1.json                | RIPPLE - Personal Notes.v1                |
      | Composition_RIPPLE_Personal_Notes.v1.2.json                | RIPPLE - Personal Notes.v1                |

# PROBLEMS IN FLAT JSON PROCESSING
#      |Composition_SMART_Growth_Chart.v0.0.json|Smart Growth Chart Data.v0|
#      |Composition_SMART_Growth_Chart.v0.1.json|Smart Growth Chart Data.v0|
#      |Composition_SMART_Growth_Chart.v0.2.json|Smart Growth Chart Data.v0|
#      |Composition_SMART_Growth_Chart.v0.3.json|Smart Growth Chart Data.v0|

  Scenario Outline: Select composition and instruction data when Composition contains instruction
  A composition which contains an instruction is queried. The composition sits under the EHR.
  AQL query specifies EHR id, Composition archetype node id and instruction archetype node id.
  The query uses WHERE clause to add Composition name as an extra criteria. The returned data
  consists of uid, composer, event context start time and some data from the activities under the instruction.

    When A an AQL query that describes an instruction under a composition is created
    And The query contains EHR id criteria
    And Composition archetype id is <composition_arch_id>
    And Composition name criteria using WHERE clause is <composition_name>
    And Instruction archetype id is <instruction_arch_id>
    Then The following data items should be available in query results:
      | composition_uid      |
      | composition_composer |
      | context_start_time   |
      | activity_data        |
    Examples:
      | composition_arch_id                        | composition_name          | instruction_arch_id                         |
      | openEHR-EHR-COMPOSITION.medication_list.v0 | Medication statement list | openEHR-EHR-INSTRUCTION.medication_order.v1 |

  Scenario: Select data item using its archetype node id and name
  Query compositions which contain data items that can can only be distinguished by their names.
  The query uses [atXXXX,'data item name'] syntax in SELECT clause to select specific data items.

    When An AQL query that selects composition uids and data items is created
    And The data items are selected based on both archetype node id and name
    Then Data items with same node id should have different values if they have different names

  Scenario Outline: Select composition
  A composition is queried. The composition sits under the EHR.
  AQL query specifies EHR id, Composition archetype node id and instruction archetype node id.
  The query uses WHERE clause to add Composition name as an extra criteria. The SELECT clause
  selects the complete composition instance

    When A an AQL query that describes a composition under an EHR is created
    And The query contains EHR id criteria
    And Composition archetype id is <composition_arch_id>
    And Composition name criteria using WHERE clause is <composition_name>
    Then The results should be composition instances

    Examples:
      | composition_arch_id                        | composition_name          |
      | openEHR-EHR-COMPOSITION.medication_list.v0 | Medication statement list |

  Scenario Outline: Select instruction
  A composition with an instruction is queried. The composition sits under the EHR.
  AQL query specifies EHR id, Composition archetype node id and instruction archetype node id.
  The query uses WHERE clause to add Composition name as an extra criteria. The SELECT clause
  selects the complete instruction instance

    When A an AQL query that describes an instruction under an EHR is created
    And The query contains EHR id criteria
    And Composition archetype id is <composition_arch_id>
    And Composition name criteria using WHERE clause is <composition_name>
    And Instruction archetype id is <instruction_arch_id>
    Then The results should be instruction instances

    Examples:
      | composition_arch_id                        | composition_name          | instruction_arch_id                         |
      | openEHR-EHR-COMPOSITION.medication_list.v0 | Medication statement list | openEHR-EHR-INSTRUCTION.medication_order.v1 |
      | openEHR-EHR-COMPOSITION.request.v1         | Laboratory order          | openEHR-EHR-INSTRUCTION.request-lab_test.v1 |

  Scenario Outline: Select evaluation
  A composition with an evaluation is queried. The composition sits under the EHR.
  AQL query specifies EHR id, Composition archetype node id and evaluation archetype node id.
  The query uses WHERE clause to add Composition name as an extra criteria. The SELECT clause
  selects the complete evaluation instance

    When A an AQL query that describes an evalution under an EHR is created
    And The Composition archetype id is <composition_arch_id>
    And Composition name criteria using WHERE clause is <composition_name>
    And Evaluation archetype id is <evaluation_arch_id>
    Then The results should be evaluation instances

    Examples:
      | composition_arch_id                              | composition_name      | evaluation_arch_id                              |
      | openEHR-EHR-COMPOSITION.adverse_reaction_list.v1 | Adverse reaction list | openEHR-EHR-EVALUATION.adverse_reaction_risk.v1 |

  Scenario Outline: Select observation
  A composition with an observation is queried. The composition sits under the EHR.
  AQL query specifies EHR id, Composition archetype node id and observation archetype node id.
  The query uses WHERE clause to add Composition name as an extra criteria. The SELECT clause
  selects the complete observation instance

    When A an AQL query that describes an observation under an EHR is created
    And The query contains EHR id criteria
    And The Composition archetype id is <composition_arch_id>
    And Composition name criteria using WHERE clause is <composition_name>
    And Observation archetype id is <observation_arch_id>
    Then The results should be observation instances

    Examples:
      | composition_arch_id                              | composition_name      | observation_arch_id                              |
      | openEHR-EHR-COMPOSITION.encounter.v1 | Vital Signs Observations | openEHR-EHR-OBSERVATION.respiration.v1 |

  Scenario: Select instruction under section
  A composition with a section is queried. The section contains an instruction.
  The composition sits under the EHR.
  The SELECT clause includes the complete instruction instance

    When A an AQL query that describes an instruction under a section is created
    Then The results should include instruction instances

  Scenario: Select evaluation under section
  A composition with a section is queried. The section contains an evaluation.
  The composition sits under the EHR.
  The SELECT clause includes the complete evaluation instance

    When A an AQL query that describes an evalution under a section is created
    Then The results should include evaluation instances

  Scenario: Select observation under section
  A composition with a section is queried. The section contains an observation.
  The composition sits under the EHR.
  The SELECT clause includes the complete observation instance

    When A an AQL query that describes an observation under a section is created
    Then The results should include observation instances

  Scenario: Select action under section
  A composition with a section is queried. The section contains an action.
  The composition sits under the EHR.
  The SELECT clause includes the complete action instance

    When A an AQL query that describes an action under a section is created
    Then The results should include action instances

  Scenario: Query without archetype id or name constraints
    A composition with a section and and entry subtype under the section is queried.
    The EHR is also included in the query.
    The query does not use any EHR ids, archetype ids or archetype names. Therefore it is
    a purely structural population query. The query returns instances of entry subtype.

    When An aql query that describes an EHR/COMPOSITION/SECTION/ENTRY structure is created
    Then The results should include ENTRY instances

  Scenario: Query after data volume increases
    A composition with a section and and entry subtype under the section is queried.
    The EHR is also included in the query.
    The query does not use any EHR ids, archetype ids or archetype names. Therefore it is
    a purely structural population query. The query returns instances of entry subtype.
    After the query returns, more data is inserted and the query is repeated. The
    query returns a larger result set.

    When An aql query that describes an EHR/COMPOSITION/SECTION/ENTRY structure is created
    And The results include ENTRY instances
    And More data is inserted
    And The AQL query is repeated
    Then A larger result set should be returned