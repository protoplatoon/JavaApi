Feature: Gradle-Cucumber integration

  Scenario: show the staff list for the week
	Given there are 5 staff in a schedule
    When I get the schedule list of staff
    Then I should have 5 staff in my list
