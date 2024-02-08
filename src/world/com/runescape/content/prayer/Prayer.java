package com.runescape.content.prayer;

import com.runescape.content.prayer.definitions.PrayerDefinition;

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
