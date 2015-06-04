package tweetstreamer;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetStreamer {
    /**
     * Main entry of this application.
     *
     * @param args
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws UnsupportedEncodingException 
     * @throws FileNotFoundException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws TwitterException, FileNotFoundException, IllegalArgumentException, IOException, InterruptedException {        
        
        // Create an Amazon AWS client using specified credentials.
        AWSCredentialsProvider credentials = new ClasspathPropertiesFileCredentialsProvider();
        
        // Create a SQS client.
        AmazonSQS sqs = new AmazonSQSClient(credentials);
        sqs.setRegion(Region.getRegion(Regions.US_WEST_2));
        
        String queueUrl = sqs.getQueueUrl("tweets").getQueueUrl();
        
        // Format date to a string
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if (status.getGeoLocation() != null) {
                    System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                    Item item = new Item();
                    item.withPrimaryKey("id", status.getId())
                        .withString("name", status.getUser().getName())
                        .withString("screen_name", status.getUser().getScreenName())
                        .withString("text", status.getText())
                        .withString("created_at", dateFormatter.format(status.getCreatedAt()))
                        .withString("language", String.valueOf(status.getLang()))
                        .withString("latitude", String.valueOf(status.getGeoLocation().getLatitude()))
                        .withString("longitude", String.valueOf(status.getGeoLocation().getLongitude()));
                    sqs.sendMessage(queueUrl, item.toJSON());
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//              System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
//              System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
//              System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
//              System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
    }

}