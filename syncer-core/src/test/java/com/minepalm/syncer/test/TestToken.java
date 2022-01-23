package com.minepalm.syncer.test;

import com.minepalm.syncer.api.SyncToken;

public class TestToken implements SyncToken<TestObject> {
    @Override
    public String getObjectId(TestObject testObject) {
        return "TestObject_"+testObject.getText();
    }
}
