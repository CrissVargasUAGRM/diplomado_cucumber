package mspedidos.steps;

import context.World;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Transpose;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import util.RequestSpecificationFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static util.Util.jsonTemplate;

public class MSPedidosSteps {
    private final World world;
    private final Properties envConfig;
    private RequestSpecification request;

    public MSPedidosSteps(World world) {
        this.world = world;
        this.envConfig = World.envConfig;
        this.world.featureContext = World.threadLocal.get();
    }

    @Before
    public void setUp() {
        request = RequestSpecificationFactory.getInstance();
    }

    @Given("a product with valid details")
    public void getProductValidData(@Transpose DataTable dataTable) throws IOException {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        String stockActual = data.get(0).get("stockActual");
        String precioVenta = data.get(0).get("precioVenta");
        String nombre = data.get(0).get("nombre");

        Map<String, Object> valuesToTemplate = new HashMap<>();
        valuesToTemplate.put("stockActual", stockActual);
        valuesToTemplate.put("precioVenta", precioVenta);
        valuesToTemplate.put("nombre", nombre);

        String jsonAsString = jsonTemplate(envConfig.getProperty("mspedidos-productos_request"), valuesToTemplate);

        world.scenarioContext.put("requestStr", jsonAsString);
    }

    @When("request is submitted for product creation")
    public void submitProductCreation() {
        String payload = world.scenarioContext.get("requestStr").toString();
        Response response = request
                .accept(ContentType.JSON)
                .body(payload)
                .contentType(ContentType.JSON)
                .when().post(envConfig.getProperty("mspedidos-service_url")
                        + envConfig.getProperty("mspedidos-prod_api"));

        world.scenarioContext.put("response", response);
    }

    @Then("verify that the HTTP response is {int}")
    public void verifyHTTPResponseCode(Integer status) {
        Response response = (Response) world.scenarioContext.get("response");
        Integer actualStatusCode = response.then()
                .extract()
                .statusCode();
        Assert.assertEquals(status, actualStatusCode);
    }

    @Then("a product id is returned")
    public void checkProductId() {
        Response response = (Response) world.scenarioContext.get("response");
        String responseString = response.then().extract().asString();
        Assert.assertNotNull(responseString);
        Assert.assertNotEquals("", responseString);
    }
}
