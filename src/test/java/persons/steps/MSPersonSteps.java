package persons.steps;

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

public class MSPersonSteps {
    private final World world;
    private final Properties envConfig;
    private RequestSpecification request;

    public MSPersonSteps(World world) {
        this.world = world;
        this.envConfig = World.envConfig;
        this.world.featureContext = World.threadLocal.get();
    }

    @Before
    public void setUp() {
        request = RequestSpecificationFactory.getInstance();
    }

    @Given("an person with valid information")
    public void getPersonValidInformation(@Transpose DataTable dataTable) throws IOException {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        String name = data.get(0).get("name");
        String lastName = data.get(0).get("lastName");
        String ci = data.get(0).get("ci");
        String userId = data.get(0).get("userId");

        Map<String, Object> valuesToTemplate = new HashMap<>();
        valuesToTemplate.put("name", name);
        valuesToTemplate.put("lastName", lastName);
        valuesToTemplate.put("ci", ci);
        valuesToTemplate.put("userId", userId);

        String jsonAsString = jsonTemplate(envConfig.getProperty("mspersons-person_request"), valuesToTemplate);
        world.scenarioContext.put("requestStr", jsonAsString);
    }

    @When("request is submitted for person creation")
    public void submitItemCreation(){
        String payload = world.scenarioContext.get("requestStr").toString();
        Response response = request
                .accept(ContentType.JSON)
                .body(payload)
                .contentType(ContentType.JSON)
                .when().post(envConfig.getProperty("mspersons-service_url")
                        + envConfig.getProperty("mspersons-person_api"));
        world.scenarioContext.put("response", response);
    }

    @Then("verify that the Person HTTP response is {int}")
    public void verifyHTTPResponseCode(Integer status){
        Response response = (Response) world.scenarioContext.get("response");
        Integer actualStatusCode = response.then()
                .extract()
                .statusCode();
        Assert.assertEquals(status, actualStatusCode);
    }

    @Then("a person Object is returned")
    public void checkPersonId(){
        Response response = (Response) world.scenarioContext.get("response");
        String responseString = response.then().extract().asString();
        System.out.println(responseString);
        Assert.assertNotNull(responseString);
        Assert.assertNotEquals("", responseString);
    }
}
