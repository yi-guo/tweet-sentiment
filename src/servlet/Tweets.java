package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import dbmanager.GetTweetsRequest;

/**
 * Servlet implementation class Tweets
 */
public class Tweets extends HttpServlet {
    private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Tweets() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        String keyword = request.getParameter("keyword");
        String language = request.getParameter("language");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        response.setContentType("application/json");
        try {
            GetTweetsRequest getTweetsRequest = new GetTweetsRequest(keyword, language, startDate, endDate);
            JSONObject result = new JSONObject();
            result.put("tweets", getTweetsRequest.getCoordinates());
            result.put("status", 200);
            response.getOutputStream().print(result.toString());
            getTweetsRequest.shutdown();
        } catch (JSONException | ProvisionedThroughputExceededException e) {
            response.getOutputStream().print("{\"status\":400,\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        try {
            JSONObject message = new JSONObject(getRequestBody(request));
            if (message.getString("Type").equals("SubscriptionConfirmation")) {
                String url = message.getString("SubscribeURL");
                URLConnection connection = new URL(url).openConnection();
                connection.getContent();
            } else {
                JSONObject body = message.getJSONObject("MessageAttributes");
                AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
                AmazonDynamoDBClient dynamo = new AmazonDynamoDBClient(credentialsProvider);
                dynamo.setEndpoint("dynamodb.us-west-2.amazonaws.com");
                final Table table = new DynamoDB(dynamo).getTable("tweet-sentiment");
                Item item = new Item();
                item.withPrimaryKey("id", body.getJSONObject("id").getString("Value"))
                    .withString("name", body.getJSONObject("name").getString("Value"))
                    .withString("screen_name", body.getJSONObject("screen_name").getString("Value"))
                    .withString("text", message.getString("Message"))
                    .withString("created_at", body.getJSONObject("created_at").getString("Value"))
                    .withString("language", body.getJSONObject("language").getString("Value"))
                    .withString("latitude", body.getJSONObject("latitude").getString("Value"))
                    .withString("longitude", body.getJSONObject("longitude").getString("Value"))
                    .withString("sentiment", body.getJSONObject("sentiment").getString("Value"));
                table.putItem(item);
                dynamo.shutdown();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private static String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line);
        String requestBody = builder.toString();
        reader.close();
        return requestBody;
    }
    
}
