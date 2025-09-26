package boombimapi.domain.place.command.infrastructure;

import boombimapi.global.properties.StaticMapProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NaverStaticMapWebClient {

    private final WebClient webClient;
    private final StaticMapProperties properties;

    public NaverStaticMapWebClient(
        @Qualifier("naverStaticMapHttpClient") WebClient webClient,
        StaticMapProperties properties
    ) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public byte[] fetchStaticMapImage(
        double latitude,
        double longitude
    ) {
        String center = longitude + "," + latitude;
        String markers = properties.markerStyle()
            + "|pos:" + longitude + " " + latitude
            + "|viewSizeRatio:" + properties.viewSizeRatio();

        return webClient.get()
            .uri(uriBuilder -> uriBuilder.path("/map-static/v2/raster")
                .queryParam("w", properties.width())
                .queryParam("h", properties.height())
                .queryParam("level", properties.level())
                .queryParam("scale", properties.scale())
                .queryParam("center", center)
                .queryParam("markers", markers)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, res ->
                res.bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new RuntimeException(
                        "Naver StaticMap error: " + res.statusCode() + " / " + error))))
            .bodyToMono(byte[].class)
            .block();

    }

}
