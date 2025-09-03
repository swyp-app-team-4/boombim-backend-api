package boombimapi.domain.clova.infrastructure;

import boombimapi.domain.clova.dto.request.ClovaChatRequest;
import boombimapi.domain.clova.dto.response.ClovaResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ClovaCongestionMessageClient {

    private final WebClient clova;

    public ClovaCongestionMessageClient(
        @Qualifier("clovaCongestionMessageWebClient") WebClient clova
    ) {
        this.clova = clova;
    }

    public Mono<ClovaResponse> autoCompleteCongestionMessage(
        ClovaChatRequest body
    ) {
        return clova.post()
            .uri("")
            .bodyValue(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, res ->
                res.bodyToMono(String.class).flatMap(err ->
                    Mono.error(new RuntimeException("Clova error: " + res.statusCode() + " / " + err))))
            .bodyToMono(ClovaResponse.class);
    }

}
