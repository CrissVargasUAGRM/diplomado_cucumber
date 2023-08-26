@Person
Feature: Perform a person creation

  Scenario: Perform person creation with valid information
    Given an person with valid information
      | name          | Juan de sol       |
      | lastName      | Rojas             |
      | ci            | 12345678          |
      | userId        | b6b3c476-d5e5-4076-8535-68f7cad03027 |
    When request is submitted for person creation
    Then verify that the Person HTTP response is 200
    And a person Object is returned