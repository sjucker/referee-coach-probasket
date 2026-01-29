package ch.refereecoach.probasket.common;

import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public enum CategoryType {
    GENERAL("General", "General", false),
    IMAGE("Image, Approach", "Image", true),
    FOULS("Criteria: Fouls", "Fouls", true),
    VIOLATIONS("Criteria: Violations", "Violations", true),
    MECHANICS(officiatingMode -> officiatingMode.getDescription() + " Mechanics & Individual Officiating Techniques", "Mechanics", true),
    FITNESS("Fitness Condition", "Fitness", true),
    GAME_CONTROL("Game Control and Management", "Game Control", true),
    POINTS_TO_KEEP("Points to Keep", "Keep", false),
    POINTS_TO_IMPROVE("Points to Improve", "Improve", false);

    private final Function<OfficiatingMode, String> description;
    private final String shortDescription;
    private final boolean scoreRequired;

    CategoryType(Function<OfficiatingMode, String> description, String shortDescription, boolean scoreRequired) {
        this.description = description;
        this.shortDescription = shortDescription;
        this.scoreRequired = scoreRequired;
    }

    CategoryType(String description, String shortDescription, boolean scoreRequired) {
        this(_ -> description, shortDescription, scoreRequired);
    }

    public List<String> getCriteriaHintsPerRank(Rank rank) {
        return switch (this) {
            case GENERAL, POINTS_TO_KEEP, POINTS_TO_IMPROVE -> List.of();
            case IMAGE -> switch (rank) {
                case RG1 -> List.of("Pünktlichkeit, aktives PreGame, Vorbild",
                                    "\"professionelle\" Spielvorbereitung (PreGame)",
                                    "Vorbild auf und neben dem Feld.",
                                    "Chef sein, nicht Polizist",
                                    "kann den Kollegen motivieren",
                                    "cool, aber nicht überheblich",
                                    "100% Kenntnis von Regeln und Weisungen");
                case RG2 -> List.of("Pünktlichkeit; aktives PreGame",
                                    "Korrektes Erscheinungsbild",
                                    "Kann das Spiel analysieren",
                                    "verhält sich auf und neben dem Spielfeld korrekt.",
                                    "keine Angst, Entscheidungen zu treffen - und zu verkaufen",
                                    "sicheres Auftreten",
                                    "Regeln beherrschen & anwenden (incl. \"Fouls in Sonderfällen\")");
                case RG3 -> List.of("Pünktlichkeit, Spielvorbereitung (PreGame)",
                                    "Erscheinungsbild korrekt.",
                                    "Spielanforderungen = \"was ist es für ein Spiel\" und \"was machen die Schiedsrichter daraus\"",
                                    "verhält sich auf und neben dem Spielfeld korrekt.",
                                    "keine Angst, Entscheidungen zu treffen",
                                    "sicheres Auftreten (kleine Unsicherheiten in schwierigen Fällen sind OK)",
                                    "Regeln beherrschen und anwenden (ohne \"Fouls in Sonderfällen\")");
                case RG4 -> List.of("Pünktlichkeit!",
                                    "Erscheinungsbild korrekt.",
                                    "ist motiviert",
                                    "Verhalten als Schiedsrichter (\"von morgens bis abends\")",
                                    "offen gegenüber Schiedsrichter-Partner und Tisch",
                                    "ein \"Minimum\" an Selbstbewusstsein",
                                    "Aufmerksamkeit gegenüber dem Spielgeschehen",
                                    "Selbst-Einschätzung, wie das Spiel verlaufen ist (u.a. auch: was kann ich noch nicht?)");
                case RK -> List.of("ist rechtzeitig am Spielort",
                                   "Erscheinungsbild korrekt.",
                                   "Verhalten als Schiedsrichter zumindest \"am Spieltag\" korrekt",
                                   "nimmt vom Kollegen Unterstützung an",
                                   "Aufmerksamkeit gegenüber dem Spielgeschehen",
                                   "ist im Besitz von Regeln und SR-Technik");
            };
            case FOULS -> switch (rank) {
                case RG1 -> List.of("\"das erste Foul erwischen...\"",
                                    "angepasste Interpretation (+ Mut zum \"no-call\"!)",
                                    "AOS / RSBQ / ADV/DADV");
                case RG2 -> List.of("klare Linie mit minimalen Schwankungen",
                                    "PPL (PW / CW / IW / QW)",
                                    "AOS / RSBQ");
                case RG3 -> List.of("erkennbare Linie, mit leichten Schwankungen, gutes Verkaufen der Entscheide",
                                    "Vor- / Nachteil werden angewendet",
                                    "Wurf- und 1-1-Situationen \"fehlerlos\" (RTD, B/CH)");
                case RG4 -> List.of("Foulentscheide mit korrekter Sanktion und korrektem Vorgehen (IOT)");
                case RK -> List.of("Foulentscheide mit korrekter Sanktion");
            };
            case VIOLATIONS -> switch (rank) {
                case RG1 -> List.of("Regelübertretungen: nicht nur können, sondern verstehen (ADV/DADV)",
                                    "souveräne IOT");
                case RG2 -> List.of("Regelübertretungen: nicht nur können, sondern verstehen (ADV/DADV)",
                                    "korrekte IOT");
                case RG3 -> List.of("Regelübertretungen \"fehlerfrei\"",
                                    "korrekte IOT");
                case RG4 -> List.of("Outball-Entscheide prompt angezeigt, mehrheitlich richtig entschieden",
                                    "Regelübertretungen, wenn gepfiffen, korrekt angezeigt / am richtigen Ort geahndet",
                                    "Rückpass");
                case RK -> List.of("Outball-Entscheide prompt angezeigt, mehrheitlich richtig entschieden",
                                   "Regelübertretungen, wenn gepfiffen, korrekt / am richtigen Ort geahndet",
                                   "Outbälle korrekt");
            };
            case MECHANICS -> switch (rank) {
                case RG1 -> List.of("aktiv beim Spiel ohne Ball",
                                    "bewegt sich / ist immer am richtigen Ort, hat die Infoluenzzonen im Griff",
                                    "cross step + Penetration",
                                    "Rotationen (Spiel lesen!)");
                case RG2 -> List.of("PPL",
                                    "hat das Spiel \"on ball\" und \"off ball\" im Griff (Influenzzonen)",
                                    "effiziente Positionen",
                                    "Standorte stimmen");
                case RG3 -> List.of("gute Kommunikation (Pfiff, Handzeichen)",
                                    "auch abseits des Balles schauen (Influenzzonen)",
                                    "effiziente Positionen",
                                    "Standorte stimmen");
                case RG4 -> List.of("Abläufe von Entscheid und Sanktion klar und korrekt",
                                    "dem Kollegen \"nicht vor die Füsse pfeifen\"",
                                    "aktiv das Fenster suchen, nicht stehen bleiben",
                                    "Positionen Einwurf / Freiwurf / Sprungball richtig");
                case RK -> List.of("Grundpositionen für Feld- & Korb-SR korrekt",
                                   "Bewegung im Gegenstoss",
                                   "Korrekte Bewegungen nach Einwurf / Freiwurf / Sprungball");
            };
            case FITNESS -> switch (rank) {
                case RG1 -> List.of("Kondition und Konzentration während dem ganzen Spiel!");
                case RG2 -> List.of("Aufwärmen / Einlaufen ist ein Standard");
                case RG3 -> List.of("Kondition, sinnvoll eingesetzt, dem Spiel entsprechend");
                case RG4 -> List.of("es wird gerannt...!");
                case RK -> List.of("es darf gerannt werden...!");
            };
            case GAME_CONTROL -> switch (rank) {
                case RG1 -> List.of("vorbeugend intervenieren",
                                    "gut verkaufte Entscheide",
                                    "hat Regeln und SR-Technik intus!",
                                    "Teamarbeit (auch vor dem Spiel) - trust your partner!",
                                    "Der SR darf das Spiel nicht schwierig machen (hilft dem Spiel)");
                case RG2 -> List.of("wenn der SR nach dem \"Buchstaben\" pfeift, ist er hier am falschen Ort",
                                    "Unbeeinflussbarkeit",
                                    "kennt Regeln (incl. Spezialfälle / Interpretationen) und Schiedsrichter-Technik",
                                    "aktive Zusammenarbeit mit Tisch / Teams; GH",
                                    "aktives GM incl. guten / passenden disziplinarischen Entscheide");
                case RG3 -> List.of("erkennt den Spielcharakter und kann entsprechend agieren",
                                    "Mut zu unpopulären Entscheiden",
                                    "kennt Regeln, Interpretationen und Schiedsrichter-Technik",
                                    "aktive Zusammenarbeit Tisch / Mannschaften",
                                    "richtiger Zeitpunkt für disziplinarische Entscheide");
                case RG4 -> List.of("grundlegendes Spielverständnis",
                                    "Mut zu Entscheidungen",
                                    "kennt Regeln (ohne Spezialfälle / Interpretationen) und Schiedsrichter-Technik",
                                    "Zusammenarbeit Tisch / Mannschaften wird erwartet!",
                                    "(teilweise) richtiger Zeitpunkt für disziplinarische Entscheide");
                case RK -> List.of("trifft Entscheidungen (pfeift)",
                                   "kennt Basis-Regeln",
                                   "Zusammenarbeit Tisch");
            };
        };
    }

}
