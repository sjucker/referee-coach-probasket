package ch.refereecoach.probasket.service.basketplan;

import ch.refereecoach.probasket.common.Rank;
import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import static ch.refereecoach.probasket.common.Rank.RK;
import static ch.refereecoach.probasket.util.DateUtil.today;
import static ch.refereecoach.probasket.util.XmlUtil.getAttributeValue;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.springframework.http.MediaType.APPLICATION_XML;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketplanUserSyncService {
    private static final String REFEREE_DATA_URL = "https://www.basketplan.ch/showRefereeDataXML.do?federationId=%d&validAfterDate=%s&xmlView=ref";

    private final ApplicationProperties applicationProperties;
    private final Builder webClientBuilder;
    private final LoginDao loginDao;

    @Scheduled(initialDelay = 0, fixedDelay = 24, timeUnit = HOURS)
    public void syncReferees() {
        log.info("Synchronizing referees from basketplan");
        try {
            var client = webClientBuilder.build();

            var existingPerId = loginDao.findAll().stream()
                                        .collect(toMap(Login::getId, identity()));

            var body = client.get()
                             .uri(REFEREE_DATA_URL.formatted(applicationProperties.getFederationId(), today().minusYears(2).toString()))
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

                var refereeList = doc.getDocumentElement().getElementsByTagName("PersonXMLReferee");
                var receivedIds = new ArrayList<Long>();
                for (var i = 0; i < refereeList.getLength(); i++) {
                    var node = (Element) refereeList.item(i);

                    var id = getAttributeValue(node, "id").map(Long::valueOf).orElse(null);
                    var email = getAttributeValue(node, "email").orElse(null);
                    var familyName = getAttributeValue(node, "familyName").orElse(null);
                    var firstName = getAttributeValue(node, "firstName").orElse(null);
                    var active = getAttributeValue(node, "active").map(Boolean::parseBoolean).orElse(false);

                    var refereeAuthorisation = node.getElementsByTagName("refereeAuthorisation").item(0);
                    var rank = getAttributeValue(refereeAuthorisation, "highestRefereeQualificationId")
                            .map(it -> Rank.fromQualificationId(NumberUtils.toInt(it)))
                            .orElse(RK);

                    if (id != null && email != null && familyName != null && firstName != null) {
                        var existing = existingPerId.get(id);
                        if (existing != null) {
                            existing.setEmail(email);
                            existing.setFirstname(firstName);
                            existing.setLastname(familyName);
                            existing.setActive(active);
                            existing.setRank(rank.name());
                            loginDao.update(existing);
                        } else {
                            loginDao.insert(new Login(id, firstName, familyName, email, null, false, true, false, false, false, rank.name(), active, false, null));
                            log.info("added referee %s %s (%d)".formatted(firstName, familyName, id));
                        }
                        receivedIds.add(id);
                    }
                }
                existingPerId.entrySet().stream()
                             .filter(entry -> !receivedIds.contains(entry.getKey()))
                             .forEach(entry -> {
                                 var login = entry.getValue();
                                 // make sure yours truly can still login even if not active anymore
                                 if (!login.getAdmin() && login.getActive()) {
                                     login.setActive(false);
                                     loginDao.update(login);
                                     log.info("deactivated referee %s %s (%d)".formatted(login.getFirstname(), login.getLastname(), login.getId()));
                                 }
                             });
            } else {
                log.warn("Basketplan referee-data call returned empty body");
            }
        } catch (Exception e) {
            log.error("unexpected error while retrieving referee-data from Basketplan", e);
        }
    }

}
