# tweet-sentiment
A heat map visualization of tweet sentiment.

This is a web application that depicts tweets in a heat map depending on their sentiment as they are being collected in near real time. The application utilizes AJAX, sending GET request to update the front end for every X seconds as the streamer listens to and saves tweets into DynamoDB.

The front end includes search funcationality where you may choose to depict tweets by keyword, language, start date, end date, and any combinations of the mentioned. The front end before conducting the search also validates the input date, and only processes valid input.

To run, please construct an AWS Java web application following the same structure. You will also need to have your AWS and Twitter credentials declared in AwsCredentials.properties and twitter4j.properties respectively and save them under the src directory. In addition, the project utilizes AlchemyAPI which returns a sentiment value of a given tweet. To use AlchemyAPI, you need to register for a key and replace the value with it in the file WorkerPool.java. Note that WorkerPool.java is a separate worker server that needs to be declared in a separate AWS Java Worker Tier Applicaiton/Project.

For the format of the two properties, please refer to
  - Amazon AWS: http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
  - Twitter4j: http://twitter4j.org/en/configuration.html
  - AlchemyAPI: http://www.alchemyapi.com/developers
