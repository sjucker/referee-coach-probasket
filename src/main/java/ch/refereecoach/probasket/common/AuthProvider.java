package ch.refereecoach.probasket.common;

public enum AuthProvider {
    /**
     * users authenticate against basketplan.ch, the local user id is the basketplan personId
     */
    BASKETPLAN,
    /**
     * users authenticate against the password stored in the local login table
     */
    LOCAL
}
