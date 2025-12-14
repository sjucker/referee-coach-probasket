package ch.refereecoach.probasket.service.basketplan;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import static ch.refereecoach.probasket.util.XmlUtil.getAttributeInElement;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.springframework.http.MediaType.APPLICATION_XML;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketplanAuthenticationService {

    private static final String AUTHORIZE_URL = "https://www.basketplan.ch/authorizeUserXML.do?userName=%s&password=%s&xmlView=ref";

    private final ApplicationProperties applicationProperties;
    private final Builder webClientBuilder;

    public Optional<Long> authenticate(String username, String password) {
        try {
            var client = webClientBuilder.build();

            byte[] body = client.get()
                                .uri(AUTHORIZE_URL.formatted(username, md5Hex(password)))
                                .accept(APPLICATION_XML)
                                .headers(headers -> headers.set("refApiKey", applicationProperties.getBasketplanApiKey()))
                                .retrieve()
                                .bodyToMono(byte[].class)
                                .block();

            if (body != null) {
                var dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature(FEATURE_SECURE_PROCESSING, true);
                var db = dbf.newDocumentBuilder();
                var doc = db.parse(new ByteArrayInputStream(body));
                doc.getDocumentElement().normalize();

                boolean accessAllowed = getAttributeInElement(doc, "AuthorizeUserResponse", "accessAllowed").map(Boolean::parseBoolean).orElse(false);
                if (accessAllowed) {
                    var personId = getAttributeInElement(doc, "AuthorizeUserResponse", "personId").orElse(null);
                    var personName = getAttributeInElement(doc, "AuthorizeUserResponse", "personName").orElse(null);
                    log.info("Basketplan authentication successful for user {}: personId={}, personName={}", username, personId, personName);
                    return Optional.ofNullable(personId).map(Long::valueOf);
                } else {
                    var message = getAttributeInElement(doc, "AuthorizeUserResponse", "message").orElse("");
                    if (Strings.CI.contains(message, "wrong type")) {
                        // for now, log this on error, so we can inform the user
                        log.error("Basketplan authentication failed for user {} with message: {}", username, message);
                    } else {
                        log.warn("Basketplan authentication failed for user {} with message: {}", username, message);
                    }
                    return Optional.empty();
                }
            } else {
                log.warn("Basketplan authorize call returned empty body");
            }
        } catch (Exception e) {
            log.error("Failed to call Basketplan authorize endpoint via WebClient", e);
        }

        return Optional.empty();
    }

}
