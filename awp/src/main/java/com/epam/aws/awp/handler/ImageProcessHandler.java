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

import java.util.stream.Collectors;

public class ImageProcessHandler implements RequestHandler<SQSEvent, APIGatewayProxyResponseEvent> {

    private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:704413653413:task10-uploads-notification-topic";
    private static final String REGION = "us-east-1";

    private final AmazonSNS amazonSNS = AmazonSNSClientBuilder.standard()
            .withRegion(REGION)
            .build();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(SQSEvent event, Context context) {
        LambdaLogger log = context.getLogger();
        try {
            String message = event.getRecords().stream()
                    .map(SQSMessage::getBody)
                    .collect(Collectors.joining("\n=========================\n"));
            log.log(" Result message = \n" + message);


            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(TOPIC_ARN)
                    .withSubject(" Processed SQS Queue Messages")
                    .withMessage(message);
            amazonSNS.publish(publishRequest);
        } catch (Exception e){
            log.log(e.getMessage());
            return null;
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody("")
                .withIsBase64Encoded(false);
    }
}