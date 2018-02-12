Feature: Support for openEHR Archetype Query Language
  In order to access openEHR based data
  As A clinical informatics actor
  I want to use openEHR AQL queries to query clinical data

  Background:
  The server is ready, an EHR and required templates is in place and the user logged in and created data.

    Given The server is running
    And The client system is logged into a server session
    And The templates with following ids are available to the server:
      |IDCR - Adverse Reaction List.v1|
      |IDCR - End of Life Patient Preferences.v0|
      |IDCR - Immunisation summary.v0|
      |IDCR - Laboratory Order.v0|
      |IDCR - Laboratory Test Report.v0|
      |IDCR - Medication Statement List.v0|
      |IDCR - Minimal MDT Output Report.v0|
      |IDCR - Problem List.v1|
      |IDCR - Procedures List.v1|
      |IDCR - Relevant contacts.v0|
      |IDCR - Service Request.v0|
      |IDCR - Service tracker.v0|
      |IDCR - Vital Signs Encounter.v1|
      |RIPPLE - Clinical Notes.v1|
      |RIPPLE - Height_Weight.v1|
      |RIPPLE - Personal Notes.v1|
      |Smart Growth Chart Data.v0|
    And An EHR is created
    And The following compositions exists under the EHR:
      |Composition_IDCR_Adverse_Reaction_List.v1.0.json|IDCR - Adverse Reaction List.v1|
      |Composition_IDCR_Adverse_Reaction_List.v1.1.json|IDCR - Adverse Reaction List.v1|
      |Composition_IDCR_End_of_Life_Patient_Preferences.v0.0.json|IDCR - End of Life Patient Preferences.v0|
      |Composition_IDCR_Immunisation_summary.v0.0.json|IDCR - Immunisation summary.v0|
      |Composition_IDCR_Immunisation_summary.v0.1.json|IDCR - Immunisation summary.v0|
      |Composition_IDCR_Laboratory_Order.v0.0.json|IDCR - Laboratory Order.v0|
      |Composition_IDCR_Laboratory_Order.v0.1.json|IDCR - Laboratory Order.v0|
      |Composition_IDCR_Laboratory_Order.v0.2.json|IDCR - Laboratory Order.v0|
      |Composition_IDCR_Laboratory_Test_Report.v0.0.json|IDCR - Laboratory Test Report.v0|
      |Composition_IDCR_Laboratory_Test_Report.v0.1.json|IDCR - Laboratory Test Report.v0|
      |Composition_IDCR_Laboratory_Test_Report.v0.2.json|IDCR - Laboratory Test Report.v0|
      |Composition_IDCR_Medication_Statement_List.v0.0.json|IDCR - Medication Statement List.v0|
      |Composition_IDCR_Medication_Statement_List.v0.1.json|IDCR - Medication Statement List.v0|
      |Composition_IDCR_Medication_Statement_List.v0.2.json|IDCR - Medication Statement List.v0|
      |Composition_IDCR__Minimal_MDT_Output_Report.v0.0.json|IDCR - Minimal MDT Output Report.v0|
      |Composition_IDCR__Minimal_MDT_Output_Report.v0.1.json|IDCR - Minimal MDT Output Report.v0|
      |Composition_IDCR_Problem_List.v1.0.json|IDCR - Problem List.v1|
      |Composition_IDCR_Problem_List.v1.1.json|IDCR - Problem List.v1|
      |Composition_IDCR_Problem_List.v1.2.json|IDCR - Problem List.v1|
      |Composition_IDCR_Problem_List.v1.3.json|IDCR - Problem List.v1|
      |Composition_IDCR_Problem_List.v1.4.json|IDCR - Problem List.v1|
      |Composition_IDCR_Problem_List.v1.5.json|IDCR - Problem List.v1|
      |Composition_IDCR_Procedures_List.v1.0.json|IDCR - Procedures List.v1|
      |Composition_IDCR_Procedures_List.v1.1.json|IDCR - Procedures List.v1|
      |Composition_IDCR_Procedures_List.v1.2.json|IDCR - Procedures List.v1|
      |Composition_IDCR_Procedures_List.v1.3.json|IDCR - Procedures List.v1|
      |Composition_IDCR_Procedures_List.v1.4.json|IDCR - Procedures List.v1|
      |Composition_IDCR_Relevant_contacts.v0.0.json|IDCR - Relevant contacts.v0|
      |Composition_IDCR_Relevant_contacts.v0.1.json|IDCR - Relevant contacts.v0|
      |Composition_IDCR_Relevant_contacts.v0.2.json|IDCR - Relevant contacts.v0|
      |Composition_IDCR_Service_Request.v0.0.json|IDCR - Service Request.v0|
      |Composition_IDCR_Service_Request.v0.1.json|IDCR - Service Request.v0|
      |Composition_IDCR_Service_Request.v0.2.json|IDCR - Service Request.v0|
      |Composition_IDCR_Service_tracker.v0.0.json|IDCR - Service tracker.v0|
      |Composition_IDCR_Service_tracker.v0.1.json|IDCR - Service tracker.v0|
      |Composition_IDCR_Service_tracker.v0.2.json|IDCR - Service tracker.v0|
      |Composition_IDCR_Service_tracker.v0.3.json|IDCR - Service tracker.v0|
      |Composition_IDCR_Vital_Signs_Encounter.v1.0.json|IDCR - Vital Signs Encounter.v1|
      |Composition_IDCR_Vital_Signs_Encounter.v1.1.json|IDCR - Vital Signs Encounter.v1|
      |Composition_IDCR_Vital_Signs_Encounter.v1.2.json|IDCR - Vital Signs Encounter.v1|
      |Composition_RIPPLE_Clinical_Notes.v1.0.json|RIPPLE - Clinical Notes.v1|
      |Composition_RIPPLE_Clinical_Notes.v1.1.json|RIPPLE - Clinical Notes.v1|
      |Composition_RIPPLE_Clinical_Notes.v1.2.json|RIPPLE - Clinical Notes.v1|
      |Composition_RIPPLE_Height_Weight.v1.0.json|RIPPLE - Height_Weight.v1|
      |Composition_RIPPLE_Height_Weight.v1.1.json|RIPPLE - Height_Weight.v1|
      |Composition_RIPPLE_Height_Weight.v1.2.json|RIPPLE - Height_Weight.v1|
      |Composition_RIPPLE_Personal_Notes.v1.0.json|RIPPLE - Personal Notes.v1|
      |Composition_RIPPLE_Personal_Notes.v1.1.json|RIPPLE - Personal Notes.v1|
      |Composition_RIPPLE_Personal_Notes.v1.2.json|RIPPLE - Personal Notes.v1|

# PROBLEMS IN FLAT JSON PROCESSING
#      |Composition_SMART_Growth_Chart.v0.0.json|Smart Growth Chart Data.v0|
#      |Composition_SMART_Growth_Chart.v0.1.json|Smart Growth Chart Data.v0|
#      |Composition_SMART_Growth_Chart.v0.2.json|Smart Growth Chart Data.v0|
#      |Composition_SMART_Growth_Chart.v0.3.json|Smart Growth Chart Data.v0|

  Scenario: Composition contains instruction
    A composition which contains an instruction is queried. The composition sits under the EHR.
    AQL query specifies EHR id, Composition archetype node id and instruction archetype node id.
    The query uses WHERE clause to add Composition name as an extra criteria. The returned data
    consists of uid, composer, event context start time and some data from the activities under the instruction.

    When A an AQL query that describes an instruction under a composition is created
    And The query contains EHR id criteria
    And Composition archetype id criteria
    And Composition name criteria using WHERE clause
    And Instruction archetype id criteria
    Then The following data items should be available in query results:
      | composition_uid |
      | composition_composer |
      | context_start_time |
      | activity_data |