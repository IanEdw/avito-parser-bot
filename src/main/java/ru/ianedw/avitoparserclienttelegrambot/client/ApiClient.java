package ru.ianedw.avitoparserclienttelegrambot.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ianedw.avitoparserclienttelegrambot.config.Config;
import ru.ianedw.avitoparserclienttelegrambot.models.Update;
import ru.ianedw.avitoparserclienttelegrambot.models.TargetDTO;
import ru.ianedw.avitoparserclienttelegrambot.util.ApiClientException;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String PARSER_URL;

    @Autowired
    public ApiClient(HttpClient httpClient, ObjectMapper objectMapper, Config config) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        PARSER_URL = config.getParserUrl();
    }

    public TargetDTO sendTargetToParseServer(TargetDTO targetDTO) throws ApiClientException {
        try {
            String json = objectMapper.writeValueAsString(targetDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .uri(URI.create(PARSER_URL + "/targets"))
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
                    .uri(URI.create(PARSER_URL + "/updates"))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), Update.class);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("Ошибка парсинга json");
        } catch (IOException | InterruptedException e) {
            throw new ApiClientException("Ошибка отправки запроса.");
        }
    }

    public void removeTargetsFromServer(List<Integer> targetIdsToRemove) {
        try {
            for (Integer integer : targetIdsToRemove) {
                HttpRequest request = HttpRequest.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .DELETE()
                        .uri(URI.create(PARSER_URL + "/targets?id=" + integer))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (IOException | InterruptedException ignored) {
        }
    }
}
