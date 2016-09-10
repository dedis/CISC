package com.epfl.dedis.api;

import com.epfl.dedis.cisc.Activity;
import com.epfl.dedis.cisc.R;
import com.epfl.dedis.crypto.Utils;
import com.epfl.dedis.net.Config;
import com.epfl.dedis.net.HTTP;
import com.epfl.dedis.net.Identity;
import com.google.gson.annotations.SerializedName;

import net.i2p.crypto.eddsa.EdDSAEngine;

public class ProposeVote implements Message {

    private class ProposeVoteMessage {

        @SerializedName("ID")
        private String id;

        @SerializedName("Signer")
        private String signer;

        @SerializedName("Signature")
        private String signature;
    }

    private Activity activity;
    private Identity identity;
    private Config proposed;

    public ProposeVote(Activity activity, Identity identity) {
        this(activity, identity, false);
    }

    public ProposeVote(Activity activity, Identity identity, boolean wait) {
        this.activity = activity;
        this.identity = identity;

        ProposeVoteMessage proposeVoteMessage = new ProposeVoteMessage();
        proposeVoteMessage.id = Utils.encodeBase64(identity.getId());
        proposeVoteMessage.signer = identity.getName();

        try {
            EdDSAEngine engine = new EdDSAEngine();
            engine.initSign(identity.getPrivate());
            proposeVoteMessage.signature = Utils.encodeBase64(engine.signOneShot(identity.getProposed().hash()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HTTP http = new HTTP(this, identity.getCothority(), PROPOSE_VOTE, Utils.toJson(proposeVoteMessage));
        if (wait) {
            String result = http.doInBackground();
            http.onPostExecute(result);
        } else {
            http.execute();
        }
    }

    public void callback(String result) {
        System.out.println(result);
    }

    public void callbackError(int error) {
        switch (error) {
            case 400: activity.taskFail(R.string.err_400); break;
            case 500: activity.taskFail(R.string.err_500); break;
            case 501: activity.taskFail(R.string.err_501); break;
            case 502: activity.taskFail(R.string.err_502); break;
            case 503: activity.taskFail(R.string.err_503); break;
            case 504: activity.taskFail(R.string.err_504); break;
            default: activity.taskFail(R.string.err_unknown);
        }
    }
}
