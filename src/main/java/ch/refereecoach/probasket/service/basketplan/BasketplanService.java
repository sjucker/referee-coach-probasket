package ch.refereecoach.probasket.service.basketplan;

import ch.refereecoach.probasket.common.OfficiatingMode;
import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.LocalDate;
import java.util.Optional;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketplanService {

    private static final String SEARCH_GAMES_URL = "https://www.basketplan.ch/showSearchGames.do?actionType=searchGames&gameNumber=%s&xmlView=true&perspective=de_default";

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

                return Optional.of(new BasketplanGameDTO(
                        gameNumber,
                        // TODO oder eher fullName?
                        getAttributeValue(leagueHoldingNode, "name").orElse("?"),
                        LocalDate.parse(getAttributeValue(gameNode, "date").orElseThrow()),
                        "%s - %s".formatted(getAttributeValue(resultNode, "homeTeamScore").orElse("?"),
                                getAttributeValue(resultNode, "guestTeamScore").orElse("?")),
                        getAttributeValue(homeTeamNode, "name").orElseThrow(),
                        Integer.valueOf(getAttributeValue(homeTeamNode, "id").orElseThrow()),
                        getAttributeValue(guestTeamNode, "name").orElseThrow(),
                        Integer.valueOf(getAttributeValue(guestTeamNode, "id").orElseThrow()),
                        // TODO
                        OfficiatingMode.OFFICIATING_2PO,
// TODO load ID if available, validation whether in DB?
                        null, null, null, null, null, null,
//                        getReferee(gameNode, "referee1Name"),
//                        getReferee(gameNode, "referee2Name"),
//                        getReferee(gameNode, "referee3Name"),
                        getAttributeValue(gameNode, "videoLink").orElse(null)
                ));
            }

        } catch (Exception e) {
            log.error("unexpected error while retrieving game-info from Basketplan", e);
        }

        return Optional.empty();
    }

    private Optional<String> getAttributeValue(Node parentNode, String name) {
        var node = parentNode.getAttributes().getNamedItem(name);
        return node != null ? Optional.ofNullable(node.getNodeValue()) : Optional.empty();
    }
}
