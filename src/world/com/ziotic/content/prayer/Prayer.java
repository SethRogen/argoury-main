package com.ziotic.content.prayer;

import com.ziotic.content.prayer.definitions.PrayerDefinition;

public class Prayer extends AbstractPrayer {

    public Prayer(PrayerDefinition definition) {
        super(definition);
        this.definition = (PrayerDefinition) definition;
    }

    protected PrayerDefinition definition;

    protected PrayerDefinition getDefinition() {
        return definition;
    }

}
