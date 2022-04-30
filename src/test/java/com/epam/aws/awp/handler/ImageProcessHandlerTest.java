package com.epam.aws.awp.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class ImageProcessHandlerTest {
    private ImageProcessHandler handler;
    @Mock
    Context context;
    @Mock
    LambdaLogger loggerMock;
    
    String message;

    private static SQSEvent load(String fixture) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(fixture, SQSEvent.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Before
    public void setUp() {
        when(context.getLogger()).thenReturn(loggerMock);

        doAnswer(call -> {
            message = call.getArgument(0);
            return null;
        }).when(loggerMock).log(anyString());

        handler = new ImageProcessHandler();
    }

    @Test
    public void handleRequest() {
        String payloadMessage = "{\n" +
                "  \"records\": [\n" +
                "    {\n" +
                "      \"messageId\": \"19dd0b57-b21e-4ac1-bd88-01bbb068cb78\",\n" +
                "      \"receiptHandle\": \"MessageReceiptHandle\",\n" +
                "      \"body\": \"Hello from SQS!\",\n" +
                "      \"attributes\": {\n" +
                "        \"ApproximateReceiveCount\": \"1\",\n" +
                "        \"SentTimestamp\": \"1523232000000\",\n" +
                "        \"SenderId\": \"123456789012\",\n" +
                "        \"ApproximateFirstReceiveTimestamp\": \"1523232000001\"\n" +
                "      },\n" +
                "      \"messageAttributes\": {},\n" +
                "      \"md5OfBody\": \"7b270e59b47ff90a553787216d55d91d\",\n" +
                "      \"eventSource\": \"aws:sqs\",\n" +
                "      \"eventSourceArn\": \"arn:{partition}:sqs:{region}:123456789012:MyQueue\",\n" +
                "      \"awsRegion\": \"{region}\"\n" +
                "    }\n" +
                "  ]\n}";
        SQSEvent sqsEvent = load(payloadMessage);
        APIGatewayProxyResponseEvent response = handler.handleRequest(sqsEvent, context);
        assertEquals(new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(message)
                .withIsBase64Encoded(false), response, "The Lambda function should return a welcome message");
    }
}
