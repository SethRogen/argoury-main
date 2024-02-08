package com.runescape.logic.mask;

import com.runescape.logic.Entity;
import com.runescape.logic.Node;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public class Splat implements Mask {
    public static enum SplatType {
        DAMAGE, BLOCK(8), SOAK(5, 19), POISON(6, 20), DISEASE(7);

        private int id;
        private int darkId;

        private SplatType() {
            this(-1);
        }

        private SplatType(int id) {
            this(id, -1);
        }

        private SplatType(int id, int darkId) {
            this.id = id;
            this.darkId = darkId;
        }

        public int getId() {
            return id;
        }

        public int getDarkId() {
            if (darkId == -1) {
                return id;
            }
            return darkId;
        }
    }

    public static enum SplatCause {
        MELEE(0, 14, 10, 24), RANGE(1, 15, 11, 25), MAGIC(2, 16, 12, 26), DEFLECT(4, 18), CANNON(13, 27), NONE(3, 17);

        private int id;
        private int darkId;
        private int maxId;
        private int maxDarkId;

        private SplatCause(int id, int darkId) {
            this(id, darkId, -1, -1);
        }

        private SplatCause(int id, int darkId, int maxId, int maxDarkId) {
            this.id = id;
            this.darkId = darkId;
            this.maxId = maxId;
            this.maxDarkId = maxDarkId;
        }

        public int getId() {
            return id;
        }

        public int getDarkId() {
            if (darkId == -1) {
                return id;
            }
            return darkId;
        }

        public int getMaxId() {
            if (maxId == -1) {
                return id;
            }
            return maxId;
        }

        public int getMaxDarkId() {
            if (maxDarkId == -1) {
                return getMaxId();
            }
            return maxDarkId;
        }
    }

    private Entity owner; // The owner of this splat or the 'victim'
    private Node inflicter; // The node from which this splat was inflicted
    private int amount; // The amount of damage involved
    private SplatType type; // The type of splat
    private SplatCause cause; // The cause of this splat
    private boolean max; // If this is the maximum damage possible by the owner of this splat.
    private int delay = 0;

    public Splat(Entity owner, Entity inflicter, int amount, SplatType type, SplatCause cause) {
        this(owner, inflicter, amount, type, cause, false, 0);
    }

    public Splat(Entity owner, Entity inflicter, int amount, SplatType type, SplatCause cause, boolean max) {
        this(owner, inflicter, amount, type, cause, max, 0);
    }

    public Splat(Entity owner, Entity inflicter, int amount, SplatType type, SplatCause cause, boolean max, int delay) {
        this.owner = owner;
        this.inflicter = inflicter;
        this.amount = amount;
        this.type = type;
        this.cause = cause;
        this.max = max;
        this.delay = delay;
    }

    public Node getOwner() {
        return owner;
    }

    public Node getInflicter() {
        return inflicter;
    }

    public int getAmount() {
        return amount;
    }

    public SplatType getType() {
        return type;
    }

    public SplatCause getCause() {
        return cause;
    }

    public boolean isMax() {
        return max;
    }

    public int typePerspectiveOfSpectator(Player spectator) {
        boolean dark = spectator != owner && spectator != inflicter;

        if (type == Splat.SplatType.DAMAGE) {
            if (max) {
                if (dark) {
                    return cause.getMaxDarkId();
                } else {
                    return cause.getMaxId();
                }
            } else {
                if (dark) {
                    return cause.getDarkId();
                } else {
                    return cause.getId();
                }
            }
        } else {
            if (dark) {
                return type.getDarkId();
            } else {
                return type.getId();
            }
        }
    }

    public int getDelay() {
        return delay;
    }
}
