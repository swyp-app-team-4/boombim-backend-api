package boombimapi.global.config.converter;

import boombimapi.domain.oauth2.domain.entity.SocialProvider;
import boombimapi.global.infra.exception.error.BoombimException;
import boombimapi.global.infra.exception.error.ErrorCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSocialProviderConverter implements Converter<String, SocialProvider> {
    @Override
    public SocialProvider convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new BoombimException(ErrorCode.INVALID_PROVIDER);
        }

        String upperSource = source.trim().toUpperCase();

        switch (upperSource) {
            case "KAKAO":
                return SocialProvider.KAKAO;
            case "NAVER":
                return SocialProvider.NAVER;
            case "APPLE": // apple은 상황봐서 빼야될듯
                return SocialProvider.APPLE;
            default:
                throw new BoombimException(ErrorCode.INVALID_PROVIDER);
        }
    }
}