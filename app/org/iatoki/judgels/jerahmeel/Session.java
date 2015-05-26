package org.iatoki.judgels.jerahmeel;

public final class Session {

    private final long id;
    private final String jid;
    private final String name;
    private final String description;

    public Session(long id, String jid, String name, String description) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
