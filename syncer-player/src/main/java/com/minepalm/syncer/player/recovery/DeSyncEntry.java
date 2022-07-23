package com.minepalm.syncer.player.recovery;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DeSyncEntry {

    final SimplePlayerLog savePoint;
    final SimplePlayerLog loadPoint;

}
