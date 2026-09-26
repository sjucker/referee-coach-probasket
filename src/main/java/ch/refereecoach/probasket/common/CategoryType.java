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
                case RG1 -> List.of("Puntualità, PreGame attivo, essere d'esempio",
                                    "Preparazione alla partita \"professionale\" (PreGame)",
                                    "Essere d'esempio dentro e fuori dal campo.",
                                    "Essere il capo, non il poliziotto",
                                    "Sa motivare il collega",
                                    "Disinvolto, ma non arrogante",
                                    "Conoscenza al 100% delle regole e direttive");
                case RG2 -> List.of("Puntualità; PreGame attivo",
                                    "Aspetto corretto",
                                    "Sa analizzare la partita",
                                    "Si comporta correttamente dentro e fuori dal campo.",
                                    "Nessun timore di prendere decisioni - e di venderle",
                                    "Presenza sicura",
                                    "Padroneggiare e applicare le regole (inclusi i \"falli nelle situazioni speciali\")");
                case RG3 -> List.of("Puntualità, preparazione alla partita (PreGame)",
                                    "Aspetto corretto.",
                                    "Esigenze della partita = \"che tipo di partita è\" e \"come la gesticono gli arbitri\"",
                                    "Si comporta correttamente dentro e fuori dal campo.",
                                    "Nessun timore di prendere decisioni",
                                    "Presenza sicura (piccole incertezze in situazioni difficili sono accettabili)",
                                    "Padroneggiare e applicare le regole (esclusi i \"falli in situazioni speciali\")");
                case RG4 -> List.of("Puntualità!",
                                    "Aspetto corretto.",
                                    "È motivato",
                                    "Comportamento da arbitro (\"in tutte le situazioni\")",
                                    "Aperto verso il collega arbitro e il tavolo",
                                    "Un \"minimo\" di sicurezza in sé",
                                    "Attenzione a ciò che accade in partita",
                                    "Autovalutazione sull'andamento della partita (tra l'altro: che cosa non so ancora fare?)");
                case RK -> List.of("Arriva puntuale sul luogo della partita",
                                   "Aspetto corretto.",
                                   "Comportamento da arbitro corretto almeno \"nel giorno della partita\"",
                                   "Accetta il supporto del collega",
                                   "Attenzione a ciò che accade in partita",
                                   "Dispone del regolamento e della tecnica arbitrale");
            };
            case FOULS -> switch (rank) {
                case RG1 -> List.of("\"prendere il primo fallo...\"",
                                    "Interpretazione adeguata (+ coraggio del \"no-call\"!)",
                                    "AOS / RSBQ / ADV/DADV",
                                    "(se necessario): uso mirato dei falli tecnici e corretta interpretazione dei falli Disruptive / Flagrant");
                case RG2 -> List.of("Linea chiara con oscillazioni minime",
                                    "PPL (PW / CW / IW / QW)",
                                    "AOS / RSBQ",
                                    "(se necessario): uso mirato dei falli tecnici e corretta interpretazione dei falli Disruptive / Flagrant");
                case RG3 -> List.of("Linea riconoscibile, con lievi variazioni, buona vendita delle decisioni",
                                    "Vantaggio / svantaggio vengono applicati",
                                    "Situazioni di tiro e di 1c1 \"senza errori\" (RTD, B/CH)",
                                    "(se necessario): uso mirato dei falli tecnici e corretta interpretazione dei falli Disruptive / Flagrant");
                case RG4 -> List.of("Decisioni sui falli con sanzione corretta e procedura corretta (IOT)",
                                    "(se necessario): uso mirato dei falli tecnici e corretta interpretazione dei falli Disruptive / Flagrant");
                case RK -> List.of("Decisioni sui falli con sanzione corretta");
            };
            case VIOLATIONS -> switch (rank) {
                case RG1 -> List.of("Infrazioni: non solo saperle, ma capirle (ADV/DADV)",
                                    "IOT buona conoscenza del IOT");
                case RG2 -> List.of("Infrazioni: non solo saperle, ma capirle (ADV/DADV)",
                                    "IOT corretta");
                case RG3 -> List.of("Infrazioni \"senza errori\"",
                                    "IOT corretta");
                case RG4 -> List.of("Decisioni di palla fuori segnalate prontamente, in prevalenza corrette",
                                    "Infrazioni, se fischiate, segnalate correttamente / sanzionate nel punto giusto",
                                    "Ritorno in zona difensiva (backcourt)");
                case RK -> List.of("Decisioni di palla fuori segnalate prontamente, in prevalenza corrette",
                                   "Infrazioni, se fischiate, sanzionate correttamente / nel punto giusto",
                                   "Palle fuori corrette");
            };
            case MECHANICS -> switch (rank) {
                case RG1 -> List.of("Attivo sul gioco senza palla",
                                    "Si muove / è sempre nel posto giusto, ha sotto controllo le zone di influenza",
                                    "cross step + Penetration",
                                    "Rotazioni (leggere il gioco!)");
                case RG2 -> List.of("PPL",
                                    "Ha il controllo del gioco \"on ball\" e \"off ball\" (zone di influenza)",
                                    "Posizioni efficienti",
                                    "Le posizioni sono corrette");
                case RG3 -> List.of("Buona comunicazione (fischio, segnalazioni)",
                                    "Guardare anche lontano dalla palla (zone di influenza)",
                                    "Posizioni efficienti",
                                    "Le posizioni sono corrette");
                case RG4 -> List.of("Procedure di decisione e sanzione chiare e corrette",
                                    "Non \"rubare\" il fischio al collega",
                                    "Cercare attivamente OA, non restare fermi",
                                    "Posizioni corrette su rimessa / tiri liberi / salto a due");
                case RK -> List.of("Posizioni base corrette per arbitro di campo e di canestro",
                                   "Movimento in contropiede",
                                   "Movimenti corretti dopo rimessa / tiri liberi / salto a due");
            };
            case FITNESS -> switch (rank) {
                case RG1 -> List.of("Condizione fisica e concentrazione per tutta la partita!");
                case RG2 -> List.of("Il riscaldamento è uno standard");
                case RG3 -> List.of("Condizione fisica, impiegata in modo sensato, adeguata alla partita");
                case RG4 -> List.of("Si corre...!");
                case RK -> List.of("Si può anche correre...!");
            };
            case GAME_CONTROL -> switch (rank) {
                case RG1 -> List.of("Intervenire in modo preventivo",
                                    "Decisioni ben vendute",
                                    "Ha regole e tecnica arbitrale nel sangue!",
                                    "Lavoro di squadra (anche prima della partita) - trust your partner!",
                                    "L'arbitro non deve rendere difficile la partita (aiuta il gioco)");
                case RG2 -> List.of("Se l'arbitro fischia \"alla lettera\", qui è fuori posto",
                                    "Non è influenzabile",
                                    "Conosce le regole (inclusi casi speciali / interpretazioni) e la tecnica arbitrale",
                                    "Collaborazione attiva con tavolo / squadre; GH",
                                    "GM proattivo, incluse decisioni disciplinari buone / adeguate");
                case RG3 -> List.of("Riconosce il carattere della partita e sa agire di conseguenza",
                                    "Coraggio di prendere decisioni impopolari",
                                    "Conosce regole, interpretazioni e tecnica arbitrale",
                                    "Collaborazione attiva con tavolo / squadre",
                                    "Momento giusto per le decisioni disciplinari");
                case RG4 -> List.of("Comprensione di base del gioco",
                                    "Coraggio di decidere",
                                    "Conosce le regole (esclusi casi speciali / interpretazioni) e la tecnica arbitrale",
                                    "Ci si aspetta collaborazione con tavolo / squadre!",
                                    "Momento (in parte) giusto per le decisioni disciplinari");
                case RK -> List.of("Prende decisioni (fischia)",
                                   "Conosce le regole di base",
                                   "Collaborazione con il tavolo");
            };
        };
    }

}
