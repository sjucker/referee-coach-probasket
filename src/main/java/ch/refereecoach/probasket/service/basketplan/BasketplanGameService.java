package ch.refereecoach.probasket.service.basketplan;

import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import ch.refereecoach.probasket.service.report.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;
import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_3PO;
import static ch.refereecoach.probasket.util.XmlUtil.getAttributeValue;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketplanGameService {

    private static final String SEARCH_GAMES_URL = "https://www.basketplan.ch/showSearchGames.do?actionType=searchGames&gameNumber=%s&xmlView=true&perspective=de_default";

    private final UserService userService;
    private final Builder webClientBuilder;

    public Optional<BasketplanGameDTO> findGameByNumber(String gameNumber) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setFeature(FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(SEARCH_GAMES_URL.formatted(gameNumber));
            doc.getDocumentElement().normalize();

            NodeList games = doc.getDocumentElement().getElementsByTagName("game");
            if (games.getLength() == 1) {
                var gameNode = games.item(0);

                var leagueHoldingNode = ((Element) gameNode).getElementsByTagName("leagueHolding").item(0);
                var homeTeamNode = ((Element) gameNode).getElementsByTagName("homeTeam").item(0);
                var guestTeamNode = ((Element) gameNode).getElementsByTagName("guestTeam").item(0);
                var resultNode = ((Element) gameNode).getElementsByTagName("result").item(0);

                if (!getAttributeValue(gameNode, "hasRefereesToDisplay").map(Boolean::parseBoolean).orElse(false)) {
                    log.error("referees not available for game {}", gameNumber);
                }

                var referee1 = getReferee(gameNode, "referee1Id");
                var referee2 = getReferee(gameNode, "referee2Id");
                var referee3 = getReferee(gameNode, "referee3Id");

                return Optional.of(new BasketplanGameDTO(
                        gameNumber,
                        getAttributeValue(leagueHoldingNode, "fullName").orElse("?"),
                        LocalDate.parse(getAttributeValue(gameNode, "date").orElseThrow()),
                        "%s - %s".formatted(getAttributeValue(resultNode, "homeTeamScore").orElse("?"),
                                            getAttributeValue(resultNode, "guestTeamScore").orElse("?")),
                        getAttributeValue(homeTeamNode, "name").orElseThrow(),
                        Integer.valueOf(getAttributeValue(homeTeamNode, "id").orElseThrow()),
                        getAttributeValue(guestTeamNode, "name").orElseThrow(),
                        Integer.valueOf(getAttributeValue(guestTeamNode, "id").orElseThrow()),
                        getAttributeValue(gameNode, "referee3Id").isPresent() ? OFFICIATING_3PO : OFFICIATING_2PO,
                        referee1.map(UserDTO::id).orElse(null), referee1.map(UserDTO::fullName).orElse(null), referee1.map(UserDTO::rank).orElse(null),
                        referee2.map(UserDTO::id).orElse(null), referee2.map(UserDTO::fullName).orElse(null), referee2.map(UserDTO::rank).orElse(null),
                        referee3.map(UserDTO::id).orElse(null), referee3.map(UserDTO::fullName).orElse(null), referee3.map(UserDTO::rank).orElse(null),
                        getAttributeValue(gameNode, "videoLink")
                                .or(() -> findAsportVideoUrl(gameNumber))
                                .orElse(null)
                ));
            }

        } catch (Exception e) {
            log.error("unexpected error while retrieving game-info from Basketplan", e);
        }

        return Optional.empty();
    }

    private Optional<UserDTO> getReferee(Node gameNode, String name) {
        Optional<String> refereeId = getAttributeValue(gameNode, name);
        if (refereeId.isPresent()) {
            Optional<UserDTO> referee = userService.findById(toLong(refereeId.get()));
            if (referee.isEmpty()) {
                log.error("referee '{}' not found in database", refereeId.get());
            }
            return referee;
        }
        return Optional.empty();
    }

    private Optional<String> findAsportVideoUrl(String gameNumber) {
        var foo = UriComponentsBuilder.fromUriString("https://manager.asport.tv/api/v1/events")
                                      .queryParam("filters", "{\"externalReference\":{\"basketplan\":\"%s\"}}".formatted(gameNumber))
                                      .build();

        var body = webClientBuilder.build().get()
                                   .uri(foo.toUri())
                                   .accept(APPLICATION_JSON)
                                   .retrieve()
                                   .bodyToMono(EventsResponse.class)
                                   .block();

        if (body != null && body.data.size() == 1) {
            return Optional.ofNullable(body.data.getFirst().id)
                           .map(("https://probasket.asport.tv/event/%d/embed")::formatted);
        }

        return Optional.empty();
    }

    private record EventsResponse(List<Event> data) {
    }

    private record Event(Long id) {
    }

}
