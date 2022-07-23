package com.minepalm.syncer.player.recovery;

import lombok.Data;

import java.util.UUID;

@Data
public class SimplePlayerLog {

    public final UUID uuid;
    public final Long task_id;
    public final String task_name;
    public final Long inventoryDate;
    public final Long taskDate;

}
