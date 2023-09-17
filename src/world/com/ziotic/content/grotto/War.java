package com.ziotic.content.grotto;

import java.util.ArrayList;
import java.util.List;

public class War {

    public static enum Stage {
        SETUP, INGAME, END
    }

    public static enum Rule {

        KILL_ALL(256, 109), LOOSE_ITEMS(512, 116), BAN_MELEE(1024, 120), BAN_RANGE(
                2048, 128), STANDARD_SPELLS_ONLY(4096, 122), BINDS_ONLY(8192,
                122), NO_MAGIC(12288, 122), BAN_SUMMON(16384, 132), BAN_EAT(
                32768, 134), BAN_DRINK(65536, 136), BAN_PRAY(131072, 130);

        private final int config;
        private final int toggle;

        Rule(int config, int toggle) {
            this.config = config;
            this.toggle = toggle;
        }

        public int getConfig() {
            return config;
        }

        public int getToggle() {
            return toggle;
        }
    }

    private final static int[] TIME_CONFIGS = {100, 0, 0, 60, 5, 16, 63, 10,
            32, 66, 30, 48, 69, 60, 64, 72, 90, 80, 75, 120, 96, 78, 150, 112,
            81, 180, 128, 84, 240, 144, 87, 300, 160, 90, 360, 176, 93, 480,
            192
    };

    private final static int[] GOAL_CONFIGS = {20, 0, 0, 25, 25, 1, 28, 50, 2,
            31, 100, 3, 34, 200, 4, 37, 400, 5, 40, 1000, 7, 46, 2500, 8, 49,
            5000, 9, 52, 10000, 10, 56, -1, 15
    };

    private Stage stage = Stage.SETUP;
    private List<Rule> rules = new ArrayList<Rule>();
    private int limit;
    private int goal;
    private int limitConfig;
    private int goalConfig;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isSet(Rule rule) {
        return rules.contains(rules);
    }

    public int calculate() {
        int config = goalConfig + limitConfig;
        for (Rule rule : rules) {
            config += rule.getConfig();
        }
        return config;
    }

    public void toggle(Rule rule) {
        if (rules.contains(rule)) {
            rules.remove(rule);
        } else {
            switch (rule) {
                case BINDS_ONLY:
                    rules.remove(Rule.STANDARD_SPELLS_ONLY);
                    break;
                case NO_MAGIC:
                    rules.remove(Rule.BINDS_ONLY);
                    break;
            }
            rules.add(rule);
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getGoal() {
        return goal;
    }

    public void onClick(int button) {
        for (int i = 0; i < TIME_CONFIGS.length; i += 3) {
            if (TIME_CONFIGS[i] == button) {
                limit = TIME_CONFIGS[i + 1];
                limitConfig = TIME_CONFIGS[i + 2];
                return;
            }
        }
        for (int i = 0; i < GOAL_CONFIGS.length; i += 3) {
            if (GOAL_CONFIGS[i] == button) {
                goal = GOAL_CONFIGS[i + 1];
                goalConfig = GOAL_CONFIGS[i + 2];
                return;
            }
        }
        switch (button) {
            case 122:
                if (!rules.contains(Rule.STANDARD_SPELLS_ONLY) && !rules.contains(Rule.BINDS_ONLY) && !rules.contains(Rule.NO_MAGIC)) {
                    rules.add(Rule.STANDARD_SPELLS_ONLY);
                } else if (rules.contains(Rule.STANDARD_SPELLS_ONLY)) {
                    toggle(Rule.BINDS_ONLY);
                } else {
                    toggle(Rule.NO_MAGIC);
                }
                return;
            case 106:
                rules.remove(Rule.KILL_ALL);
                return;
        }
        for (Rule rule : Rule.values()) {
            if (button == rule.getToggle()) {
                toggle(rule);
                break;
            }
        }
    }

}
