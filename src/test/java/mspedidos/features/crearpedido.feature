@Producto
Feature: Perform a product creation

  Scenario: Perform product creation with valid details
    Given a product with valid details
      | stockActual | 1         |
      | precioVenta | 100.0     |
      | nombre      | empanadas |
    When request is submitted for product creation
    Then verify that the HTTP response is 200
    And a product id is returned