package ru.ianedw.avitoparserclienttelegrambot.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ianedw.avitoparserclienttelegrambot.models.Update;
import ru.ianedw.avitoparserclienttelegrambot.models.TargetDTO;
import ru.ianedw.avitoparserclienttelegrambot.util.ApiClientException;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String POST_TARGET = "http://localhost:8080/targets";
    private static final String GET_LAST_POSTS = "http://localhost:8080/updates";

    @Autowired
    public ApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public TargetDTO sendTargetToParseServer(TargetDTO targetDTO) throws ApiClientException {
        try {
            String json = objectMapper.writeValueAsString(targetDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .uri(URI.create(POST_TARGET))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            targetDTO = objectMapper.readValue(response.body(), TargetDTO.class);
            return targetDTO;
        } catch (JsonProcessingException e) {
            throw new ApiClientException("Ошибка парсинга json");
        } catch (IOException | InterruptedException e) {
            throw new ApiClientException("Ошибка отправки запроса.");
        }
    }

    public Update getNewUpdate() throws ApiClientException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .uri(URI.create(GET_LAST_POSTS))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), Update.class);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("Ошибка парсинга json");
        } catch (IOException | InterruptedException e) {
            throw new ApiClientException("Ошибка отправки запроса.");
        }
    }
}
