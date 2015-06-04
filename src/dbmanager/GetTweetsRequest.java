package dbmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class GetTweetsRequest {

    private String keyword;
    private String language;
    private String startDate;
    private String endDate;
    
    private Table table;
    private AmazonDynamoDBClient dynamo;
    
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public GetTweetsRequest(String keyword, String language, String startDate, String endDate) {
        this.setKeyword(keyword);
        this.setLanguage(language);
        this.setStartDate(startDate);
        this.setEndDate(endDate);
        AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        dynamo = new AmazonDynamoDBClient(credentialsProvider);
        dynamo.setEndpoint("dynamodb.us-west-2.amazonaws.com");
        table = new DynamoDB(dynamo).getTable("tweet-sentiment");
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate.isEmpty() ? "0000-01-01 00:00:00" : (startDate + " 00:00:00");
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate.isEmpty() ? dateFormatter.format(new Date()) : (endDate + " 00:00:00");
    }
    
    public JSONArray getCoordinates() throws JSONException {  
        StringBuilder filterExpression = new StringBuilder("#time > :s and #time < :e");
        ValueMap valueMap = new ValueMap()
            .withString(":s", startDate)
            .withString(":e", endDate);
        NameMap nameMap = new NameMap()
            .with("#time", "created_at");
        if (!keyword.isEmpty()) {
            nameMap.with("#text", "text");
            valueMap.withString(":k", keyword);
            filterExpression.append(" and contains(#t, :k)");
        }
        if (!language.equals("default")) {
            nameMap.with("#language", "language");
            valueMap.withString(":l", language);
            filterExpression.append(" and #language = :l");
        }       
        ScanSpec scanSpec = new ScanSpec()
            .withFilterExpression(filterExpression.toString())
            .withNameMap(nameMap)
            .withValueMap(valueMap)
            .withProjectionExpression("latitude, longitude, sentiment");
        
        JSONArray coordinates = new JSONArray();
        ItemCollection<ScanOutcome> items = table.scan(scanSpec);
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext())
            coordinates.put(new JSONObject(iterator.next().toJSON()));
        return coordinates;
    }
    
    public void shutdown() {
        dynamo.shutdown();
    }
    
}
