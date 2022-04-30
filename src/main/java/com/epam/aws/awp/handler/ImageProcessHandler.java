package com.epam.aws.awp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class ImageProcessHandler implements RequestHandler<SQSEvent, APIGatewayProxyResponseEvent> {

    private static final String TOPIC_ARN;
    private static final String REGION;
    private static final AmazonSNS amazonSNS; 
    static {
        Properties properties = new Properties();
        try (InputStream is = ImageProcessHandler.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TOPIC_ARN  = properties.getProperty("topic-arn");
        REGION = properties.getProperty("region");
        amazonSNS = AmazonSNSClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(SQSEvent event, Context context) {
        String message;
        LambdaLogger log = context.getLogger();
        try {
             message = event.getRecords().stream()
                    .map(SQSMessage::getBody)
                    .collect(Collectors.joining("\n=========================\n"));
            log.log(" Result message = \n" + message);

            if(!TOPIC_ARN.equals("")) {
                PublishRequest publishRequest = new PublishRequest()
                        .withTopicArn(TOPIC_ARN)
                        .withSubject(" Processed SQS Queue Messages")
                        .withMessage(message);
                amazonSNS.publish(publishRequest);
            }
        } catch (Exception e) {
            log.log(e.getMessage());
            return null;
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(" Result message = \n" + message)
                .withIsBase64Encoded(false);
    }
}