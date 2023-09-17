package com.ziotic.adapter.protocol;

import com.ziotic.adapter.ClientConfiguration;
import com.ziotic.engine.login.LoginResponse;

/**
 * @author Lazaro
 */
public class ClientConfigurationAdapter implements ClientConfiguration {
    private static final int[] FRAME_LENGTHS = new int[256];

    static {
        for (int i = 0; i < FRAME_LENGTHS.length; i++) {
            FRAME_LENGTHS[i] = -3;
        }
        FRAME_LENGTHS[0] = 4;
        FRAME_LENGTHS[1] = 7;
        FRAME_LENGTHS[2] = 6;
        FRAME_LENGTHS[3] = 3;
        FRAME_LENGTHS[4] = 1;
        FRAME_LENGTHS[5] = 7;
        FRAME_LENGTHS[6] = 8;
        FRAME_LENGTHS[7] = 3;
        FRAME_LENGTHS[8] = -1;
        FRAME_LENGTHS[9] = -1;
        FRAME_LENGTHS[10] = 4;
        FRAME_LENGTHS[11] = 2;
        FRAME_LENGTHS[12] = 7;
        FRAME_LENGTHS[13] = 8;
        FRAME_LENGTHS[14] = -1;
        FRAME_LENGTHS[15] = -1;
        FRAME_LENGTHS[16] = 12;
        FRAME_LENGTHS[17] = 7;
        FRAME_LENGTHS[18] = -1;
        FRAME_LENGTHS[19] = 3;
        FRAME_LENGTHS[20] = 8;
        FRAME_LENGTHS[21] = 7;
        FRAME_LENGTHS[22] = 7;
        FRAME_LENGTHS[23] = 11;
        FRAME_LENGTHS[24] = 2;
        FRAME_LENGTHS[25] = -1;
        FRAME_LENGTHS[26] = 2;
        FRAME_LENGTHS[27] = 8;
        FRAME_LENGTHS[28] = -1;
        FRAME_LENGTHS[29] = 15;
        FRAME_LENGTHS[30] = 1;
        FRAME_LENGTHS[31] = -1;
        FRAME_LENGTHS[32] = 3;
        FRAME_LENGTHS[33] = 4;
        FRAME_LENGTHS[34] = 3;
        FRAME_LENGTHS[35] = 6;
        FRAME_LENGTHS[36] = 15;
        FRAME_LENGTHS[37] = 7;
        FRAME_LENGTHS[38] = 2;
        FRAME_LENGTHS[39] = -1;
        FRAME_LENGTHS[40] = 2;
        FRAME_LENGTHS[41] = 8;
        FRAME_LENGTHS[42] = 16;
        FRAME_LENGTHS[43] = 8;
        FRAME_LENGTHS[44] = 3;
        FRAME_LENGTHS[45] = 18;
        FRAME_LENGTHS[46] = 3;
        FRAME_LENGTHS[47] = 2;
        FRAME_LENGTHS[48] = 3;
        FRAME_LENGTHS[49] = 6;
        FRAME_LENGTHS[50] = 0;
        FRAME_LENGTHS[51] = 4;
        FRAME_LENGTHS[52] = 3;
        FRAME_LENGTHS[53] = 4;
        FRAME_LENGTHS[54] = -1;
        FRAME_LENGTHS[55] = -1;
        FRAME_LENGTHS[56] = -1;
        FRAME_LENGTHS[57] = 3;
        FRAME_LENGTHS[58] = 4;
        FRAME_LENGTHS[59] = 3;
        FRAME_LENGTHS[60] = -1;
        FRAME_LENGTHS[61] = -1;
        FRAME_LENGTHS[62] = 8;
        FRAME_LENGTHS[63] = 0;
        FRAME_LENGTHS[64] = 8;
        FRAME_LENGTHS[65] = -1;
        FRAME_LENGTHS[66] = -1;
        FRAME_LENGTHS[67] = 8;
        FRAME_LENGTHS[68] = 11;
        FRAME_LENGTHS[69] = 5;
        FRAME_LENGTHS[70] = -1;
        FRAME_LENGTHS[71] = 8;
        FRAME_LENGTHS[72] = 16;
        FRAME_LENGTHS[73] = 7;
        FRAME_LENGTHS[74] = 7;
        FRAME_LENGTHS[75] = 3;
        FRAME_LENGTHS[76] = -1;
        FRAME_LENGTHS[77] = 3;
        FRAME_LENGTHS[78] = -1;
        FRAME_LENGTHS[79] = -1;
        FRAME_LENGTHS[80] = 7;
        FRAME_LENGTHS[81] = 2;
        FRAME_LENGTHS[82] = 3;
        FRAME_LENGTHS[83] = 0;
        FRAME_LENGTHS[84] = 4;
        FRAME_LENGTHS[85] = -1;
    }

    public int getClientVersion() {
        return 640;
    }

    public int getLoginResponseCode(LoginResponse resp) {
        switch (resp) {
            case LOGIN:
                return 2;
            case INVALID_DETAILS:
                return 3;
            case BANNED:
                return 4;
            case ALREADY_ONLINE:
                return 5;
            case CLIENT_UPDATED:
                return 6;
            case WORLD_FULL:
                return 7;
            case LOGIN_SERVER_OFFLINE:
                return 8;
            case LOGIN_LIMIT_EXCEEDED:
                return 9;
            case MEMBERS_REQUIRED:
                return 12;
            case ERROR:
                return 13;
            case CURRENTLY_UPDATING:
                return 14;
            case IP_BANNED:
                return 26;
        }
        return 13;
    }

    public LoginResponse getLoginResponseForCode(int code) {
        switch (code) {
            case 2:
                return LoginResponse.LOGIN;
            case 3:
                return LoginResponse.INVALID_DETAILS;
            case 4:
                return LoginResponse.BANNED;
            case 5:
                return LoginResponse.ALREADY_ONLINE;
            case 6:
                return LoginResponse.CLIENT_UPDATED;
            case 7:
                return LoginResponse.WORLD_FULL;
            case 8:
                return LoginResponse.LOGIN_SERVER_OFFLINE;
            case 9:
                return LoginResponse.LOGIN_LIMIT_EXCEEDED;
            case 12:
                return LoginResponse.MEMBERS_REQUIRED;
            case 13:
                return LoginResponse.ERROR;
            case 14:
                return LoginResponse.CURRENTLY_UPDATING;
            case 26:
                return LoginResponse.IP_BANNED;
        }
        return LoginResponse.ERROR;
    }

    @Override
    public int[] getFrameLengths() {
        return FRAME_LENGTHS;
    }
}
