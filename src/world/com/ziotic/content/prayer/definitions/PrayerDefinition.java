package com.ziotic.content.prayer.definitions;

import com.ziotic.content.prayer.PrayerManager.PrayerType;

public class PrayerDefinition extends AbstractPrayerDefinition {

    protected boolean protectFromMelee = false;
    protected boolean protectFromMagic = false;
    protected boolean protectFromRanged = false;

    protected boolean rapidRestore = false;
    protected boolean rapidHeal = false;
    protected boolean rapidRenewal = false;

    protected boolean retribution = false;
    protected boolean redemption = false;
    protected boolean smite = false;

    protected PrayerType prayerType;

    public final boolean isProtectFromMelee() {
        return protectFromMelee;
    }

    public final boolean isProtectFromMagic() {
        return protectFromMagic;
    }

    public final boolean isProtectFromRanged() {
        return protectFromRanged;
    }

    public final boolean isRapidRestore() {
        return rapidRestore;
    }

    public final boolean isRapidHeal() {
        return rapidHeal;
    }

    public final boolean isRapidRenewal() {
        return rapidRenewal;
    }

    public final boolean isRetribution() {
        return retribution;
    }

    public final boolean isRedemption() {
        return redemption;
    }

    public final boolean isSmite() {
        return smite;
    }

    public final PrayerType getType() {
        return prayerType;
    }
}
