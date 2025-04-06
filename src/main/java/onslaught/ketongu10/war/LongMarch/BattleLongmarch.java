package onslaught.ketongu10.war.LongMarch;

import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.War;

import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.TIME_BETWEEN_WAVES;
import static onslaught.ketongu10.util.handlers.ConfigHandler.print;

public class BattleLongmarch extends Battle {



    public BattleLongmarch(War war, BattleType type) {
       super(war, type);
    }

    public BattleLongmarch(War war) {
        super(war);
    }

    protected Consumer<Integer> getPlot() {

        this.wavesLeft = 1;
        this.wavesTotal = 1;
        return this::longmarch;

    }
    protected void longmarch(int a) {
        int d = wavesTotal-wavesLeft < 3 ? wavesTotal-wavesLeft : 0;
        int wave = d+1;
        if (timer==100+TIME_BETWEEN_WAVES*d) {
            prepareWave(wave);
            print("===============PREPAIRING "+wave+" WAVE=============");
            return;
        }
        if (timer==200+TIME_BETWEEN_WAVES*d) {
            setUpWave();
            print("===============SETTING UP "+wave+" WAVE=============");
            return;
        }
        if (timer==300+TIME_BETWEEN_WAVES*d) {
            wavesLeft--;
            startWave();
            print("===============STARTING "+wave+" WAVE=============");
            return;
        }
    }
}
