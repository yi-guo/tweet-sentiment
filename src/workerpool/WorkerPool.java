package workerpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class WorkerPool extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String UTF_8 = Charset.forName("UTF-8").name();
    private final static String topicArn = "arn:aws:sns:us-west-2:665550041439:tweets";
    
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        try {
            AWSCredentialsProvider credentials = new ClasspathPropertiesFileCredentialsProvider();
            AmazonSNSClient sns = new AmazonSNSClient(credentials);
            sns.setEndpoint("sns.us-west-2.amazonaws.com");
            JSONObject message = new JSONObject(getRequestBody(request));
            String tweet = message.getString("text");
            String sentiment = getSentiment(tweet);
            PublishRequest publishRequest = new PublishRequest(topicArn, tweet);
            publishRequest.addMessageAttributesEntry("id",
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("id")));
            publishRequest.addMessageAttributesEntry("name",
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("name")));
            publishRequest.addMessageAttributesEntry("screen_name",
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("screen_name")));
            publishRequest.addMessageAttributesEntry("created_at",
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("created_at")));
            publishRequest.addMessageAttributesEntry("language",
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("language")));
            publishRequest.addMessageAttributesEntry("longitude", 
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("longitude")));
            publishRequest.addMessageAttributesEntry("latitude", 
                    new MessageAttributeValue().withDataType("String").withStringValue(message.getString("latitude")));
            publishRequest.addMessageAttributesEntry("sentiment", 
                    new MessageAttributeValue().withDataType("String").withStringValue(sentiment));
            PublishResult publishResult = sns.publish(publishRequest);
            System.out.println(publishResult.getMessageId());
            response.setStatus(200);
            
        } catch (RuntimeException | JSONException exception) {
            
            response.setStatus(200);
            try (PrintWriter writer = new PrintWriter(response.getOutputStream())) {
                exception.printStackTrace(writer);
            }
        }
        
    }
    
    private static String getSentiment(String tweet) throws MalformedURLException, IOException {
        try {
            String key = "";
            String url = "http://access.alchemyapi.com/calls/text/TextGetTextSentiment";
            String query = String.format("apikey=%s&text=%s&outputMode=json",
                    URLEncoder.encode(key, UTF_8), URLEncoder.encode(tweet, UTF_8));
            URLConnection connection = new URL(url + "?" + query).openConnection();
            connection.setRequestProperty("Accept-Charset", UTF_8);
            InputStream response = (InputStream) connection.getContent();
            byte[] contentRaw = new byte[connection.getContentLength()];
            response.read(contentRaw);
            JSONObject content = new JSONObject(new String(contentRaw));
            return content.getJSONObject("docSentiment").getString("score");
        } catch (JSONException exception) {
            return "0";
        }
    }
    
    private static String getRequestBody(final HttpServletRequest request) throws IOException {
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
